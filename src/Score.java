/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marek
 */
public class Score implements Cloneable{
    public int WinsCount;
    public int DrawsCount;
    public int CapturesCount;
    public int TotalCapturesCount;
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return (Score)super.clone();
    }
}
