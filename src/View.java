import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class View extends JFrame{
    private Model model;
    protected Control control;
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    
    View(Control control, Player player){
        this.model=new Model(this, player);
        this.control=control;
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //mettre en plein ecran et interdire le changement de taille de la fenetre
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.heightFS=r.height-this.getInsets().top;
        this.widthFS=r.width;
        this.setBounds(0, 0, r.width, r.height);
        this.setResizable(false);
        
        this.setLayout(null);//on definiera chaque layout individuellement
        this.setTitle("Kwyk");
    }
    
    Model getModel(){
        return this.model;
    }
}