import java.awt.Color;
import java.awt.EventQueue;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public class Main{
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
            	UIManager.put("Panel.background", new ColorUIResource(219,252,161));
            	UIManager.put("OptionPane.background", new ColorUIResource(219,252,161));
                UIManager.put("OptionPane.messageForeground", new ColorUIResource(Color.BLACK));
            	UIManager.put("ProgressBar.foreground", new ColorUIResource(254, 201, 245));
            	UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.BLACK));
            	UIManager.put("Button.foreground", new Color(251, 236, 174));
            	UIManager.put("Button.background", Color.BLACK);
            	UIManager.put("ComboBox.background", Color.WHITE);
            	new Control();//charge le menu de connexion
            });
        }
        catch(Exception e){
            System.out.println("Failed to start the game.");
        }
    }
}