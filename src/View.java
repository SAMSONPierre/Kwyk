import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class View extends JFrame{
    private Model model;
    protected Control control;
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    private CustomJButton music=new CustomJButton("", null);
    private CustomJButton colorMode=new CustomJButton("", null);
    
    View(Control control, Player player){
        this.model=new Model(this, player);
        this.control=control;
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //mettre en plein ecran et interdire le changement de taille de la fenetre
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.heightFS=r.height-this.getInsets().top-35;
        this.widthFS=r.width;
        this.setBounds(0, 0, r.width, r.height);
        this.setResizable(false);
        
        this.setLayout(null);//on definiera chaque layout individuellement
        this.setTitle("Kwyk");
        
        changeMusicState();//bonne image
        music.setOpaque(false);
        music.addActionListener((event)->{
            control.musicChangeState();
            changeMusicState();
        });
        music.setBounds(75, heightFS, 35, 35);
        this.add(music);
        
        changeColorState();
        colorMode.addActionListener((event)->{
            control.themeChangeState();
            changeColorState();
            changeButtonColor(this, control.darkModeOn());
            changeLabelColor(this, control.darkModeOn());
            SwingUtilities.updateComponentTreeUI(this);
        });
        colorMode.setBounds(10, heightFS, 60, 35);
        this.add(colorMode);
    }
    
    private void changeMusicState(){
        try{
            File f=new File("images/"+(control.musicIsActive()?"musicOff":"musicOn")+".png");
            music.addImage(ImageIO.read(f));
        }
        catch(Exception e){e.printStackTrace();}
    }
    
    private void changeColorState(){
        try{
            File f=new File("images/"+(control.darkModeOn()?"light":"dark")+".png");//on affiche "LIGHT" quand on est en dark mode
            colorMode.addImage(ImageIO.read(f));
        }
        catch(Exception e){}
    }
    
    void changeButtonColor(Container parent, boolean dark){
        for(Component c : parent.getComponents()){
            if(c instanceof JComboBox){}
            else if(c instanceof Container){
                if(c instanceof JButton){
                    ((JButton)c).setBackground(dark?Color.BLACK:Color.DARK_GRAY);
                    ((JButton)c).setForeground(dark?new Color(251, 236, 174):Color.WHITE);
                }
                changeButtonColor((Container)c, dark);
            }
        }
    }
    
    void changeLabelColor(Container parent, boolean dark){
        for(Component c : parent.getComponents()){
            if(c instanceof Container && !(c instanceof ViewPlaying.PanelDragDropBoard) && !(c instanceof VariablePanel)){
                if(c.getClass()==JLabel.class && !((JLabel)c).getForeground().equals(Color.RED))
                    ((JLabel)c).setForeground(dark?new Color(251, 236, 174):Color.BLACK);
                changeLabelColor((Container)c, dark);
            }
        }
    }

    Model getModel(){
        return this.model;
    }
}