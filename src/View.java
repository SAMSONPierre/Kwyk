import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public class View extends JFrame{
    private Model model;
    protected Control control;
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    private CustomJButton colorMode;
    
    View(Control control, Player player){
        this.model=new Model(this, player);
        this.control=control;
        
        this.setVisible(true);//fenetre visible a l affichage
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //mettre en plein ecran et interdire le changement de taille de la fenetre
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.heightFS=r.height-this.getInsets().top;
        this.widthFS=r.width;
        this.setBounds(0, 0, r.width, r.height);
        this.setResizable(false);
        
        this.setLayout(null);//on definiera chaque layout individuellement
        this.setTitle("Kwyk");
        
        colorMode=new CustomJButton("", null);
        changeColorState();
        colorMode.addActionListener((event)->{
        	control.themeChangeState();
        	changeColorState();
        	changeButtonColor(this, control.darkModeOn());
        	SwingUtilities.updateComponentTreeUI(this);
        });
        colorMode.setBounds(0, heightFS-this.getInsets().bottom-35, 60, 35);
        this.add(colorMode);
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
            if(c instanceof Container){
                if(c instanceof JButton){
                    ((JButton)c).setBackground(dark?Color.BLACK:Color.DARK_GRAY);
                    ((JButton)c).setForeground(dark?new Color(251, 236, 174):Color.WHITE);
                }
                changeButtonColor((Container)c, dark);
            }
        }
    }
    /*
    void lightMode(){
    	UIManager.put("Panel.background", new ColorUIResource(219,252,161));
    	UIManager.put("OptionPane.background", new ColorUIResource(219,252,161)); 
    	UIManager.put("ProgressBar.foreground", new ColorUIResource(254, 201, 245));
    	UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.black));
    	UIManager.put("Label.foreground", new ColorUIResource(Color.BLACK));
    	SwingUtilities.updateComponentTreeUI(this);
    }
    
    void darkMode(){
    	UIManager.put("Panel.background", new ColorUIResource(Color.DARK_GRAY));
    	UIManager.put("OptionPane.background", new ColorUIResource(Color.DARK_GRAY));
    	UIManager.put("ProgressBar.foreground", new ColorUIResource(188, 177, 250));
    	UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.black));
    	UIManager.put("Label.foreground", new ColorUIResource(251, 236, 174));
    	SwingUtilities.updateComponentTreeUI(this);
    }
    */
    Model getModel(){
        return this.model;
    }
}