import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicProgressBarUI;

public class ViewGame extends View{//a une barre de controle superieur en plus
    private JButton training, challenge, create, logout;//boutons pour acceder aux autres pages
    private CustomJButton music;
    
    ViewGame(Control control, Player player){
        super(control, player);
        setButton(!player.username.equals("default"));
        setTop(player.getCurrentLevel());
    }
    
    void setButton(boolean canCreate){
        //initialisation + listeners
        training=new JButton("Training");
        training.addActionListener((event)->super.control.switchTraining());
        
        challenge=new JButton("Challenge");
        challenge.addActionListener((event)->super.control.switchChallenge());
        
        create=new JButton("Create");
        create.setEnabled(canCreate);
        create.addActionListener((event)->super.control.switchCreate());
        
        logout=new JButton("Log out");
        logout.addActionListener((event)->super.control.logout());
        
        music=new CustomJButton("", null);
        changeMusicState();//bonne image
        music.setBackground(Color.WHITE);
        music.addActionListener((event)->{
            super.control.musicChangeState();
            changeMusicState();
        });
        
        //largeur des boutons
        Dimension size=challenge.getPreferredSize();//le plus large
        size.width+=size.width/3;
        training.setPreferredSize(size);
        challenge.setPreferredSize(size);
        create.setPreferredSize(size);
        logout.setPreferredSize(size);      
        music.setPreferredSize(new Dimension(getButtonHeight(), getButtonHeight()));
        
        changeButtonColor(this, control.darkModeOn());
    }
    
    private void changeMusicState(){
        try{
            File f=new File("images/"+(control.musicIsActive()?"musicOff":"musicOn")+".png");
            music.addImage(ImageIO.read(f));
        }
        catch(Exception e){}
    }
    
    void setTop(boolean[][] playerBool){
        JPanel topBar=new JPanel(new GridLayout());
        int width=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
        topBar.setBounds(0, 0, width, create.getPreferredSize().height);//hauteur d un bouton
        this.add(topBar);
        
        //panel de gauche
        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));//training, challenge, create
        left.add(training);
        left.add(challenge);
        left.add(create);
        left.add(music);
        topBar.add(left);
        
        //panel de droite
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));//username, progression, bouton logout
        right.add(new JLabel(super.getModel().getPlayer().username));//username
        right.add(new JLabel("       Total progress  "));
        right.add(createProgressBar(playerBool));//barre de progression
        right.add(new JLabel("     "));//separation entre barre de progression et bouton
        right.add(logout);
        topBar.add(right);
    }
    
    private JProgressBar createProgressBar(boolean[][] playerBool) {
    	JProgressBar res=new JProgressBar();
    	res.setStringPainted(true);
    	res.setBackground(Color.black);
    	//UIManager.put("ProgressBar.selectionBackground", Color.black);
    	int nbLvl=0;
        File[] arrayLevels=nombreNiveau("levels/training/");
        for(int i=0; i<arrayLevels.length; i++)
            nbLvl+=nombreNiveau("levels/training/"+arrayLevels[i].getName()).length;
    	if(nbLvl>0) res.setValue(100*nbSucceded(playerBool)/nbLvl);
    	return res;
    }
    
    private int nbSucceded(boolean[][] playerBool) {
    	int res=-playerBool.length;//niveau reussi si le suivant est deverouille
        for(int i=0; i<playerBool.length; i++){
            int j=0;
            while(j<playerBool[i].length && playerBool[i][j++]) res++;
        }
    	return res;
    }
    
    int getButtonHeight(){
        return create.getPreferredSize().height;
    }
    
    File[] nombreNiveau(String path){
        File file=new File(path);
        File[] files=file.listFiles();
        return files;
    }
}