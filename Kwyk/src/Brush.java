import java.awt.Color;

public class Brush{//pinceau
    private int x, y;//emplacement du pinceau
    private int angle=0;//par defaut, orientee "->" (angle 0°)
    private boolean drawing=true;//pinceau posé par defaut
    private Color color;
    
    Brush(int x, int y, Color color){
        this.x=x;
        this.y=y;
        this.color=color;
    }
    
    Brush(int x, int y, Color color, int angle){
        this(x, y, color);
        this.angle=angle;
    }
    
    
    /*****************
    *     Setter     *
    *****************/
    
    void setPosition(int x, int y){
        this.x=x;
        this.y=y;
    }
    
    void changeAngle(int angle){
        this.angle=angle;
    }
    
    void setNotDrawing(){
        this.drawing=false;
    }
    
    void setDrawing(){
        this.drawing=true;
    }
    
    void changeColor(Color color){
        this.color=color;
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