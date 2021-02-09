package Kwyk;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class View extends JFrame{
    private Model model;
    private Control control;
    
    View(Player player){
        this.model=new Model(this, player);
        this.control=new Control(this);
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();//dimension de l ecran
        this.setBounds(0, 0, dim.width, dim.height);//plein ecran
        this.setResizable(false);//on ne pourra pas changer la taille de la fenetre
    }
    
    Model getModel(){
        return this.model;
    }
}