import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedList;

public class Level implements Serializable{
    private ViewPlaying viewPlaying;//la vue du niveau
    final int brushX, brushY, brushAngle;
    final Color brushFirstColor;
    private int numberOfCommands;
    private String name;
    private String[] availableCommands;//nom des commandes disponibles
    private LinkedList<Vector> pattern=new LinkedList<Vector>();//patron
    private LinkedList<Vector> playerDraw=new LinkedList<Vector>();
    
    Level(Player p, int x, int y, int angle, Color color, int nbOfC, String name, String[] nameOfC, LinkedList<Vector> v){
        //this.viewPlaying=new ViewPlaying(p);//--> level=JButton qui active un ViewPlaying
        this.brushX=x;
        this.brushY=y;
        this.brushAngle=angle;
        this.brushFirstColor=color;
        this.numberOfCommands=nbOfC;
        this.name=name;
        this.availableCommands=nameOfC;
        this.pattern=v;
    }
    
    Level(Player p, int nbOfC, String name, String[] nameOfC, LinkedList<Vector> v){
        //this.viewPlaying=new ViewPlaying(p);//--> level=JButton qui active un ViewPlaying
        this.brushX=200;
        this.brushY=200;
        this.brushAngle=0;
        this.brushFirstColor=Color.WHITE;
        this.numberOfCommands=nbOfC;
        this.name=name;
        this.availableCommands=nameOfC;
        this.pattern=v;
    }
    
    Level(Player p){        
        //this.viewPlaying=new ViewPlaying(p);//--> level=JButton qui active un ViewPlaying
        this.brushX=200;
        this.brushY=200;
        this.brushAngle=0;
        this.brushFirstColor=Color.WHITE;
        this.numberOfCommands=0;
        this.name="editor";
        String[] c={"for", "if", "drawLine", "drawArc", "raisePutBrush", "changeAngle", "changeColor", "moveTo"};
        this.availableCommands=c;
        this.pattern=new LinkedList<Vector>();
    }
    
    String[] getAvailableCommands(){
        return this.availableCommands;
    }
    
    LinkedList<Vector> getPattern(){
        return this.pattern;
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
    
    LinkedList<Vector> getSimplifyPattern(){//pour niveau cree
        LinkedList<Vector> res=new LinkedList<Vector>();
        for(Vector v : this.playerDraw){
            if(notInList(res, v)) res.add(v);
        }
        return res;
    }
    
    boolean notInList(LinkedList<Vector> list, Vector v){
        for(Vector vList : list){
            if(v.sameVector(vList)) return false;
        }
        return true;
    }
}