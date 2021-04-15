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

public class Control implements Serializable{
    private Model model;
    private View view;
    final String secretKey="ssshhhhhhhhhhh!!!!";//gestion des mots de passe
    
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
        if(this.model.getPlayer().canlSave){//on ne sauvegarde pas le compte default
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
            this.exitFrame();
            Level level=new Level(this.model.getPlayer());
            playLevel(level, true);
        }
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
    
    void playLevel(Level level, boolean isCreating) throws IOException{//quand on appuie sur un bouton pour commencer un niveau
        this.model.getPlayer().setLevel(level);
        this.exitFrame();
        this.view=new ViewPlaying(this.model.getPlayer(), isCreating);
        this.model=view.getModel();
    }
    
    
    /*******************
    * Submit new level *
    *******************/
    
    void submit(String name, Level level, String[] mainCode, String[] functions){
        if(name==null) return;
    	ViewPlaying tmp=(ViewPlaying)this.view;
    	LinkedList<Vector> newPattern=level.getSimplifyDraw(level.getPlayerDraw());
    	int[] numbers=tmp.getNumbersFromHead();
    	String[] commandsAvailable=tmp.getCommandsArray();
        Rectangle screenRect=new Rectangle(tmp.getX()+tmp.getInsets().left+20,
                tmp.getY()+tmp.getInsets().top+tmp.buttonHeight+20, 400, 400);
        BufferedImage capture;
        File[] arrayLevels=((ViewGame)view).nombreNiveau("levels/training/1Tutoriel/");
        int cpt = arrayLevels.length;
        try{
            capture=new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "png", new File("preview/training/1Tutoriel/"+cpt+"- "+name+".png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    	Level newLvl=new Level(model.getPlayer(),level.brushX,level.brushY,level.brushAngle,level.brushFirstColor,
            numbers[0],numbers[1],numbers[2],numbers[3],cpt+"- "+name,commandsAvailable,newPattern,mainCode,functions);
    	try{
            String saveFile="levels/training/1Tutoriel/"+cpt+"- "+name+".lvl";
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