import java.awt.Color;

public class Vector{
    final int x, y;//coordonees de depart
    final int angle;//sur 360 degres (0° si le pinceau est comme ca "->", et ordre trigo)
    final double distance;
    final Color color;//couleur du trait
    
    Vector(int x, int y, int angle, double distance, Color color){
        this.x=x;
        this.y=y;
        this.angle=angle;
        this.distance=distance;
        this.color=color;
    }
    
    boolean sameVector(Vector vector2){//verifier si deux vecteurs font le meme trait
        if(this.distance==vector2.distance && this.color==vector2.color){
            if(this.angle%180==vector2.angle%180){//angle oppose=angle+/-180
                if(this.x==vector2.x && this.y==vector2.y) return true;//meme vecteur
                //si vector2 est oppose a this, alors (vector2.x, vector2.y) est le point d arrivee, donc :
                double d=Math.sqrt(((double)(this.x-vector2.x)*(this.x-vector2.x)+(this.y-vector2.y)*(this.y-vector2.y)));
                return d==this.distance;//vecteur oppose (true) ou different (false)
            }
            return false;
        }
        return false;
    }
}