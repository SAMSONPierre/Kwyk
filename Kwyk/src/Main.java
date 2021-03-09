import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;

public class Main {
    public static void main(String[]a){
        //EventQueue.invokeLater(()-> new Control(new ViewLogin()));
        
        Player p=new Player("user");
        Level l=new Level(p, 200, 100, -90, Color.BLUE);//settings en mode game master
        
        //creer
        p.setLevel(l);
        
        EventQueue.invokeLater(()->{
            try{
                new Control(new ViewPlaying(p, true));
            }
            catch(IOException e){
                e.printStackTrace();
            }
        });
    }
}