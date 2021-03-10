import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedList;

public class Level implements Serializable{
    final int brushX, brushY, brushAngle;
    final Color brushFirstColor;
    final int numberOfCommands;
    final int numberOfFunctions;
    final int numberOfVariables;
    final String name;
    final String[] availableCommands;//nom des commandes disponibles
    private LinkedList<Vector> pattern=new LinkedList<Vector>();//patron
    private LinkedList<Vector> playerDraw=new LinkedList<Vector>();
    
    Level(Player p, int x, int y, int angle, Color color, int nbOfC, int nbOfF, int nbOfV,
            String name, String[] nameOfC, LinkedList<Vector> v){
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
        String[] c={"for", "if", "while", "drawLine", "drawArc", "raisePutBrush", "moveTo",
            "setAngle", "addAngle", "setColor", "shiftColor"};
        this.availableCommands=c;
        this.pattern=new LinkedList<Vector>();
    }
    
    Level(Player p, int x, int y, int angle, Color color){//pour construire tutoriel
        this.brushX=x;
        this.brushY=y;
        this.brushAngle=angle;
        this.brushFirstColor=color;
        this.numberOfCommands=0;
        this.numberOfFunctions=-1;
        this.numberOfVariables=-1;
        this.name="GMVersion";
        String[] c={"for", "if", "while", "drawLine", "drawArc", "raisePutBrush", "moveTo",
            "setAngle", "addAngle", "setColor", "shiftColor"};
        this.availableCommands=c;
        this.pattern=new LinkedList<Vector>();
    }
    
    String[] getAvailableCommands(){
        return this.availableCommands;
    }
    
    LinkedList<Vector> getPattern(){
        return this.pattern;
    }
    
    LinkedList<Vector> getSimplifyPattern(){//pour un niveau cree
        LinkedList<Vector> res=new LinkedList<Vector>();
        for(Vector v : this.playerDraw){
            if(notInList(res, v)){
                res.add(v);
            }
        }
        return res;
    }
    
    boolean notInList(LinkedList<Vector> list, Vector v){
        for(Vector vList : list){
            if(v.sameVector(vList)) return false;
        }
        return true;
    }
    
    LinkedList<Vector> getPlayerDraw(){
        return this.playerDraw;
    }
    
    void addToDraw(Vector vector){
        this.playerDraw.add(vector);
    }
    
    void initializePlayerDraw(){
        this.playerDraw.removeAll(playerDraw);
    }
    
    boolean compare(){//a la fin, pour verifier si dessin correct
        if(pattern.size()==0) return false;//pas de message de victoire
        for(Vector patternV : this.pattern){//on prend chaque trait du patron
            boolean found=false;
            for(Vector playerV : this.playerDraw){//on verifie s il est dessine
                if(patternV.sameVector(playerV)){
                    found=true;
                    this.playerDraw.remove(playerV);//enleve petit a petit les traits
                    break;//on sort du for
                }
            }
            if(!found) return false;//il manque un trait au moins
        }
        return this.playerDraw.isEmpty();//si traits restant==rtaits en trop
    }
}