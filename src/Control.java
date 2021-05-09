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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public class Control implements Serializable{
    private Model model;
    private View view;
    final static private String secretKey="ssshhhhhhhhhhh!!!!";//gestion des mots de passe
    private Clip clip=null;
    private long clipTime=0;
    private boolean darkMode=false;
    
    Control(){
        try{
            AudioInputStream audio=AudioSystem.getAudioInputStream(new File("sounds/8am.wav"));
            clip=AudioSystem.getClip();
            clip.open(audio);
            musicChangeState();//charge la musique
        }
        catch(Exception e){}
        this.view=new ViewLogin(this);
        view.setVisible(true);
        this.model=null;
    }
    
    protected static void initializeAccount() throws IOException{
        Player[] players={new Player("GM", AES.encrypt("Azozo", secretKey)), 
                new Player("default", AES.encrypt("default", secretKey))};
        for(Player p : players){
            File file=new File("players/"+p.username+".player");
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(p);
            oos.close();
        }
    }
    
    void changeFrame(View tmp){//quand on change de fenetre courante
        tmp.setVisible(true);
        view.dispose();//enleve la vue precedente
        view=tmp;//remplace la vue
        this.model=tmp.getModel();
    }
    
    
    /******************
    *      Music      *
    ******************/
    
    void musicChangeState(){
        if(clip==null) return;//echec de chargement dans constructeur
        if(clip.isRunning()){
            clipTime=(clip.getMicrosecondPosition())%clip.getMicrosecondLength();
            clip.stop();
            return;
        }
        clip.setMicrosecondPosition(clipTime);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    boolean musicIsActive(){
        return clip!=null && clip.isRunning();
    }
    
    
    /******************
    *      Theme      *
    ******************/
    
    boolean darkModeOn(){
    	return this.darkMode;
    }
    
    void themeChangeState(){
    	darkMode=!darkMode;
    	if(darkMode) darkMode();
    	else lightMode();
    }
    
    void lightMode(){
    	UIManager.put("Panel.background", new ColorUIResource(219,252,161));
    	UIManager.put("OptionPane.background", new ColorUIResource(219,252,161));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(Color.BLACK));
    	UIManager.put("ProgressBar.foreground", new ColorUIResource(254, 201, 245));
    	UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.BLACK));
    }
    
    void darkMode(){
    	UIManager.put("Panel.background", new ColorUIResource(Color.DARK_GRAY));
    	UIManager.put("OptionPane.background", new ColorUIResource(Color.DARK_GRAY));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(251, 236, 174));
    	UIManager.put("ProgressBar.foreground", new ColorUIResource(188, 177, 250));
    	UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.BLACK));
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
                if(decryptedPassword.equals(password)) changeFrame(new ViewSummaryTraining(this, p));
                else ((ViewLogin)view).errorLogin();//affichage du message d erreur
                ois.close();
            }
            catch(Exception e){}
        }
    }
    
    void createAccount(String username, String password){//creer un compte
        if(alreadyExists(username)) ((ViewLogin)view).usernameAlreadyExists();//affichage du message d erreur
        else{
            Player p=new Player(username, AES.encrypt(password, secretKey));
            save(p);
            changeFrame(new ViewSummaryTraining(this, p));
        }
    }
    
    void save(Player p){//sauvegarde du joueur apres chaque niveau reussi
        if(!p.username.equals("default")){//on ne sauvegarde pas le compte default
            try{
                File file=new File("players/"+p.username+".player");
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(p);
                oos.close();
            }
            catch(Exception e){
                System.out.println("Fail to save.");
            }
        }
    }
    
    boolean alreadyExists(String username){
    	File file=new File("players/");
    	File[] files=file.listFiles();
    	for(int i=0; i<files.length; i++){
            if(files[i].getName().equals(username+".player")) return true;
    	}
    	return false;
    }
    
    void logout(){
        changeFrame(new ViewLogin(this));
    }
    
    
    /******************
    *   Switch page   *
    ******************/
    
    void switchTraining(String name){
    	changeFrame(new ViewSummaryTraining(this, model.getPlayer(), name));
    }
    
    void switchTraining(){
    	changeFrame(new ViewSummaryTraining(this, model.getPlayer()));
    }
    
    void switchChallenge(){
        changeFrame(new ViewSummaryChallenge(this, model.getPlayer()));
    }
    
    void switchCreate(){
        if(!model.getPlayer().username.equals("default")){
            if(model.getPlayer().username.equals("GM")){
                JDialog popup=new JDialog(new JFrame(), "Brush settings");
                popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JTextField x=new JTextField("200", 4);
                JTextField y=new JTextField("200", 4);
                JTextField angle=new JTextField("0", 4);
                ColorBox color=new ColorBox();
                JButton confirmChoice=new JButton("Confirm");
                popup.getRootPane().setDefaultButton(confirmChoice);//raccourci ENTER
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
                            playLevel(new Level(xval, yval, aval, color.colorRes), true);
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
                view.changeLabelColor(popup, darkMode);
            }
            else playLevel(new Level(), true);
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
    
    
    /*****************
    *   Play Level   *
    *****************/
    
    void playLevel(Level level, boolean isCreating){//quand on appuie sur un bouton pour commencer un niveau
        try{
            this.model.getPlayer().setLevel(level);
            changeFrame(new ViewPlaying(this, model.getPlayer(), isCreating));
        }
        catch(Exception e){}
    }
    
    void win(int directory, String name){
        int lvl=Integer.parseInt(name.substring(0, name.indexOf('-')-1))+1;
        model.getPlayer().unlock(directory, lvl, name);//debloque niveau reussi
        save(model.getPlayer());//sauvegarde apres chaque reussite
    }
    
    
    /*******************
    * Submit new level *
    *******************/
    
    void submit(String name, boolean isT, Level level, String[] mainCode, String[] functions, String dest, int size){
    	ViewPlaying tmp=(ViewPlaying)this.view;
    	LinkedList<Vector> newPattern=level.getSimplifyDraw(level.getPlayerDraw());
    	int[] numbers=tmp.getNumbers();
    	String[] commandsAvailable=tmp.getCommandsArray();
        Rectangle screenRect=new Rectangle(tmp.getX()+tmp.getInsets().left+20,
                tmp.getY()+tmp.getInsets().top+tmp.buttonH*2+20, size, size);
        BufferedImage capture;
        int cpt=((ViewGame)view).nombreNiveau("levels/"+dest).length;
        try{
            capture=new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "png", new File("preview/"+dest+cpt+" - "+name+".png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    	Level newLvl=new Level(level.brushX,level.brushY,level.brushAngle,level.brushFirstColor,numbers[0],
            numbers[1],numbers[2],numbers[3],cpt+" - "+name,isT,commandsAvailable,newPattern,mainCode,functions);
    	try{
            String saveFile="levels/"+dest+cpt+" - "+name+".lvl";
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