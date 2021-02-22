import java.awt.Color;

public class Brush{//pinceau
    int x, y;//emplacement du pinceau
    int angle=0;//par defaut, orientee "->" (angle 0°)
    boolean drawing=true;//pinceau posé par defaut
    Color color;
    
    Brush(int x, int y, Color color){
        this.x=x;
        this.y=y;
        this.color=color;
    }
    
    Brush(int x, int y, Color color, int angle){
        this(x, y, color);
        this.angle=angle;
    }
    
    void changeColor(Color color){
        this.color=color;
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
    
    void setPosition(int x, int y){
        this.x=x;
        this.y=y;
    }
}