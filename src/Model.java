public class Model {
    private View view;
    private Player player;
    
    Model(View view, Player player){
        this.view=view;
        this.player=player;
    }
    
    void setPlayer(Player player){
        this.player=player;
    }
    
    Player getPlayer(){
        return this.player;
    }
}