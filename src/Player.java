
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marek
 */
public class Player implements Cloneable{
    ArrayList<Pawn> pawns;
    private PlayerType playerType;
    public Score score;
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Player tmp = new Player();
        tmp.setPlayerType(this.playerType);
        tmp.score=score;
        ArrayList<Pawn> newPawns = new ArrayList<>();
        tmp.pawns=newPawns;
        tmp.score=(Score)score.clone();
        return tmp;
    }
    
    public void setPlayerType(PlayerType playerType){
        this.playerType = playerType;
    }
    public PlayerType getPlayerType(){
        return playerType;
    }
    public boolean hasPawnsLeft(){
        return !pawns.isEmpty();
    }
    
    public enum PlayerType{
        Player,
        BotRandomowy,
        BotHeurystycznyEASY,
        BotHeurystycznyMEDIUM,
        BotHeurystycznyPRO,
        BotHeurystycznyEASYprzykrawedzi
    }

    public Player(){
        pawns = new ArrayList<>();
        playerType = PlayerType.Player;
        score = new Score();
    }
    
    public static String[] getPlayerTypes(){
        PlayerType[] values = PlayerType.values();
        String[] names = new String[values.length];
        
        for(int i = 0; i<values.length; i++)
        {
            names[i] = values[i].name();
        }
        return names;
    }
    
    public void Restart(){
        pawns.clear();
    }
    
    public void addPawn(Pawn pawn){
        pawns.add(pawn);
    }
    public void removePawn(Pawn pawn){
        pawns.remove(pawn);
    }
    public ArrayList<Pawn> getPawns(){
        return pawns;
    }
}
