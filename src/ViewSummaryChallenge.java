import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ViewSummaryChallenge extends ViewGame{//sommaire des defis
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    
    ViewSummaryChallenge(Control control, Player player){
        super(control, player);
        listeNiveau(player.getClear());
    }
    
    void listeNiveau(LinkedList<String> clear){
        JPanel summary=new JPanel(new WrapLayout(WrapLayout.CENTER, 50, 50));
        JScrollPane scrollP=new JScrollPane(summary, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollP.setBounds(0,50+buttonHeight,widthFS,heightFS-50-buttonHeight);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);//vitesse de scroll
        File[] arrayLevels=randomFiles();//mise en place aleatoire des challenges
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length()-4);
                Image img=ImageIO.read(new File("preview/challenge/"+name+".png"));
                CustomJButton jb=new CustomJButton(name.substring(name.indexOf('-')+1), img, clear.remove(name));
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
    
    File[] randomFiles(){//sinon niveau 11 avant niveau 2 par exemple
        File[] toChange=nombreNiveau("levels/challenge/");
        for(int i=0; i<toChange.length/2; i++){
            File tmp=toChange[i];
            int random=(int)(Math.random()*(toChange.length));
            toChange[i]=toChange[random];
            toChange[random]=tmp;
        }
        return toChange;
    }
}