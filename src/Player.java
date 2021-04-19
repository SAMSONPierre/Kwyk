import java.io.Serializable;
import java.util.LinkedList;

public class Player implements Serializable{
    final String username, password;//compte par defaut a pour username "default"
    private Level playingLevel;//partie en cours
    private boolean[][] currentLevel=new boolean[6][15];//training
    private LinkedList<String> clear=new LinkedList<String>();//challenge
    
    Player(String username, String password){
        this.username=username;
        this.password=password;
        this.playingLevel=null;//pas de partie en cours quand on cree un nouveau joueur
        unlockAll();
    }
    
    void setLevel(Level level){
        this.playingLevel=level;
    }
    
    Level getLevel(){
        return this.playingLevel;
    }
    
    boolean[][] getCurrentLevel(){
        boolean[][] res=new boolean[currentLevel.length][currentLevel[0].length];
        for(int i=0; i<res.length; i++){
            int j=0;
            while(j<currentLevel[0].length && currentLevel[i][j]) res[i][j++]=true;
        }
        return res;
    }
    
    LinkedList<String> getClear(){
        LinkedList<String> res=new LinkedList<String>();
        for(String toAdd : clear) res.add(toAdd);
        return res;
    }
    
    void unlockAll(){
        for(int i=0; i<currentLevel.length; i++) currentLevel[i][0]=true;
        if(username.equals("GM")){
            for(int i=0; i<currentLevel.length; i++){
                for(int j=1; j<currentLevel[i].length; j++) currentLevel[i][j]=true;
            }
        }
    }
    
    void unlock(int directory, int nb, String name){
        if(directory<0){
            if(!clear.contains(name)) clear.add(name);
        }
        else currentLevel[directory][nb]=true;
    }
}