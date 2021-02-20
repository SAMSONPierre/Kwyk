import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class ViewPlaying extends View{
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelWhiteBoard whiteBoard;//ecriture du code avec blocs de commandes
    private PanelCommandBoard commandBoard;//blocs de commande disponibles
    
    ViewPlaying(Player player){
        super(player);
        
        //ajout des 3 tableaux, avec des marges de 20 (haut, bas et entre tableaux)
        //taille fixee
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);
        //taille relative a l ecran
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();//plein écran
        int width=(r.width-480)/2;//480=4 marges de colonne + taille blackBoard
        int height=r.height-40-this.getInsets().top;//40=marges haut+bas, getInsets().top=barre supérieur de la fenetre
        whiteBoard=new PanelWhiteBoard(width, height);
        this.add(whiteBoard);
        commandBoard=new PanelCommandBoard(width, height);
        this.add(commandBoard);
        
    }
}
