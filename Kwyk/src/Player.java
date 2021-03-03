public class Player {
    final String username;//compte par defaut a pour username "default"
    final boolean canlSave;//on ne sauvegarde pas si compte par defaut
    private Level playingLevel;//partie en cours
    
    Player(String username){
        this.username=username;
        canlSave=!(username.equals("default"));
        this.playingLevel=null;//pas de partie en cours quand on cree un nouveau joueur
    }
    
    void setLevel(Level level){
        this.playingLevel=level;
    }
    
    Level getLevel(){
        return this.playingLevel;
    }
}