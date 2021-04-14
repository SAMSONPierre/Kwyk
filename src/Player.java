import java.io.Serializable;

public class Player implements Serializable {
    final String username;//compte par defaut a pour username "default"
    final String password;
    final boolean canlSave;//on ne sauvegarde pas si compte par defaut
    private Level playingLevel;//partie en cours
    boolean[][] currentLevel;
    
    Player(String username, String password){
        this.username=username;
        this.password = password;
        canlSave=!(username.equals("default"));
        this.playingLevel=null;//pas de partie en cours quand on cree un nouveau joueur
        currentLevel=new boolean[3][8];
        currentLevel[0][0]=true;
        currentLevel[1][0]=true;
        currentLevel[2][0]=true;
    }
    
    void setLevel(Level level){
        this.playingLevel=level;
    }
    
    Level getLevel(){
        return this.playingLevel;
    }
}