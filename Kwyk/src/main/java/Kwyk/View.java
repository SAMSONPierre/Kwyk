package Kwyk;

/*import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;*/
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class View extends JFrame{
    private Model model;
    private Control control;
    
    View(Player player){
        this.model=new Model(this, player);
        this.control=new Control(this);
        
        this.setVisible(true);//fen�tre visible � l'affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fen�tre
        
        Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();//dimension de l'�cran
        this.setBounds(0, 0, dim.width, dim.height);//plein �cran
        this.setResizable(false);//on ne pourra pas changer la taille de la fen�tre
        
        //test de taille
        /*JPanel tmp=new JPanel();
        tmp.setBounds(50, 50, 400, 400);
        tmp.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setLayout(null);
        this.add(tmp);*/
    }
    
    View(){//fen�tre sans joueur
        this(null);
    }
    
    Model getModel(){
        return this.model;
    }
}