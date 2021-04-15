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
    
    boolean moving(){//override
        return true;
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
        final int x2, y2, angle;//coordonees d arrivee
        
        VectorLine(int x1, int y1, int x2, int y2, int angle, Color color){
            super(x1, y1, color);
            this.x2=x2;
            this.y2=y2;
            this.angle=angle;
        }

        boolean sameVector(Vector vector2){//verifier si deux vecteurs font le meme trait
            if(vector2 instanceof VectorLine){
                VectorLine v2=(VectorLine)vector2;
                boolean same=Math.abs(x1-v2.x1)<2 && Math.abs(y1-v2.y1)<2
                    && Math.abs(x2-v2.x2)<2 && Math.abs(y2-v2.y2)<2;
                boolean reverse=Math.abs(x1-v2.x2)<2 && Math.abs(y1-v2.y2)<2
                    && Math.abs(x2-v2.x1)<2 && Math.abs(y2-v2.y1)<2;
                return same || reverse;//prend en compte arrondis de calcul
            }
            return false;
        }
        
        VectorLine[] toMerge(VectorLine v2){//this=ancien, v2=recent
            if(isAligned(v2)){//this et v2 sont alignes
                Point a=new Point(this.x1, this.y1);
                Point b=new Point(this.x2, this.y2);
                Point c=new Point(v2.x1, v2.y1);
                Point d=new Point(v2.x2, v2.y2);
                double distS=distance(a, b)+distance(c, d);
                Point[] ranked={a, b, c, d};
                ranked=sort(ranked);//ranges de haut en bas/gauche a droite
                if(ranked[1].x==ranked[2].x && ranked[1].y==ranked[2].y && !color.equals(v2.color))//pas de superposition
                    return new VectorLine[0];
                double distG=distance(ranked[0], ranked[3]);
                if(distG<distS || (distG==distS && color.equals(v2.color))){//superposable
                    if(color.equals(v2.color) || ((ranked[1]==a || ranked[1]==b) && (ranked[2]==a || ranked[2]==b))){//ancienne couleur au milieu=recouvrement
                        VectorLine[] v={new VectorLine(ranked[0].x, ranked[0].y, ranked[3].x, ranked[3].y, angle, v2.color)};
                        return v;
                    }
                    //[0]=nouvelle couleur, [1] et [2]=ancienne couleur :
                    if((ranked[1]==c || ranked[1]==d) && (ranked[2]==c || ranked[2]==d)){//nouvelle couleur au milieu
                        VectorLine[] v=new VectorLine[3];
                        v[0]=new VectorLine(ranked[1].x, ranked[1].y, ranked[2].x, ranked[2].y, angle, v2.color);
                        v[1]=new VectorLine(ranked[0].x, ranked[0].y, ranked[1].x, ranked[1].y, angle, color);
                        v[2]=new VectorLine(ranked[2].x, ranked[2].y, ranked[3].x, ranked[3].y, angle, color);
                        return v;
                    }
                    VectorLine[] v=new VectorLine[2];
                    if((ranked[1]==c || ranked[1]==d) && (ranked[3]==c || ranked[3]==d)){//nouvelle superpose ancienne a droite
                        v[0]=new VectorLine(ranked[1].x, ranked[1].y, ranked[3].x, ranked[3].y, angle, v2.color);
                        v[1]=new VectorLine(ranked[0].x, ranked[0].y, ranked[1].x, ranked[1].y, angle, color);
                    }
                    else if((ranked[0]==c || ranked[0]==d) && (ranked[2]==c || ranked[2]==d)){//nouvelle superpose ancienne a gauche
                        v[0]=new VectorLine(ranked[0].x, ranked[0].y, ranked[2].x, ranked[2].y, angle, v2.color);
                        v[1]=new VectorLine(ranked[2].x, ranked[2].y, ranked[3].x, ranked[3].y, angle, color);
                    }
                    if(v[0]!=null) return v;
                }
            }
            return new VectorLine[0];//pas de superposition
        }
        
        boolean isAligned(VectorLine v2){
            if(angle%180==v2.angle%180){
                Point a=new Point(x1, y1);
                Point b=new Point(x2, y2);
                return isAligned(a,b,new Point(v2.x1, v2.y1)) && isAligned(a,b,new Point(v2.x2, v2.y2));
            }
            return false;
        }
        
        boolean isAligned(Point a, Point b, Point c){
            return (b.x-a.x)*(c.y-b.y)-(b.y-a.y)*(c.x-b.x)==0;
        }
        
        double distance(Point a, Point b){
            return Math.sqrt((b.x-a.x)*(b.x-a.x)+(b.y-a.y)*(b.y-a.y));
        }
        
        boolean tooLong() {
        	return(this.x2<-1 || this.x2>401 || this.y2<-1 || this.y2>401);
        }
        
        Point[] sort(Point[] points){//trie de haut en bas/gauche vers droite
            for(int i=1; i<points.length; i++){
                int j=i;
                while(j-1>=0 && comparaison(points[i], points[j-1])) j--;
                Point tmp=points[i];
                for(int k=i; k>j; k--) points[k]=points[k-1];
                points[j]=tmp;
            }
            return points;
        }

        boolean comparaison(Point a, Point b){
            if(b.y>a.y) return true;//de haut en bas, a avant b
            else if(a.y==b.y) return b.x>a.x;//alignes horizontalement -> de gauche a droite
            return false;
        }
        
        boolean moving(){//ne prend pas en compte les traits fixes ou trop petits
            return (x1!=x2 || y1!=y2) && (Math.abs(x1-x2)>1 || Math.abs(y1-y2)>1);
        }
    }
    
    
    class VectorArc extends Vector{//arc
        final int diameter, startAngle, scanAngle;
        
        VectorArc(int x1, int y1, int diameter, int startA, int scanA, Color color){
            super(x1, y1, color);
            this.diameter=diameter;
            if(scanA<0){
                this.startAngle=(startA+scanA+360)%360;
                this.scanAngle=maxAngle(-scanA);
            }
            else{
                this.startAngle=(startA+360)%360;
                this.scanAngle=maxAngle(scanA);
            }
        }
            
        int maxAngle(int n){
            return (n>360)?360:n;//tous les angles sont >0
        }
        
        boolean sameVector(Vector vector2){
            if(vector2 instanceof VectorArc){
                VectorArc v2=(VectorArc)vector2;
                return x1==v2.x1 && y1==v2.y1 && diameter==v2.diameter
                && ((scanAngle>359 && v2.scanAngle>359) || (startAngle==v2.startAngle && scanAngle==v2.scanAngle));
            }
            return false;
        }
        
        VectorArc[] toMerge(VectorArc v2){
            if(diameter==v2.diameter && x1==v2.x1 && y1==v2.y1){//inclus dans le meme carre
                int a1=startAngle;//debut de l angle sur le cercle
                int a2=(startAngle+scanAngle+360)%360;//fin de l angle sur cercle
                int a3=v2.startAngle;
                int a4=(v2.startAngle+v2.scanAngle+360)%360;
                if(v2.scanAngle>359 || inclusion(a3, a1, a2, a4)){//nouveau=cercle entier, ou ancien inclu dans nouveau
                   VectorArc[] v={v2};
                   return v;
                }
                if(inclusion(a1, a3, a4, a2)){//nouveau inclu dans ancien
                    if(!color.equals(v2.color)){
                        VectorArc[] v=new VectorArc[3];//v[1]/v[2] eventuellement !moving() si inclusion non-stricte
                        v[0]=v2;
                        v[1]=new VectorArc(x1, y1, diameter, a1, a3-a1, color);
                        v[2]=new VectorArc(x1, y1, diameter, a3+v2.scanAngle, scanAngle-v[0].scanAngle-v[1].scanAngle, color);
                        return v;
                    }
                    VectorArc[] v={this};
                    return v;
                }
                if(inRange(a3, a1, a4) && inRange(a3, a2, a4)){//forme un cercle entier
                    if(color.equals(v2.color)){
                        VectorArc[] v={new VectorArc(x1, y1, diameter, 10, 360, color)};//au hasard
                        return v;
                    }
                    VectorArc[] v={v2, new VectorArc(x1, y1, diameter, a3, v2.scanAngle-360, color)};
                    return v;
                }
                if((a2==a3 || a1==a4)){//prolongement
                    if(!color.equals(v2.color)) return new VectorArc[0];//pas de changement
                    VectorArc[] v={new VectorArc(x1, y1, diameter, (a2==a3)?a1:a3, scanAngle+v2.scanAngle, color)};
                    return v;
                }
                //un point de chaque inclu dans l autre (intersection) :
                VectorArc[] v=new VectorArc[2];
                v[0]=v2;//le plus recent toujours au dessus des autres
                if(inRange(a1, a4, a2) && inRange(a3, a1, a4))//nouveau a gauche
                    v[1]=new VectorArc(x1, y1, diameter, a4, (a2-a4+360)%360, color);
                else if(inRange(a3, a2, a4) && inRange(a1, a3, a2))//nouveau a droite
                    v[1]=new VectorArc(x1, y1, diameter, a1, (a3-a1+360)%360, color);
                if(v[1]!=null) return v;
            }
            return new VectorArc[0];
        }
        
        boolean inRange(int start, int comp, int end){//start<=comp<=end
            end=(end-start+360)%360;//decalage de start, pour mettre start a 0
            comp=(comp-start+360)%360;
            return 0<=comp && comp<=((end==0)?360:end);
        }
        
        boolean inclusion(int start, int p1, int p2, int end){
            return inRange(start, p1, end) && inRange(start, p2, end) && !inRange(p1, start-1, p2);
        }
        
        boolean moving(){
            return scanAngle>0;
        }
    }
}