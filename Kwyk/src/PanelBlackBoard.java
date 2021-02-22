import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class PanelBlackBoard extends JPanel{
    boolean voirGrille=true;
    
    PanelBlackBoard(int heightButt){
        this.setBounds(20, 20+heightButt, 400, 400);//marge=20 à gauche, 20+bauteur d un bouton en haut, taille 400*400
        this.setBackground(Color.BLACK);//fond noir
        
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if(voirGrille) {
            paintGrid(g);
        }
        
        g2.setStroke(new BasicStroke(5));
        g.setColor(Color.red.darker());
        g.drawLine(100, 200, 100, 300);
        g.drawLine(100, 300, 300, 300);
        g.drawLine(300, 300, 300, 200);
        g.drawLine(300, 200, 100, 200);
        
        g.setColor(Color.red);
        g.drawLine(100, 200, 100, 300);
        g.drawLine(100, 300, 200, 300);
        
    }
    
    void paintGrid(Graphics g) {
    	g.setColor(Color.gray);
        g.drawLine(100, 0, 100, 400);
        g.drawLine(200, 0, 200, 400);
        g.drawLine(300, 0, 300, 400);
        g.drawLine(0, 100, 400,100 );
        g.drawLine(0, 200, 400,200 );
        g.drawLine(0, 300, 400,300 );
    }

}