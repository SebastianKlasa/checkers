
import java.util.Locale;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marek
 */
public class Move {
    public int dirX;
    public int dirY;
    public int startX;
    public int startY;
    public int distance;
    public Pawn pawn;
    public int value;
    
    public Move(int dirX, int dirY, int distance, Pawn pawn, int a){
        this.distance = distance;
        this.dirX = dirX;
        this.dirY = dirY;
        this.pawn = pawn;
        startX=this.pawn.x;
        startY=this.pawn.y;
    }
    
    public int GetFinalX()
    {
        return pawn.x+distance*dirX;
    }
    public int GetFinalY()
    {
        return pawn.y+distance*dirY;
    }
    
    @Override
    public String toString()
    {
        return String.format(Locale.UK, "(%d, %d) -> (%d, %d)", pawn.x+1, pawn.y+1, GetFinalX()+1, GetFinalY()+1);
    }
    public Pawn GetPawn(){
        return pawn;
    }
}
