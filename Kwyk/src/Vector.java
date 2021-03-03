import java.awt.Color;

class Vector{
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
    
    
    //--------a faire--------
    class VectorArc extends Vector{//arc
        final int width, height, startAngle, scanAngle;
        
        //constructeur a modifier
        VectorArc(int x1, int y1, int width, int height, int startA, int scanA, Color color){
            super(x1, y1, color);
            this.width=width;
            this.height=height;
            this.startAngle=startA;
            this.scanAngle=scanA;
        }
        
        boolean sameVector(Vector vector2){
            if(vector2 instanceof VectorArc){
                VectorArc v2=(VectorArc)vector2;
                boolean same=(x1==v2.x1 && y1==v2.y1 && startAngle==v2.startAngle);
                boolean reverse=(startAngle==(v2.startAngle+v2.scanAngle)%360);
                return (same || reverse) && width==v2.width && height==v2.height
                    && scanAngle==v2.scanAngle && color.equals(v2.color);
            }
            //----------a faire----------
            return false;
        }
    }
}