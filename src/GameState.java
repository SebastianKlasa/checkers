import java.util.ArrayList;
import java.util.List;

public class GameState implements Cloneable{
    
    // Variables
    public int boardSizeX;
    public int boardSizeY;
    Player[] players;
    public Pawn[][] board;
    public int currentPlayerId;
    public Pawn selectedPawn;
    public boolean gameReady = false;
    public boolean autoPlay = true;
    public boolean sendNotifications = true;
    public int moveCounter=0;
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        GameState tmp = new GameState();
        Pawn [][]b=new Pawn[this.boardSizeX][this.boardSizeY];
        tmp.players[0]=(Player)this.players[0].clone();
        tmp.players[1]=(Player)this.players[1].clone();
        for(int x=0;x<boardSizeX;x++){
             for(int y=0;y<boardSizeY;y++){
                 if(board[x][y]!=null){
                    Pawn newPawn=(Pawn)board[x][y].clone();
                    b[x][y]=newPawn;
                    tmp.players[board[x][y].playerId].pawns.add(newPawn);
                 }
            }
        }
        tmp.board=b;
        tmp.boardSizeX=this.boardSizeX;
        tmp.boardSizeY=this.boardSizeY;
        tmp.currentPlayerId=this.currentPlayerId;
        tmp.selectedPawn=this.selectedPawn;
        tmp.gameReady=this.gameReady;
        tmp.sendNotifications=this.sendNotifications;
        tmp.autoPlay=false;
        return tmp;
    }
    
    public boolean isGameOver(){
        ArrayList<Move> moves=getAllPossibleAttacksForPlayer(getCurrentPlayerId());
        if(moves.isEmpty()){
            moves=getAllPossibleMovesForPlayer(getCurrentPlayerId());
        }
        if(moves.isEmpty()){
            return true;
        }
        return false;
    }
    
    //reszta klasy
    
    // Get; Set;
    
    Pawn getPawn(int x, int y){
        return board[x][y];
    }
    
    void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }
    public void SetPlayerType(Object type, int playerId){
        players[playerId].setPlayerType(Player.PlayerType.valueOf((String)type));
    }
    public void setIsKing(Pawn pawn, boolean isKing){
        pawn.setIsKing(isKing);

        fieldChanged(selectedPawn.x, selectedPawn.y, selectedPawn.playerId, false, selectedPawn.getIsKing());
    }
    // Score
    public void setScore_AddCapture(int playerId){
        players[playerId].score.CapturesCount++;
        players[playerId].score.TotalCapturesCount++;

        scoreChanged(playerId, players[playerId].score, false);
    }
    public void setScore_AddWin(int playerId){
        players[playerId].score.WinsCount++;
        scoreChanged(playerId, players[playerId].score, true);
    }
    public void setScore_AddDraw(){
        for(int i=0;i<2;i++){
            players[i].score.DrawsCount++;
            scoreChanged(i, players[i].score, true);
        }
    }
    public void resetCaptures(){
        players[0].score.CapturesCount = 0;
        players[1].score.CapturesCount = 0;
         
        scoreChanged(0, players[0].score, false);
        scoreChanged(1, players[1].score, false);
    }
    public void resetTotalScore(){
        players[0].score = new Score();
        players[1].score = new Score();
        
        scoreChanged(0, players[0].score, true);
        scoreChanged(1, players[1].score, true);
    }
    // Current Player
    public int getCurrentPlayerId() {
      return currentPlayerId;
    }
    public void setCurrentPlayerId( int whosTurn ) {
      this.currentPlayerId = whosTurn;
      turnChanged(whosTurn);
    }
    public void SetNextPlayersTurn(){
        setCurrentPlayerId((currentPlayerId+1)%2);
    }
    private void SetTheSamePlayersTurn(){
        setCurrentPlayerId(currentPlayerId);
    }
    public Player getCurrentPlayer(){
        return players[currentPlayerId];
    }
    private boolean isCurrentPlayerAHuman() {
        return players[currentPlayerId].getPlayerType() == Player.PlayerType.Player;
    }
    
    
    // Constructor
    public GameState() {
        players = new Player[2];
        players[0] = new Player();
        players[1] = new Player();
    }
    public void Restart(int sizeX, int sizeY){       
        gameReady = false;
        boardSizeX = sizeX;
        boardSizeY = sizeY;
        moveCounter=0;
        
        setCurrentPlayerId(0);
        board = new Pawn[sizeX][sizeY];
        
        players[0].Restart();
        players[1].Restart();
        resetCaptures();
        
        for (int x=0; x<boardSizeX; x++) {
            for (int y=0; y<boardSizeX; y++) {
                if(x%2!=y%2)
                {
                    if(y<boardSizeY/2-1)
                    {
                        int playerId = 1;
                        Pawn newPawn = new Pawn(playerId,x,y);
                        players[playerId].addPawn(newPawn);
                        board[x][y] = newPawn;
                        
                        fieldChanged(x,y,playerId, false, false);
                    }
                    else if(y>boardSizeY/2)
                    {
                        int playerId = 0;
                        Pawn newPawn = new Pawn(playerId,x,y);
                        players[playerId].addPawn(newPawn);
                        board[x][y] = newPawn;
                        
                        fieldChanged(x,y,playerId, false, false);
                    }
                    else 
                    {
                        fieldChanged(x,y,2, false, false);
                    }
                }
            }
        }
        System.out.println("GAME STATE RESTARTED");
        gameReady = true;
    }


    // Player or bot must call this method to make a move.
    public void move(int x, int y, int newX, int newY){ 
        Move move =  getMoveIfPossible(x,y,newX,newY);
        if(move == null)return;
        
        boolean moveDone = false;

        if(selectedPawn.getIsKing())
        { 
            ArrayList<Pawn> pawnsOnTheWay = getPawnsToCaptureIfMovePossible(move);
            if(pawnsOnTheWay == null) return;
            if(pawnsOnTheWay.isEmpty()) moveCounter++;
            else moveCounter=0;
            movePawn(move);
            pawnsOnTheWay.forEach((pawnToRemove) -> {
                capturePawn(pawnToRemove.x, pawnToRemove.y);
            });
            
            ArrayList<Move> possibleAttacks = getPossibleAttacksForPawn(selectedPawn);
            boolean attackPossible = !possibleAttacks.isEmpty();
            
            if(attackPossible == false || pawnsOnTheWay.isEmpty())
            {
                SetNextPlayersTurn();
                if(players[currentPlayerId].getPlayerType() == Player.PlayerType.Player){
                    highlightAllPossibleMoves();
                }
            }
            else
            {
                SetTheSamePlayersTurn();
                if (isCurrentPlayerAHuman()) highlightPawnsField(selectedPawn);
            }
            
            //if(sendNotifications)System.out.println("KING MOVE : ("+x+","+y+")->("+newX+","+newY+")");
        }
        else if(selectedPawn.getIsKing() == false)
        {
            if(canPawnMove(move) == false) return;
            else if(move.distance == 1)
            {
                movePawn(move);
                moveDone = true;
                moveCounter++;
            }
            else if(move.distance == 2)
            {
                movePawn(move);
                capturePawn((x+newX)/2, (y+newY)/2);
                moveCounter=0;

                if(getPossibleAttacksForPawn(move.pawn).size() > 0)
                {
                    SetTheSamePlayersTurn();
                    if(isCurrentPlayerAHuman())highlightPawnsField(selectedPawn);
                }
                else
                {
                    moveDone = true;
                }
            }
            
            if(moveDone) {
                if(ShouldBecomeKing(selectedPawn))
                    setIsKing(selectedPawn ,true);

                SetNextPlayersTurn();
                if(isCurrentPlayerAHuman()) highlightAllPossibleMoves();
            }
        }
        
        //if(sendNotifications)System.out.println("PAWN MOVE : ("+x+","+y+")->("+newX+","+newY+")");
        
        if(players[currentPlayerId].hasPawnsLeft()==false || (getAllPossibleAttacksForPlayer(currentPlayerId).isEmpty() && getAllPossibleMovesForPlayer(currentPlayerId).isEmpty()) || moveCounter>30)
        {
            if(moveCounter<=30) setScore_AddWin(1-currentPlayerId);
            else setScore_AddDraw();
            gameReady = false;
            if(autoPlay)Restart(boardSizeX, boardSizeY);
        }
    }
    
    // Help with moving, attacking
    public boolean ShouldBecomeKing(Pawn pawn){
        if(pawn.playerId == 0 && pawn.y == 0)return true;
        return pawn.playerId == 1 && pawn.y == boardSizeY-1;
    }
    private boolean canPawnMove(Move move) {
        if(move.distance > 2)
        {
            System.out.println("Pawn can't move so far!");
            return false;
        }
        else if(move.distance == 1)
        {
            if(getAllPossibleAttacksForPlayer(currentPlayerId).isEmpty() == false)
            {
                System.out.println("You must attack when possible!");
                return false;
            }
            else if((currentPlayerId == 0 && move.dirY == 1) || (currentPlayerId == 1 && move.dirY == -1))
            {
                System.out.println("Pawn can't move backward!");
                return false;
            }
        }
        else if(move.distance == 2)
        {
            int testedX = move.pawn.x;
            int testedY = move.pawn.y;
            
            for(int i=0; i<move.distance; i++)
            {   
                testedX += move.dirX;
                testedY += move.dirY;
                
                if (board[testedX][testedY] != null && board[testedX][testedY].playerId == move.pawn.playerId)
                {
                    System.out.println("Your own pawn block the way!");
                    return false;
                }
                if(i == 0)
                {
                    if (board[testedX][testedY] == null)
                    {
                        System.out.println("Pawn can't move so far!");
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    private ArrayList<Pawn> getPawnsToCaptureIfMovePossible(Move move) {
        int pawnsInARow = 0;
        ArrayList<Pawn> pawnsOnTheWay = new ArrayList<>();
        
        int testedX = move.pawn.x;
        int testedY = move.pawn.y;
                    
        for(int i=0; i<move.distance; i++)
        {   
            testedX += move.dirX;
            testedY += move.dirY;
                
            if (board[testedX][testedY] != null)
            {
                if(board[testedX][testedY].playerId == selectedPawn.playerId)
                {
                    System.out.println("Your own pawn block the way!");
                    return null;
                }
                pawnsOnTheWay.add(board[testedX][testedY]);
                pawnsInARow++;
                    
                if(pawnsInARow == 2)
                {
                    System.out.println("2 pawns in the row on the way!");
                    return null;
                }
            }
            else pawnsInARow = 0;
        }
        
        if(pawnsOnTheWay.isEmpty() && getAllPossibleAttacksForPlayer(currentPlayerId).isEmpty() == false ){
            System.out.println("Ruch bez bicia, bicie wymagane!");
            return null;
        }
        
        return pawnsOnTheWay;
    }
    private Move getMoveIfPossible(int x, int y, int newX, int newY){
        selectedPawn = board[x][y];
        int distance = Math.abs(newX-x);
        int yMove = newY-y;
        int xMove = newX-x;
        
        if(gameReady == false)
        {
            System.out.println("GameNotReady");
            return null;
        }
        if(board[newX][newY] != null)
        {
            System.out.println("Field"+newX+" "+newY+" already taken!");
            return null;
        }
        if(selectedPawn == null)
        {
            System.out.println("SelectedPawnWasNULL!");
            return null;
        }
        if(Math.abs(xMove) != Math.abs(yMove))
        {
            System.out.println("Pawns cannot move that way!");
            return null;
        }
        
        return new Move(xMove/distance, yMove/distance, distance, selectedPawn, 0);
    }
    
    // Move or Remove Pawn
    private void capturePawn(int x, int y){
        Pawn pawn = board[x][y];
        board[x][y] = null;
        players[pawn.playerId].removePawn(pawn);
        setScore_AddCapture(1-pawn.playerId);
        
        fieldChanged(x,y,2, false, pawn.getIsKing());
    }
    private void movePawn(Move move){
        selectedPawn = board[move.pawn.x][move.pawn.y];
        board[move.pawn.x][move.pawn.y] = null;
        clearField(selectedPawn);
        
        selectedPawn.x = move.GetFinalX();
        selectedPawn.y = move.GetFinalY();
        board[selectedPawn.x][selectedPawn.y] = selectedPawn;
        updateField(selectedPawn);
    }

    
    
    // Find possible moves or attacks
    private Move getMove(Pawn p, int dirX, int dirY) {
        int finalX = p.x+dirX;
        int finalY = p.y+dirY;
        
        // out of border?
        if(finalX < 0 || finalY < 0 || finalX >= boardSizeX || finalY >= boardSizeY) return null;
        
        if(board[finalX][finalY] == null)
        {
            return new Move(dirX, dirY, 1, p, 0);
        }
        else return null;
    }
    public Move getAttack(Pawn p, int dirX, int dirY) {
        if(p.getIsKing())
        {            
            // destination field coordinates
            int finalX = p.x+2*dirX;
            int finalY = p.y+2*dirY;
            
            int distance = 2;
            int maxAttackDistance = 0;
            
            while(finalX >= 0 && finalY >= 0 && finalX < boardSizeX && finalY < boardSizeY )
            {
               Pawn pawnOnTheWay = board[finalX-dirX][finalY-dirY];
               
               if(pawnOnTheWay != null)
               {
                    if(pawnOnTheWay.playerId == p.playerId || board[finalX][finalY] != null) break;
                    maxAttackDistance = distance;
               }
               
               finalX += dirX;
               finalY += dirY;
               distance ++;
            }
            if(maxAttackDistance>0)
            {
                
                Move move = new Move(dirX, dirY, maxAttackDistance, p, 0);
                return move;
            }
            else 
            {
                return null;
            }
        }
        else
        {        
            int finalX = p.x+2*dirX;
            int finalY = p.y+2*dirY;

            // out of border?
            if(finalX < 0 || finalY < 0 || finalX >= boardSizeX || finalY >= boardSizeY) return null;

            boolean isEnemyPawnOnTheWay = 
                    (board[p.x+dirX][p.y+dirY] != null 
                    && board[p.x+dirX][p.y+dirY].playerId != p.playerId);

            if(isEnemyPawnOnTheWay && board[finalX][finalY] == null)
            {
                return new Move(dirX, dirY, 2, p, 0);
            }
            else return null;
        }
    } 
    
    public ArrayList<Move> getPossibleMovesForPawn(Pawn pawn){
        ArrayList<Move> moves = new ArrayList<>();
        
        if(pawn.playerId == 0)
        {
            Move move = getMove(pawn, -1, -1);
            if(move != null) moves.add(move);
            move = getMove(pawn, +1, -1);
            if(move != null) moves.add(move);
            
            if(pawn.getIsKing())
            {
                move = getMove(pawn, +1, +1);
                if(move != null) moves.add(move);
                move = getMove(pawn, -1, +1);
                if(move != null) moves.add(move);
            }
        }
        else
        {
            Move move = getMove(pawn, +1, +1);
            if(move != null) moves.add(move);
            move = getMove(pawn, -1, +1);
            if(move != null) moves.add(move);
            
            if(pawn.getIsKing())
            {
                 move = getMove(pawn, -1, -1);
                if(move != null) moves.add(move);
                move = getMove(pawn, +1, -1);
                if(move != null) moves.add(move);
            }
        }

        return moves;
    }
    public ArrayList<Move> getPossibleAttacksForPawn(Pawn pawn){
        ArrayList<Move> moves = new ArrayList<>();
        
        if(pawn.playerId == 0)
        {
            Move attack = getAttack(pawn, -1, -1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, -1, +1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, +1, -1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, +1, +1);
            if(attack != null) moves.add(attack);
        }
        else
        {
            Move attack = getAttack(pawn, -1, -1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, -1, +1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, +1, -1);
            if(attack != null) moves.add(attack);
            attack = getAttack(pawn, +1, +1);
            if(attack != null) moves.add(attack);
        }
        
        return moves;
    }
    
    
    public ArrayList<Move> getAllPossibleAttacksForPlayer(int playerId){
        ArrayList<Move> attacks = new ArrayList<>();
        players[playerId].pawns.forEach((pawn) -> {
            attacks.addAll(getPossibleAttacksForPawn(pawn));
        });
        return attacks;
    }
    
    
    public ArrayList<Move> getAllPossibleMovesForPlayer(int playerId){
        ArrayList<Move> moves = new ArrayList<>();
        players[playerId].pawns.forEach((pawn) -> {
            moves.addAll(getPossibleMovesForPawn(pawn));
        });
        return moves;
    }

    
    
    // Interaction With GUI
    public interface FieldChangedListener {
        public void fieldChanged(int x, int y, int player, boolean highlight, boolean isKing);
    }
    public interface TurnChangedListener {
        public void turnChanged(int player);
    }
    public interface ScoreChangedListener {
        public void scoreChanged(int player, Score score);
    }
    
    private final List<FieldChangedListener> fieldChangedListeners = new ArrayList<>();
    public void addListener_FieldChanged(FieldChangedListener toAdd) {
        fieldChangedListeners.add(toAdd);
    }
    public void fieldChanged(int x, int y, int player, boolean highlight, boolean isKing) {
        if(!sendNotifications)return;
        fieldChangedListeners.forEach((hl) -> {
            hl.fieldChanged(x, y, player, highlight, isKing);
        });
    } 
    private void clearField(Pawn pawn){
        fieldChanged(pawn.x,pawn.y, 2, false, false);
    }
    private void updateField(Pawn pawn){
        fieldChanged(pawn.x,pawn.y,pawn.playerId, false, pawn.getIsKing());
    }
    
    public final List<TurnChangedListener> turnChangedListeners = new ArrayList<>();
    public void addListener_TurnChanged(TurnChangedListener toAdd) {
        turnChangedListeners.add(toAdd);
    }
    public void turnChanged(int player) {
        if(!sendNotifications)return;
        turnChangedListeners.forEach((hl) -> {
            hl.turnChanged(player);
        });
    } 
    
    public final List<ScoreChangedListener> scoreChangedListeners = new ArrayList<>();
    public void addListener_ScoreChanged(ScoreChangedListener toAdd) {
        scoreChangedListeners.add(toAdd);
    }
    public void scoreChanged(int player, Score score, boolean winsChanged) {
        if(!sendNotifications && winsChanged == false)return;
        scoreChangedListeners.forEach((hl) -> {
            hl.scoreChanged(player, players[player].score);
        });
    } 
    
    
    private void highlightPawnsField(Pawn pawn){
        fieldChanged(pawn.x, pawn.y, pawn.playerId, true, pawn.getIsKing());
    }
    private void highlightAllPossibleMoves(){
        ArrayList<Move> attacks = getAllPossibleAttacksForPlayer(currentPlayerId);
        if(attacks.isEmpty() == false)
        {
            attacks.forEach((move) -> {
                fieldChanged(move.pawn.x, move.pawn.y, move.pawn.playerId, true, move.pawn.getIsKing());
            });
        }
    }
    public void Field_Click(int x, int y){
        if(board[x][y] != null)
        {
           selectedPawn = board[x][y];
           getPossibleAttacksForPawn(selectedPawn);
           getPossibleMovesForPawn(selectedPawn);
        }
        else if(selectedPawn != null)
        {
            if(selectedPawn.playerId == currentPlayerId)
            {
                move(selectedPawn.x, selectedPawn.y, x, y);
            }
        }
    }
    
   
}
