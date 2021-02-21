import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

public class Control {
    private Model model;
    private View view;
    private LinkedList<String> database=new LinkedList<String>();//pair=username, impair=password
    
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
    
    int checkLogin(String username, String password){//verification des identifiants
        int res=0;
        if(username.equals("default") || database.isEmpty() || !database.contains(username))
            return -1;
        while(true){
            if(database.get(res).equals(username)){
                if(database.get(res+1).equals(password)) return res;//login ok
                return -1;//mauvais password
            }
            res+=2;//les username se trouvent dans les cases pairs
        }
    }
    
    void login(String username, String password){//se connecter
        int index=checkLogin(username, password);//indice d username dans database (-1 sinon)
        if(index!=-1){//identifiants correctes
            String saveFile=database.get(index)+".bin";//extension .bin pour tous les fichiers de sauvegarde
            try{
                File file=new File(saveFile);
                ObjectInputStream ois=new ObjectInputStream(new FileInputStream(file));
                Player player=(Player)ois.readObject();//celui qu on recupere depuis le fichier de sauvegarde
                this.exitFrame();//quitte la fenetre courante
                this.view=new ViewSummaryTraining(player);//pour en ouvrir une autre
                this.model=view.getModel();
                ois.close();
            }
            catch(Exception e) {
                System.out.println("Echec de reprise de sauvegarde");
            }
        }
        else ((ViewLogin)view).errorLogin();//affichage du message d erreur
    }
    
    void createAccount(String username, String password){//creer un compte
        if(database.contains(username)) ((ViewLogin)view).usernameAlreadyExists();
        Player player=new Player(username);//creer un nouveau joueur=nouveau compte
        database.add(username);//ajout dans la database
        database.add(password);
        this.exitFrame();//quitte la fenetre courante
        this.view=new ViewSummaryTraining(player);//pour en ouvrir une autre
        this.model=view.getModel();
        this.save();//on cree le fichier de sauvegarde qui lui est associe
    }
    
    void save(){//sauvegarde du joueur apres chaque niveau reussi
        if(this.model.getPlayer().canlSave){//on ne sauvegarde pas le compte default
            try{
                String saveFile=this.model.getPlayer().username+".bin";
                File file=new File(saveFile);
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(this.model.getPlayer());
                oos.close();
            }
            catch(Exception e){
                System.out.println("Echec de sauvegarde");
            }
        }
    }
    
    void tryWithoutAccount(){//login to default account
        try{
            File file=new File("default.bin");
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(file));
            Player player=(Player)ois.readObject();//celui qu on recupere depuis le fichier de sauvegarde
            this.exitFrame();//quitte la fenetre courante
            this.view=new ViewSummaryTraining(player);//pour en ouvrir une autre
            this.model=view.getModel();
            ois.close();
        }
        catch(Exception e) {
            System.out.println("Echec de reprise de sauvegarde");
        }
    }
    
    /******************
    *   Switch page   *
    ******************/
    
    void switchTraining(){
        
    }
    
    void switchChallenge(){
        
    }
    
    void switchCreate(){
        
    }
    
    void logout(){
        this.save();
        this.exitFrame();
        this.view=new ViewLogin();
        this.model=view.getModel();
    }
}