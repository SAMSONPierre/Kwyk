import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

class Vector implements Serializable{
    final int x1, y1;//coordonees de depart
    final Color color;//couleur du trait
    
    Vector(int x1, int y1, Color color){
        this.x1=x1;
        this.y1=y1;
        this.color=color;
    }
    
    Vector(){//pour initialiser un vecteur temporaire, et creer des vecteurs internes
        this(0, 0, Color.BLACK);
    }
    
    boolean sameVector(Vector vector2){//jamais utilise normalement (override)
        return false;
    }

    Point destinationLine(int x1, int y1, int angle, int distance){//trigo
        angle%=360;
        if(angle>=270){
            angle=360-angle;
            int x2=x1+(int)(distance*Math.cos(Math.toRadians(angle)));
            int y2=y1+(int)(distance*Math.sin(Math.toRadians(angle)));
            return new Point(x2, y2);
        }
        if(angle>=180){
            angle=270-angle;
            int x2=x1-(int)(distance*Math.sin(Math.toRadians(angle)));
            int y2=y1+(int)(distance*Math.cos(Math.toRadians(angle)));
            return new Point(x2, y2);
        }
        if(angle>=90){
            angle=180-angle;
            int x2=x1-(int)(distance*Math.cos(Math.toRadians(angle)));
            int y2=y1-(int)(distance*Math.sin(Math.toRadians(angle)));
            return new Point(x2, y2);
        }
        int x2=x1+(int)(distance*Math.cos(Math.toRadians(angle)));
        int y2=y1-(int)(distance*Math.sin(Math.toRadians(angle)));
        return new Point(x2, y2);
    }
    
    
    /*****************
    * Classe interne *
    *****************/
    
    class VectorLine extends Vector{//trait droit
        final int x2, y2;//coordonees d arrivee
        
        VectorLine(int x1, int y1, int x2, int y2, Color color){
            super(x1, y1, color);
            this.x2=x2;
            this.y2=y2;
        }

        boolean sameVector(Vector vector2){//verifier si deux vecteurs font le meme trait
            if(vector2 instanceof VectorLine){
                VectorLine v2=(VectorLine)vector2;
                boolean same=(x1==v2.x1 && y1==v2.y1 && x2==v2.x2 && y2==v2.y2);
                boolean reverse=(x1==v2.x2 && y1==v2.y2 && x2==v2.x1 && y2==v2.y1);
                return (same || reverse) && color.equals(v2.color);
            }
            return false;
        }
    }
    
    
    class VectorArc extends Vector{//arc
        final int diameter, startAngle, scanAngle;
        
        VectorArc(int x1, int y1, int diameter, int startA, int scanA, Color color){
            super(x1, y1, color);
            this.diameter=diameter;
            this.startAngle=startA;
            this.scanAngle=scanA;
        }
        
        boolean sameVector(Vector vector2){
            if(vector2 instanceof VectorArc){
                VectorArc v2=(VectorArc)vector2;
                boolean sameAngle=startAngle==v2.startAngle
                    || startAngle==(v2.startAngle+v2.scanAngle)%360;
                return sameAngle && x1==v2.x1 && y1==v2.y1 && diameter==v2.diameter
                    && scanAngle==v2.scanAngle && color.equals(v2.color);
            }
            return false;
        }
    }
}