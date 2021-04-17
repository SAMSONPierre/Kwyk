import java.awt.EventQueue;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[]a){
        try{
            String[] path=new String[15];//repertoires a initialiser avant de pouvoir jouer
            String[] tmp={"training/1_Tutorial", "training/2_For", "training/3_If", 
                "training/4_While", "training/5_Variable", "training/6_Function", "challenge"};
            for(int i=0; i<7; i++){
                path[2*i]="levels/"+tmp[i];
                path[2*i+1]="preview/"+tmp[i];
            }
            path[14]="players";
            for(int i=0; i<path.length; i++) Files.createDirectories(Paths.get(path[i]));
            EventQueue.invokeLater(()-> new Control(new ViewLogin()));
        }
        catch(Exception e){
            System.out.println("Failed to create directories.");
        }
    }
}