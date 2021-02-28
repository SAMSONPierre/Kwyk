import java.awt.Color;

public class Brush{//pinceau
    private int x, y;//emplacement du pinceau
    private int angle=0;//par defaut, orientee "->" (angle 0°)
    private Color color;
    private boolean drawing=true;//pinceau posé par defaut
    
    Brush(int x, int y, int angle, Color color){
        this.x=x;
        this.y=y;
        this.angle=angle;
        this.color=color;
    }
    
    
    /*****************
    *     Setter     *
    *****************/
    
    void changePosition(int x, int y){
        this.x=x;
        this.y=y;
    }
    
    void changeAngle(int angle){
        this.angle=angle;
    }
    
    void changeColor(Color color){
        this.color=color;
    }
    
    void changeNotDrawing(){
        this.drawing=false;
    }
    
    void changeDrawing(){
        this.drawing=true;
    }
    
    
    /*****************
    *     Getter     *
    *****************/
    
    int getX(){
        return this.x;
    }
    
    int getY(){
        return this.y;
    }
    
    int getAngle(){
        return this.angle;
    }
    
    boolean getDrawing(){
        return this.drawing;
    }
    
    Color getColor(){
        return this.color;
    }
}