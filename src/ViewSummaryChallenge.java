import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ViewSummaryChallenge extends ViewGame{//sommaire des defis
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    
    ViewSummaryChallenge(Player player){
        super(player);
        listeNiveau();
    }
    
    void listeNiveau(){
        JPanel summary=new JPanel();
        summary.setLayout(new WrapLayout(WrapLayout.CENTER, 50, 50));
        JScrollPane scrollP=new JScrollPane(summary, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollP.setBounds(0,50+buttonHeight,widthFS,heightFS);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);//vitesse de scroll
        File[] arrayLevels=nombreNiveau("levels/challenge/");
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length()-4);
                Image img=ImageIO.read(new File("preview/challenge/"+name+".png"));           
                CustomJButton jb=new CustomJButton(name, img);
                jb.addActionListener((event)->super.control.load("challenge/"+name));
                jb.setPreferredSize(new Dimension(200, 200));
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(scrollP);
    }
}