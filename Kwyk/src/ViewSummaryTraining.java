import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

//--------a faire--------
public class ViewSummaryTraining extends ViewGame{//sommaire des exercices
	final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    
    ViewSummaryTraining(Player player){
        super(player);
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();//plein écran
        this.heightFS=r.height;//getInsets().top=barre supérieur de la fenetre
        this.widthFS=r.width;
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
                CustomJButton jb=new CustomJButton(name, img);
                jb.addActionListener((event)->super.control.load("training/"+name, false));
                jb.setPreferredSize(new Dimension(200, 200));
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(summary);
    }
    
    File[] nombreNiveau(String path){
        File file=new File(path);
        File[] files=file.listFiles();
        return files;
    }
    
    
    class CustomJButton extends JButton{
        private String text;
        private Image image;
        
        CustomJButton(String text, Image image){
            this.text=text;
            this.image=image;
        }
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(image==null) return;
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            if(text!=null){
                g.setColor(Color.WHITE);
                FontMetrics metric=g.getFontMetrics();
                int width=metric.stringWidth(text);
                g.drawString(text, (getWidth()-width)/2, (getHeight()+70)/2);
            }
        }
    }
}