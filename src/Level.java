import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedList;

public class Level implements Serializable{
    final int brushX, brushY, brushAngle;
    final Color brushFirstColor;
    final int numberOfCommands, numberOfFunctions, numberOfVariables;
    final String name;
    final String[] availableCommands;//nom des commandes disponibles
    final String[] mainCode, functions;//code principal a charger
    final LinkedList<Vector> pattern;//patron
    private LinkedList<Vector> playerDraw=new LinkedList<Vector>();
    
    Level(Player p, int x, int y, int angle, Color color, int nbOfC, int nbOfF, int nbOfV,
            String name, String[] nameOfC, LinkedList<Vector> v, String[] mainCode, String[] functions){
        this.brushX=x;
        this.brushY=y;
        this.brushAngle=angle;
        this.brushFirstColor=color;
        this.numberOfCommands=nbOfC;
        this.numberOfFunctions=nbOfF;
        this.numberOfVariables=nbOfV;
        this.name=name;
        this.availableCommands=nameOfC;
        this.pattern=v;
        this.mainCode=mainCode;
        this.functions=functions;
    }
    
    Level(Player p){//pour creer des niveaux
        this.brushX=200;
        this.brushY=200;
        this.brushAngle=0;
        this.brushFirstColor=Color.WHITE;
        this.numberOfCommands=0;
        this.numberOfFunctions=-1;
        this.numberOfVariables=-1;
        this.name="editor";
        String[] c={"for", "if", "while", "drawLine", "drawArc", "raisePutBrush",
            "moveTo", "setAngle", "addAngle", "setColor", "shiftColor", "symmetry"};
        this.availableCommands=c;
        this.pattern=new LinkedList<Vector>();
        this.mainCode=null;
        this.functions=null;
    }
    
    Level(Player p, int x, int y, int angle, Color color, String[] mainCode, String[] functions){//pour construire tutoriel
        this.brushX=x;
        this.brushY=y;
        this.brushAngle=angle;
        this.brushFirstColor=color;
        this.numberOfCommands=0;
        this.numberOfFunctions=-1;
        this.numberOfVariables=-1;
        this.name="GMVersion";
        String[] c={"for", "if", "while", "drawLine", "drawArc", "raisePutBrush",
            "moveTo", "setAngle", "addAngle", "setColor", "shiftColor", "symmetry"};
        this.availableCommands=c;
        this.pattern=new LinkedList<Vector>();
        this.mainCode=mainCode;
        this.functions=functions;
    }
    
    LinkedList<Vector> getPlayerDraw(){
        LinkedList<Vector> res=new LinkedList<Vector>();//proteger l encapsulation
        for(Vector v : this.playerDraw) res.add(v);
        return res;
    }
    
    void addToDraw(Vector vector){
        if(vector.moving()) playerDraw.add(vector);
    }
    
    void initializePlayerDraw(){
        this.playerDraw.removeAll(playerDraw);
    }

    boolean compare(){//a la fin, pour verifier si dessin correct
        if(pattern.isEmpty()) return false;//pas de message de victoire
        playerDraw=getSimplifyDraw(playerDraw);
        for(Vector patternV : pattern){//on prend chaque trait du patron
            boolean found=false;
            for(Vector playerV : playerDraw){//on verifie s il est dessine
                if(patternV.color.equals(playerV.color) && patternV.sameVector(playerV)){
                    found=playerDraw.remove(playerV);//enleve petit a petit les traits
                    break;//on passe au trait suivant
                }
            }
            if(!found) return false;//il manque un trait au moins
        }
        return playerDraw.isEmpty();//si traits restant==rtaits en trop
    }
    
    LinkedList<Vector> getSimplifyDraw(LinkedList<Vector> list){//avant creation ou comparaison avec patron
        LinkedList<Vector> res=new LinkedList<Vector>();
        for(Vector v : list){
            removeSameInList(res, v);//enleve doublon plus vieux, sans regarder la couleur
            res.add(v);
        }
        return mergeVector(res, new LinkedList<Vector[]>());//dessin le plus efficace
    }
    
    void removeSameInList(LinkedList<Vector> list, Vector v){//enleve doublon precedent
        for(Vector vList : list){
            if(v.sameVector(vList)){
                list.remove(vList);
                return;//qu un seul doublon, autres deja traites
            }
        }
    }
    
    LinkedList<Vector> mergeVector(LinkedList<Vector> list, LinkedList<Vector[]> already){
        LinkedList<Vector> res=new LinkedList<Vector>();
        boolean again=false;
        for(int i=0; i<list.size(); i++){//parmi tous les vecteurs dessines
            Vector toAdd=list.get(i);
            int j=res.size()-1;
            while(toAdd!=null && j>=0){//parmi les vecteurs deja traites
                Vector[] v;
                Vector[] combined={res.get(j--), toAdd};
                if(notIn(combined, already)){//combinaison jamais faite encore
                    if(combined[0] instanceof Vector.VectorLine && combined[1] instanceof Vector.VectorLine){
                        Vector.VectorLine v1=(Vector.VectorLine)combined[0];//ancien
                        Vector.VectorLine v2=(Vector.VectorLine)combined[1];//nouveau
                        v=v1.toMerge(v2);
                    }
                    else if(combined[0] instanceof Vector.VectorArc && combined[1] instanceof Vector.VectorArc){
                        Vector.VectorArc v1=(Vector.VectorArc)combined[0];
                        Vector.VectorArc v2=(Vector.VectorArc)combined[1];
                        v=v1.toMerge(v2);
                    }
                    else v=new Vector[0];
                    already.add(combined);
                    if(v.length>0){//fusion a faire
                        res.remove(combined[0]);
                        toAdd=null;
                        for(int k=v.length-1; k>-1; k--){//en [0] se trouve le plus recent
                            if(v[k]!=null && v[k].moving()) again=res.add(v[k]);//vecteur valable
                        }
                    }
                }
            }
            if(toAdd!=null) res.add(toAdd);//dans le cas ou pas de changement
        }
        if(again) res=mergeVector(res, already);//nouvelle possibilite de merge
        return res;
    }
    
    boolean notIn(Vector[] v, LinkedList<Vector[]> t){
        for(Vector[] tmp : t){//teste dans les deux sens
            if(v[0].sameVector(tmp[0]) && v[0].color.equals(tmp[0].color)
            && v[1].sameVector(tmp[1]) && v[1].color.equals(tmp[1].color))
                return false;
            if(v[0].sameVector(tmp[1]) && v[0].color.equals(tmp[1].color)
            && v[1].sameVector(tmp[0]) && v[1].color.equals(tmp[0].color))
                return false;
        }
        return true;
    }
}