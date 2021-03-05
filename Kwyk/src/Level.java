import java.awt.Color;
import java.util.LinkedList;

public class Level{
    private ViewPlaying viewPlaying;//la vue du niveau
    final int brushX, brushY, brushAngle;
    final Color brushFirstColor;
    private String[] availableCommands;//nom des commandes disponibles
    private Vector[] pattern;//patron
    private LinkedList<Vector> playerDraw=new LinkedList<Vector>();
    
    Level(Player p, int x, int y, int angle, Color color, String[] nameOfC, Vector[] v){
        //this.viewPlaying=new ViewPlaying(p);//--> level=JButton qui active un ViewPlaying
        this.brushX=x;
        this.brushY=y;
        this.brushAngle=angle;
        this.brushFirstColor=color;
        this.availableCommands=nameOfC;
        this.pattern=v;
    }
    
    String[] getAvailableCommands(){
        return this.availableCommands;
    }
    
    Vector[] getPattern(){
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
}