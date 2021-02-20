import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class View extends JFrame{
    private Model model;
    protected Control control;
    
    View(Player player){
        this.model=new Model(this, player);
        this.control=new Control(this);
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //mettre en plein ecran et interdire le changement de taille de la fenetre
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.setBounds(0, 0, r.width, r.height);
        this.setResizable(false);
        
        this.setLayout(null);//on definiera chaque layout individuellement
        this.setTitle("Kwyk");
    }
    
    Model getModel(){
        return this.model;
    }
}