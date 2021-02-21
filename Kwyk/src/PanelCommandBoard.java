import java.awt.Color;
import javax.swing.JPanel;

public class PanelCommandBoard extends JPanel{
    PanelCommandBoard(int width, int height, int heightButt){
        this.setBackground(Color.LIGHT_GRAY);//fond gris clair
        this.setBounds(460+width, 20+heightButt, width, height);
    }
}