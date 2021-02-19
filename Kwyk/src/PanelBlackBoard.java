import java.awt.Color;
import javax.swing.JPanel;

public class PanelBlackBoard extends JPanel{
    PanelBlackBoard(){
        this.setBounds(20, 20, 400, 400);//taille 400*400 + marge de 20 avec haut et gauche
        this.setBackground(Color.BLACK);//fond noir
        
    }
}