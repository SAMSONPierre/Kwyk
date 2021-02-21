import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanelWhiteBoard extends JPanel{
    PanelWhiteBoard(int width, int height, int heightButt){
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK,3));//bordure noire d'épaisseur 3
        this.setBounds(440, 20+heightButt, width, height);
    }
}