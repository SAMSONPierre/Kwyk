import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ViewSummaryTraining extends ViewGame{//sommaire des exercices
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    
    ViewSummaryTraining(Player player){
        super(player);
        listeNiveau();
    }
    
    void listeNiveau(){
        JPanel summary=new JPanel();
        summary.setLayout(new FlowLayout());
        summary.setBounds(0,50+buttonHeight,widthFS,heightFS);
        File[] arrayLevels=nombreNiveau("levels/training/");
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length()-4);
                Image img=ImageIO.read(new File("preview/training/"+name+".png"));
                CustomJButton jb=new CustomJButton(name, img,false); // à changer plus tard
                jb.addActionListener((event)->super.control.load("training/"+name));
                jb.setPreferredSize(new Dimension(200, 200));
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(summary);
    }
    
    
}