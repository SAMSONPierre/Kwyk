import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Control implements Serializable{
    private Model model;
    private View view;
    final private String secretKey="ssshhhhhhhhhhh!!!!";//gestion des mots de passe
    
    Control(View view){
        this.view=view;
        this.model=this.view.getModel();
    }
    
    void exitFrame(){//quand on change de fenetre
        view.setVisible(false);
        view.dispose();
    }
    
    
    /******************
    *      Login      *
    ******************/
    
    void login(String username, String password){//se connecter
        if(!alreadyExists(username)) ((ViewLogin)view).errorLogin();//affichage du message d erreur
        else{
            try{
                FileInputStream fis=new FileInputStream("players/"+username+".player");
                ObjectInputStream ois=new ObjectInputStream(fis);
                Player p=(Player)ois.readObject();
                String decryptedPassword=AES.decrypt(p.password, secretKey);
                if(decryptedPassword.equals(password)){
                    this.exitFrame();//quitte la fenetre courante
                    this.view=new ViewSummaryTraining(p);//pour en ouvrir une autre
                    this.model=view.getModel();
                    ois.close();
                }
                else ((ViewLogin)view).errorLogin();//affichage du message d erreur
            }
            catch(Exception e){}
        }
    }
    
    void createAccount(String username, String password){//creer un compte
        if(alreadyExists(username)) ((ViewLogin)view).usernameAlreadyExists();//affichage du message d erreur
        else{
            Player p=new Player(username, AES.encrypt(password, secretKey));
            try{
                File file=new File("players/"+username+".player");
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(p);
                oos.close();
            }
            catch(Exception e){}
            this.exitFrame();//quitte la fenetre courante
            this.view=new ViewSummaryTraining(p);//pour en ouvrir une autre
            this.model=view.getModel();
        }
    }
    
    void save(){//sauvegarde du joueur apres chaque niveau reussi
        if(!model.getPlayer().username.equals("default")){//on ne sauvegarde pas le compte default
            try{
                String saveFile="players/"+this.model.getPlayer().username+".player";
                File file=new File(saveFile);
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(this.model.getPlayer());
                oos.close();
            }
            catch(Exception e){
                System.out.println("Fail to save.");
            }
        }
    }
    
    void tryWithoutAccount(){//login to default account
        try{
            File file=new File("players/default.player");
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(file));
            Player player=(Player)ois.readObject();//celui qu on recupere depuis le fichier de sauvegarde
            this.exitFrame();//quitte la fenetre courante
            this.view=new ViewSummaryTraining(player);//pour en ouvrir une autre
            this.model=view.getModel();
            ois.close();
        }
        catch(Exception e){}
    }
    
    boolean alreadyExists(String username){
    	File file=new File("players/");
    	File[] files=file.listFiles();
    	for(int i=0; i<files.length; i++){
            if(files[i].getName().equals(username+".player")) return true;
    	}
    	return false;
    }
    
    
    /******************
    *   Switch page   *
    ******************/
    
    void switchTraining(String name){
    	this.exitFrame();
        this.view=new ViewSummaryTraining(this.model.getPlayer(), name);
        this.model=view.getModel();
    }
    
    void switchTraining(){
    	this.exitFrame();
        this.view=new ViewSummaryTraining(this.model.getPlayer());
        this.model=view.getModel();
    }
    
    void switchChallenge(){
        this.exitFrame();
        this.view=new ViewSummaryChallenge(this.model.getPlayer());
        this.model=view.getModel();
    }
    
    void switchCreate() throws IOException{
        if(!model.getPlayer().username.equals("default")){
            if(model.getPlayer().username.equals("GM")){
                JDialog popup=new JDialog(new JFrame(), "Brush settings");
                popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JTextField x=new JTextField("200", 4);
                JTextField y=new JTextField("200", 4);
                JTextField angle=new JTextField("0", 4);
                ColorBox color=new ColorBox();
                JButton confirmChoice=new JButton("Confirm");
                confirmChoice.addActionListener((event)->{
                    try{
                        int xval=Integer.parseInt(x.getText());
                        int yval=Integer.parseInt(y.getText());
                        int aval=Integer.parseInt(angle.getText());
                        boolean ok=true;
                        if(xval>400 || xval<0){
                            ok=false;
                            x.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                        }
                        if(yval>400 || yval<0){
                            ok=false;
                            y.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                        }
                        if(ok){
                            playLevel(new Level(model.getPlayer(),xval,yval,aval,color.colorRes), true);
                            popup.dispose();
                        }
                    }
                    catch(NumberFormatException e){
                        JOptionPane.showMessageDialog(popup, "Only integers are allowed.", "Wrong format", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                GridBagLayout layout=new GridBagLayout();
                layout.columnWidths=new int[] {60, 90, 0};
                layout.rowHeights=new int[] {20, 20, 20, 20};
                JPanel settings=new JPanel(layout);
                GridBagConstraints c=new GridBagConstraints();
                c.fill=GridBagConstraints.BOTH;
                JLabel[] labels=new JLabel[] {new JLabel("x :"), new JLabel("y :"), new JLabel("angle :"), new JLabel("color :")};
                Component[] fields={x, y, angle, color};
                for(int i=0; i<4; i++) addLabelAndTextField(labels[i], i, settings, fields[i], c);
                c.insets=new Insets(20, 25, 0, 50);
                c.gridy=4;
                settings.add(confirmChoice, c);
                popup.add(settings);
                popup.setSize(300, 250);
                popup.setLocationRelativeTo(view);
                popup.setVisible(true);
            }
            else playLevel(new Level(this.model.getPlayer()), true);
        }
    }
    
    private void addLabelAndTextField(JLabel label, int y, JPanel panel, Component textField, GridBagConstraints c){
        c.insets=new Insets(0, 0, 5, 5);
        c.gridx=0;
        c.gridy=y;
        panel.add(label, c);

        c.insets=new Insets(0, 0, 5, 0);
        c.gridx=1;
        panel.add(textField, c);
    }
    
    void logout(){
        this.save();
        this.exitFrame();
        this.view=new ViewLogin();
        this.model=view.getModel();
    }
    
    
    /*****************
    *   Play Level   *
    *****************/
    
    void playLevel(Level level, boolean isCreating){//quand on appuie sur un bouton pour commencer un niveau
        try{
            this.model.getPlayer().setLevel(level);
            this.exitFrame();
            this.view=new ViewPlaying(this.model.getPlayer(), isCreating);
            this.model=view.getModel();
        }
        catch(Exception e){}
    }
    
    
    /*******************
    * Submit new level *
    *******************/
    
    void submit(String name, Level level, String[] mainCode, String[] functions, String dest, int size){
    	ViewPlaying tmp=(ViewPlaying)this.view;
    	LinkedList<Vector> newPattern=level.getSimplifyDraw(level.getPlayerDraw());
    	int[] numbers=tmp.getNumbersFromHead();
    	String[] commandsAvailable=tmp.getCommandsArray();
        Rectangle screenRect=new Rectangle(tmp.getX()+tmp.getInsets().left+20,
                tmp.getY()+tmp.getInsets().top+tmp.buttonH+20, size, size);
        BufferedImage capture;
        File[] arrayLevels=((ViewGame)view).nombreNiveau("levels/"+dest);
        int cpt = arrayLevels.length;
        try{
            capture=new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "png", new File("preview/"+dest+cpt+"- "+name+".png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    	Level newLvl=new Level(model.getPlayer(),level.brushX,level.brushY,level.brushAngle,level.brushFirstColor,
            numbers[0],numbers[1],numbers[2],numbers[3],cpt+"- "+name,commandsAvailable,newPattern,mainCode,functions);
    	try{
            String saveFile="levels/"+dest+cpt+"- "+name+".lvl";
            File file=new File(saveFile);
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(newLvl);
            oos.close();
        }
        catch(Exception e){
            System.out.println("Fail to submit.");
        }
    }
    
    
    /*****************
    *   Load level   *
    *****************/
    
    void load(String name){
    	try{
            FileInputStream fis=new FileInputStream("levels/"+name+".lvl");
            ObjectInputStream ois=new ObjectInputStream(fis);
            Level lvl=(Level)ois.readObject();
            playLevel(lvl, false);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}