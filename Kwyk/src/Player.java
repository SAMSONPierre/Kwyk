public class Player {
    final String username;//compte par defaut a pour username "default"
    final boolean canlSave;//on ne sauvegarde pas si compte par defaut
    
    Player(String username){
        this.username=username;
        canlSave=!(username.equals("default"));
    }
}