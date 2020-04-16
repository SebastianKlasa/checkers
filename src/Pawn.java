/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marek
 */
public class Pawn implements Cloneable{
    public int playerId;
    public int x;
    public int y;
    private boolean isKing;
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    public Pawn(int playerId, int x, int y){
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        isKing = false;
    }
    public Pawn(Pawn p){
        playerId = p.playerId;
        x = p.x;
        y = p.y;
    }

    boolean getIsKing() {
        return isKing;
    }
    void setIsKing(boolean isKing) {
        this.isKing = isKing;
    }
}
