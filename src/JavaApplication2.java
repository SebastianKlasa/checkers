import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaApplication2 {

    private final GameState gameState;
    Thread botsThread = new Thread(new BotsController());     
    public boolean running = true;
    private int boardWidth;
    public ArrayList<Position> highlightedFields = new ArrayList<>();
    
    private int depth;
    
    private int imageWidth;
    private final Color whitePlayerTextColor = new Color(130,145,130);
    private final Color redPlayerTextColor = Color.red;
    
    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    private JButton[][] chessBoardSquares;
    private JPanel board;
    private JLabel labelTurn;
    private JLabel labelWhiteScore;
    private JLabel labelRedScore;
    private JLabel labelDraws;
    private final Insets FIELD_MARGIN = new Insets(1,1,1,1);
    private final JFrame frame;
    private JComboBox comboBoxPlayer1Type;
    private JComboBox comboBoxPlayer2Type;
    
    private ImageIcon whiteImage;
    private ImageIcon redImage;
    private ImageIcon whiteKing;
    private ImageIcon redKing;
    
    
    
    // change display when GameState Variables change
    public void UpdateField(int x, int y, int player, boolean highlight, boolean isKing) {
        if(highlight)
        {
            chessBoardSquares[x][y].setBackground(Color.green);
            highlightedFields.add(new Position(x,y));
        }
        else
        {
            chessBoardSquares[x][y].setBackground(Color.black);
        }
        switch (player) {
            case 0:
                if(isKing)chessBoardSquares[x][y].setIcon(new ImageIcon(whiteKing.getImage().getScaledInstance(imageWidth, imageWidth, Image.SCALE_SMOOTH)));
                else chessBoardSquares[x][y].setIcon(new ImageIcon(whiteImage.getImage().getScaledInstance(imageWidth, imageWidth, Image.SCALE_SMOOTH)));
                break;
            case 1:
                if(isKing)chessBoardSquares[x][y].setIcon(new ImageIcon(redKing.getImage().getScaledInstance(imageWidth, imageWidth, Image.SCALE_SMOOTH)));
                else chessBoardSquares[x][y].setIcon(new ImageIcon(redImage.getImage().getScaledInstance(imageWidth, imageWidth, Image.SCALE_SMOOTH)));
                break;
            default:
                chessBoardSquares[x][y].setIcon(null);
                break;
        }
    }
    public void UpdateTurn(int player){
        if(player == 0)
        {
            labelTurn.setText("White Player's Turn");
            labelTurn.setForeground(whitePlayerTextColor);
        }
        else // if(player == 1)
        {
            labelTurn.setText("Red Player's Turn");
            labelTurn.setForeground(Color.red);
        }
        highlightedFields.forEach((p) -> {
            chessBoardSquares[p.x][p.y].setBackground(Color.black);
        });
        highlightedFields.clear();
    }
    private void UpdateScore(int player, Score score) {
      //  if(isDisplayOn==false)return;
        JLabel label;
        JLabel drawLabel;
        
        if (player == 0){  
            label = labelWhiteScore;
            drawLabel= labelDraws;
            label.setText("Wins:"+score.WinsCount+"   Captures:"+score.CapturesCount+"   TotalCaptures:"+score.TotalCapturesCount);
            drawLabel.setText("Draws: "+score.DrawsCount);
        }
        else{
            label = labelRedScore;
            label.setText("Wins:"+score.WinsCount+"   Captures:"+score.CapturesCount+"   TotalCaptures:"+score.TotalCapturesCount);
        }
    }
    
    
    // start, restart
    private void NewGame(){
        String test1= JOptionPane.showInputDialog("Please input board size (6>x>12)");
        try
        {
            int a = Integer.parseInt(test1);
            Restart(a);
        }
        catch(NumberFormatException e){
            System.out.println("Wrong Input");
        }
        
    }
    private void Restart(int x){
        GenerateBoard(x);
        gameState.Restart(boardWidth, boardWidth);
        
        if (botsThread.isAlive() == false) botsThread.start();
    }
    
    
    // Initialize
    public static void main(String[] args) {
        Runnable r = () -> {
            JavaApplication2 cb = new JavaApplication2();
        };
        SwingUtilities.invokeLater(r);
    }
    JavaApplication2() {
        frame = new JFrame("Warcaby");
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
            }
        });
        
        initializeGui();
        
        frame.add(gui);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationByPlatform(true);

        // ensures the frame is the minimum size it needs to be
        // in order display the components within it
        frame.pack();
        frame.setVisible(true);
        
        gameState = new GameState();
        gameState.addListener_FieldChanged((x,y,player, highlight, isKing) -> {UpdateField(x, y, player,highlight, isKing);});
        gameState.addListener_TurnChanged((player) -> {UpdateTurn(player);});
        gameState.addListener_ScoreChanged((player, score) -> {UpdateScore(player, score);});
        
        NewGame();
    }
    public final void initializeGui() {
        // set up the main GUI
        
        whiteImage = new ImageIcon(this.getClass().getResource("/Untitled.png"));
        redImage = new ImageIcon(this.getClass().getResource("/Untitled2.png"));
        whiteKing = new ImageIcon(this.getClass().getResource("/WhiteKing.png"));
        redKing = new ImageIcon(this.getClass().getResource("/RedKing.png"));
        
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        gui.add(tools, BorderLayout.PAGE_START);
        
        JButton buttonNew = new JButton("Resize");
        JButton buttonRestart = new JButton("Restart");
        JButton buttonResetScore = new JButton("ResetScore");
        JCheckBox buttonAutoPlay = new JCheckBox("AutoPlayON/OFF");
        JCheckBox buttonDisplay = new JCheckBox("DisplayON/OFF");
        buttonAutoPlay.setSelected(true);
        buttonDisplay.setSelected(true);
        
        
        labelTurn = new JLabel();
        JPanel panel = new JPanel();
        JPanel panel_Combo = new JPanel();
        JPanel panel_Score = new JPanel();
        JPanel panel_ScoreButtons = new JPanel();
        Dimension comboBoxMaxSize = new Dimension(200,30);
        labelWhiteScore = new JLabel("");
        labelRedScore = new JLabel("");
        labelDraws=new JLabel("");
        
        panel.add(labelTurn);
        Dimension minSize = new Dimension(200,55);
        panel.setPreferredSize(minSize);
        Border border = labelTurn.getBorder();
        Border margin = new EmptyBorder(0,10,0,10);
        labelTurn.setBorder(new CompoundBorder(border, margin));
        labelTurn.setFont(new Font("Century Gothic", Font.BOLD, 35));
        
        
        String[] PlayerTypeStrings = Player.getPlayerTypes();
        comboBoxPlayer1Type = new JComboBox(PlayerTypeStrings);
        comboBoxPlayer2Type = new JComboBox(PlayerTypeStrings);
        
        
        panel_Combo.setBorder(new CompoundBorder(border, margin));
        panel_Combo.setLayout(new BoxLayout(panel_Combo, BoxLayout.Y_AXIS));
        
        panel_Score.setBorder(new CompoundBorder(border, margin));
        panel_Score.setLayout(new BoxLayout(panel_Score, BoxLayout.Y_AXIS));
        
        panel_ScoreButtons.setBorder(new CompoundBorder(border, margin));
        panel_ScoreButtons.setLayout(new BoxLayout(panel_ScoreButtons, BoxLayout.Y_AXIS));
        
        
        comboBoxPlayer1Type.setForeground(whitePlayerTextColor);
        comboBoxPlayer2Type.setForeground(redPlayerTextColor);
        
        labelWhiteScore.setForeground(whitePlayerTextColor);
        labelRedScore.setForeground(redPlayerTextColor);
        
        comboBoxPlayer1Type.setMaximumSize(comboBoxMaxSize);
        comboBoxPlayer2Type.setMaximumSize(comboBoxMaxSize);
      
        
        
        
        // listeners-------------------------------------------------------------------------------------------------------
        buttonResetScore.addActionListener((e)->{gameState.resetTotalScore();});
        buttonDisplay.addActionListener((e)->{ gameState.sendNotifications = buttonDisplay.isSelected();});
        buttonAutoPlay.addActionListener((e)->{ gameState.setAutoPlay(buttonAutoPlay.isSelected());});
        buttonNew.addActionListener(e ->{ NewGame();});
        buttonRestart.addActionListener(e -> { Restart(boardWidth);});
        comboBoxPlayer1Type.addActionListener(e -> {gameState.SetPlayerType(comboBoxPlayer1Type.getSelectedItem(), 0);});
        comboBoxPlayer2Type.addActionListener(e -> {gameState.SetPlayerType(comboBoxPlayer2Type.getSelectedItem(), 1);});
        //------------------------------------------------------------------------------------------------------------------
        
        
        
        
        tools.add(buttonNew);
        tools.add(buttonRestart);
        tools.add(Box.createHorizontalGlue());
        
        panel_ScoreButtons.add(buttonResetScore);
        panel_ScoreButtons.add(buttonAutoPlay);
        panel_ScoreButtons.add(buttonDisplay);
        
        panel_Score.add(labelWhiteScore);
        panel_Score.add(labelRedScore);
        panel_Score.add(labelDraws);
        
        tools.add(panel_ScoreButtons);
        tools.add(panel_Score);
        
        panel_Combo.add(comboBoxPlayer1Type);
        panel_Combo.add(comboBoxPlayer2Type);
        
        tools.add(panel_Combo);
        tools.add(panel);
        
        
        //GenerateBoard(5);
    }
    public void GenerateBoard(int boardWidth){    
        if (board != null) gui.remove(board);
        this.boardWidth = boardWidth;
        
        board = new JPanel(new GridLayout(0, boardWidth+1));
        board.setBorder(new LineBorder(Color.BLACK));
        gui.add(board);
        
        
        chessBoardSquares = new JButton[boardWidth][boardWidth];
        
        imageWidth = 80 - boardWidth*2;
        if(imageWidth<10)imageWidth = 10;
        
        Dimension dimension = new Dimension(Math.max((imageWidth+20)*boardWidth+250, 1100), (imageWidth+3)*boardWidth+100);
        frame.setMinimumSize(dimension);
        
        for (int i=0; i<boardWidth; i++) {
            for (int j=0; j<boardWidth; j++) {
                JButton newButton = new JButton();
                newButton.setMargin(FIELD_MARGIN);
                
                // Color board on black and white.
                if(i%2 == j%2)
                {
                    newButton.setBackground(Color.GRAY);
                    newButton.disable();
                }
                else
                {
                    final int x = i;
                    final int y = j;
                    newButton.setBackground(Color.BLACK);
                    newButton.addActionListener(e ->{ gameState.Field_Click(x, y); });
                }
                
                chessBoardSquares[i][j] = newButton;
            }
        }
        
        // top-left row space
        board.add(new JLabel(""));
        
        // top row
        for (int x = 0; x < boardWidth; x++) { board.add( new JLabel(Integer.toString(x+1), SwingConstants.CENTER)); }

        // left column (line numbers) and all fields
        for (int y = 0; y < boardWidth; y++) {
            for (int x = 0; x < boardWidth; x++) {
                switch (x) {
                    case 0: board.add(new JLabel("" + (y + 1), SwingConstants.CENTER));
                    default: board.add(chessBoardSquares[x][y]);
                }
            }
        }
    }   

    
    // Bots
    private class BotsController implements Runnable {
        Random rand = new Random();
        @Override
        public void run(){
            while(running)
            {
                try
                {
                    if(gameState.gameReady && gameState.getCurrentPlayer().getPlayerType() == Player.PlayerType.BotRandomowy )
                    {
                        ArrayList<Move> allPossibleAttacksForPlayer = gameState.getAllPossibleAttacksForPlayer(gameState.getCurrentPlayerId());
                        
                        if(allPossibleAttacksForPlayer.isEmpty() == false)
                        {
                            Move m = allPossibleAttacksForPlayer.get(rand.nextInt(allPossibleAttacksForPlayer.size()));
                            gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                        }
                        else
                        {
                            ArrayList<Move> allPossibleMovesForPlayer = gameState.getAllPossibleMovesForPlayer(gameState.getCurrentPlayerId());
                            if(allPossibleMovesForPlayer.isEmpty() == false)
                            {
                                Move m = allPossibleMovesForPlayer.get(rand.nextInt(allPossibleMovesForPlayer.size()));
                                gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                            }
                        }
                    }
                    if(gameState.gameReady && gameState.getCurrentPlayer().getPlayerType() == Player.PlayerType.BotHeurystycznyEASY )
                    {
                        depth =4;
                        GameState tmp=(GameState)gameState.clone();
                        
                        Move m = selectMove(tmp, false);
                        gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                    }
                    if(gameState.gameReady && gameState.getCurrentPlayer().getPlayerType() == Player.PlayerType.BotHeurystycznyMEDIUM )
                    {
                        depth = 6;
                        GameState tmp=(GameState)gameState.clone();
                        
                        Move m = selectMove(tmp, false);
                        gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                    }
                    if(gameState.gameReady && gameState.getCurrentPlayer().getPlayerType() == Player.PlayerType.BotHeurystycznyPRO )
                    {
                        depth = 8;
                        GameState tmp=(GameState)gameState.clone();
                        
                        Move m = selectMove(tmp, false);
                        gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                    }
                    if(gameState.gameReady && gameState.getCurrentPlayer().getPlayerType() == Player.PlayerType.BotHeurystycznyEASYprzykrawedzi )
                    {
                        depth = 4;
                        GameState tmp=(GameState)gameState.clone();
                        
                        Move m = selectMove(tmp, true);
                        gameState.move(m.pawn.x, m.pawn.y, m.GetFinalX(), m.GetFinalY());
                    }
                }
                catch(Exception e)
                {
                    System.out.println("Bot nie mógł wykonać ruchu!");
                }
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JavaApplication2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    Move selectMove(GameState gt, boolean edge) throws CloneNotSupportedException{
        ArrayList<Move> allPossibleMoves = gt.getAllPossibleAttacksForPlayer(gt.getCurrentPlayerId());
        
        if(allPossibleMoves.isEmpty()){
            allPossibleMoves = gt.getAllPossibleMovesForPlayer(gt.getCurrentPlayerId());
        }
        
        int m1= minmax(depth, gameState.currentPlayerId, 0, gt, Integer.MIN_VALUE, Integer.MAX_VALUE, edge);
        return allPossibleMoves.get(m1);
    }
    
    public int minmax(int level, int player, int score, GameState gt, int alpha, int beta, boolean edge) throws CloneNotSupportedException{ 
        if (gt.isGameOver() || level == 0){
            return score;
        }
        
        ArrayList<Move> allPossibleMoves = gt.getAllPossibleAttacksForPlayer(gt.getCurrentPlayerId());
        
        if(allPossibleMoves.isEmpty()){
            allPossibleMoves = gt.getAllPossibleMovesForPlayer(gt.getCurrentPlayerId());
        }
        
        Random rand = new Random();
        int index=0;
        if(gameState.currentPlayerId==gt.currentPlayerId){
            for(int i=0;i<allPossibleMoves.size();i++){
                GameState tmp =(GameState)gt.clone();
                int movingPlayer=tmp.currentPlayerId;
                tmp.move(allPossibleMoves.get(i).pawn.x, allPossibleMoves.get(i).pawn.y, allPossibleMoves.get(i).GetFinalX(), allPossibleMoves.get(i).GetFinalY());
                int x = minmax(level-1, tmp.currentPlayerId, score+rate(allPossibleMoves.get(i), tmp, gt, level, movingPlayer, edge), tmp, Integer.MIN_VALUE, beta, edge);
                if(x>=beta){
                    return x;
                }
                if(x>alpha){ 
                    alpha = x;
                    index=i;
                }
                else if(x==alpha){
                    if(rand.nextInt(2)==0){
                        index=i;
                        alpha = x;
                    }
                }
          }
            if(level==depth) {
                return index;
            }
            return alpha;
        }
        else{
            for(int i=0;i<allPossibleMoves.size();i++){
                GameState tmp =(GameState)gt.clone();
                int movingPlayer=tmp.currentPlayerId;
                tmp.move(allPossibleMoves.get(i).pawn.x, allPossibleMoves.get(i).pawn.y, allPossibleMoves.get(i).GetFinalX(), allPossibleMoves.get(i).GetFinalY());
                int x = minmax(level-1, tmp.currentPlayerId, score+rate(allPossibleMoves.get(i), tmp, gt, level, movingPlayer, edge), tmp, alpha, Integer.MAX_VALUE, edge);
                if(x<=alpha){
                    return x;
                }
                
                if(x<beta){
                    index=i;
                    beta = x;
                }
                else if(x==beta){
                    if(rand.nextInt(2)==0){
                        index=i;
                        beta = x;
                    }
                }
             }
            return beta;
        }
   
    }
    
   //funkcje oceniajace
   
     public boolean isPossibleAttack(Move move, GameState gt){
         if(gt.getAttack(move.pawn, move.dirX, move.dirY)!=null) return true;
         return false;
    }
    
    public int power(GameState gt){
       int x1=0;
       Player player=gt.players[gameState.getCurrentPlayerId()];
       int a=player.pawns.size();
       for(int i=0; i<player.pawns.size();i++){
            if(player.pawns.get(i).getIsKing()) x1+=50;
            else x1+=10;
       }
       int x2=0;
       player=gt.players[(gameState.getCurrentPlayerId()+1)%2];
       int b=player.pawns.size();
       for(int i=0; i<player.pawns.size();i++){
            if(player.pawns.get(i).getIsKing()) x2-=50;
            else x2-=10;
       }
       return x1+x2;
    }
    
    int moveEdgePawn(Move move, GameState gt){
        int i=0;
        int x = move.startX;
        int y = move.startY;
        if(x==0||y==0||x==gameState.boardSizeX-1||y==gameState.boardSizeY-1) i+=5;
        else if(x==1||y==1||x==gameState.boardSizeX-2||y==gameState.boardSizeY-2) i+=3;
        return i;
    }
    
    int moveCenterPawn(Move move, GameState gt){
        int i=0;
        int x = move.startX;
        int y = move.startY;
        if(x==0||y==0||x==gameState.boardSizeX-1||y==gameState.boardSizeY-1) i-=5;
        else if(x==1||y==1||x==gameState.boardSizeX-2||y==gameState.boardSizeY-2) i-=3;
        return i;
    }
    
    public int rate(Move move, GameState afterMove,GameState beforeMove, int level, int currentPlayer, boolean edge){
        int x=0;
        if(currentPlayer==gameState.getCurrentPlayerId()){
            if(isPossibleAttack(move, beforeMove)) x+=20;
            if(afterMove.isGameOver()) x+=1000;
            if(edge){
                x+=moveEdgePawn(move, beforeMove);
            }
            else{
                x+=moveCenterPawn(move, beforeMove);
            }
        }
        else {
            if(isPossibleAttack(move, beforeMove)) x-=20;
            if(afterMove.isGameOver()) x-=1000;
            if(edge){
                x-=moveEdgePawn(move, beforeMove);
            }
            else{
                x-=moveCenterPawn(move, beforeMove);
            }
        }
          x+=power(afterMove);
        
        return x;
    }
}