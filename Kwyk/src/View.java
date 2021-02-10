import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

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
        
        
        JPanel test=new Dessin();
        test.setBorder(BorderFactory.createLineBorder(Color.black,5));
        test.setBounds(10, 20, 400, 400);        
        this.setLayout(null);
        this.add(test);
    }
    class Dessin extends JPanel{
        public void paintComponent(Graphics g){
            //super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            //BasicStroke bs=new BasicStroke(5);
            //g2.setStroke(bs);
            g2.setColor(Color.red);
            g2.drawLine(100, 50, 100, 150);
            g2.drawLine(300, 50, 300, 150);
            g2.drawArc(80, 200, 250, 150, 180, 180);
        }
    }
    
    Model getModel(){
        return this.model;
    }
}