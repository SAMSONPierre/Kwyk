import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViewGame extends View{//a une barre de controle superieur en plus
    private JButton training, challenge, create, logout;//boutons pour acceder aux autres pages
    
    ViewGame(Player player){
        super(player);
        setButton();
        setTop();
    }
    
    void setButton(){
        //initialisation + listeners
        training=new JButton("Training");
        training.addActionListener((event)->super.control.switchTraining());
        challenge=new JButton("Challenge");
        challenge.addActionListener((event)->super.control.switchChallenge());
        create=new JButton("Create");
        create.addActionListener((event)->{
			try {
				super.control.switchCreate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
        logout=new JButton("Log out");
        logout.addActionListener((event)->super.control.logout());
        
        //largeur des boutons
        Dimension size=challenge.getPreferredSize();
        size.width+=size.width/3;
        training.setPreferredSize(size);
        challenge.setPreferredSize(size);
        create.setPreferredSize(size);
        logout.setPreferredSize(size);        
    }    
    
    void setTop(){
        JPanel topBar=new JPanel();
        topBar.setLayout(new GridLayout());
        int width=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
        topBar.setBounds(0, 0, width, create.getPreferredSize().height);//hauteur d un bouton
        this.add(topBar);
        
        //pannel de gauche
        JPanel left=new JPanel();//training, challenge, create
        left.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.add(training);
        left.add(challenge);
        left.add(create);
        topBar.add(left);
        
        //panel de droite
        JPanel right=new JPanel();//username, progression, bouton logout
        right.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(new JLabel(super.getModel().getPlayer().username));
        right.add(new JLabel("     "));//separation entre username et barre de progression
        /* ... barre de progression a ajouter plus tard */
        right.add(new JLabel("     "));//separation entre barre de progression et bouton
        right.add(logout);
        topBar.add(right);
    }
    
    int getButtonHeight(){
        return create.getPreferredSize().height;
    }
}
