import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ViewSummaryChallenge extends ViewGame{//sommaire des defis
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    
    ViewSummaryChallenge(Player player){
        super(player);
        //listeNiveau();
        sommaire();
    }
    ViewSummaryChallenge(Player player,String path){
    	super(player);
    	listeNiveau(path);
    }
    
    void sommaire() {
    	JPanel summary=new JPanel();
        summary.setLayout(new FlowLayout());
        summary.setBounds(0,50+buttonHeight,widthFS,heightFS);
        File[] arrayLevels=nombreNiveau("levels/challenge/");
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length())+"/";          
                JButton jb=new JButton(name);
                jb.addActionListener((event)->{
                	this.control.switchChallenge(name);
                });
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(summary);
    }
    
    void listeNiveau(String path){
        JPanel summary=new JPanel();
        summary.setLayout(new FlowLayout());
        summary.setBounds(0,50+buttonHeight,widthFS,heightFS);
        int directory = Integer.parseInt(path.charAt(0)+"")-1;
        File[] arrayLevels=nombreNiveau("levels/challenge/"+path);
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length()-4);
                Image img=ImageIO.read(new File("preview/challenge/"+path+name+".png"));
                CustomJButton jb=new CustomJButton(name, img,super.getModel().getPlayer().currentLevel[directory][i+1]);
                jb.setEnabled(super.getModel().getPlayer().currentLevel[directory][i]);
                jb.addActionListener((event)->{
                super.control.load("challenge/"+path+name);}
                );
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