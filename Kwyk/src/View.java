import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class View extends JFrame{
    private Model model;
    private Control control;
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelWhiteBoard whiteBoard;//ecriture du code avec blocs de commandes
    private PanelCommandBoard commandBoard;//blocs de commande disponibles
    
    View(Player player){
        this.model=new Model(this, player);
        this.control=new Control(this);
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //mettre en plein ecran et interdire le changement de taille de la fenetre
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.setBounds(0, 0, r.width, r.height);
        this.setResizable(false);
        
        //ajout des panels, avec des marges de 20 (haut, bas et entre tableaux)
        this.setLayout(null);
        //taille fixee
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);
        //taille relative a l ecran
        int width=(r.width-480)/2;//480=4 marges de colonne + taille blackBoard
        int height=r.height-40-this.getInsets().top;//40=marges haut+bas, getInsets().top=barre supérieur de la fenetre
        whiteBoard=new PanelWhiteBoard(width, height);
        this.add(whiteBoard);
        commandBoard=new PanelCommandBoard(width, height);
        this.add(commandBoard);
       
    }
    
    Model getModel(){
        return this.model;
    }
}