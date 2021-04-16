import java.io.Serializable;

public class Player implements Serializable{
    final String username, password;//compte par defaut a pour username "default"
    private Level playingLevel;//partie en cours
    boolean[][] currentLevel;
    
    Player(String username, String password){
        this.username=username;
        this.password=password;
        this.playingLevel=null;//pas de partie en cours quand on cree un nouveau joueur
        currentLevel=new boolean[3][11];//niveaux entre 0 et 9, 10e case pour empecher erreur
        currentLevel[0][0]=true;
        currentLevel[1][0]=true;
        currentLevel[2][0]=true;
        unlockAll();
    }
    
    void setLevel(Level level){
        this.playingLevel=level;
    }
    
    Level getLevel(){
        return this.playingLevel;
    }
    
    void unlockAll(){
        if(!username.equals("GM")) return;
        for(int i=0; i<currentLevel.length; i++){
            for(int j=1; j<currentLevel[i].length; j++) currentLevel[i][j]=true;
        }
    }
}