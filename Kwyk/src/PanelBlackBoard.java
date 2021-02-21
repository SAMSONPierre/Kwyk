import java.awt.Color;
import javax.swing.JPanel;

public class PanelBlackBoard extends JPanel{
    PanelBlackBoard(int heightButt){
        this.setBounds(20, 20+heightButt, 400, 400);//marge=20 à gauche, 20+bauteur d un bouton en haut, taille 400*400
        this.setBackground(Color.BLACK);//fond noir
        
    }
}