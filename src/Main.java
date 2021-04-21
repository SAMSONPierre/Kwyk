import java.awt.EventQueue;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[]a){
        try{
            //repertoires a initialiser avant de pouvoir jouer, quitte a ne pas avoir de niveaux
            String[] path=new String[15];
            String[] tmp={"training/1_Tutorial", "training/2_For", "training/3_If", 
                "training/4_While", "training/5_Variable", "training/6_Function", "challenge"};
            for(int i=0; i<7; i++){
                path[2*i]="levels/"+tmp[i];
                path[2*i+1]="preview/"+tmp[i];
            }
            path[14]="players";
            for(int i=0; i<path.length; i++) Files.createDirectories(Paths.get(path[i]));
            
            //comptes a ajouter des le debut
            Control.initializeAccount();
            
            //on lance le jeu
            EventQueue.invokeLater(()->{
                Control control=new Control();//charge le menu de connexion
                //control.musicChangeState();//charge la musique
            });
        }
        catch(Exception e){
            System.out.println("Failed to start the game.");
        }
    }
}