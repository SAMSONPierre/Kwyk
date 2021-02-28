import java.awt.Color;
import java.awt.EventQueue;

public class Main {
    public static void main(String[]a){
        //EventQueue.invokeLater(()-> new Control(new ViewLogin()));
        
        //EventQueue.invokeLater(()-> new Control(new ViewPlaying(new Player("test"))));
        
        //Level(Player p, int x, int y, int angle, Color color, String[] c, Vector[] v)
        Player p=new Player("user");
        String[]c={"for", "for", "if", "drawLine"};
        Level l=new Level(p, 0, 0, 0, Color.BLUE, c, null);
        p.setLevel(l);
        EventQueue.invokeLater(()-> new Control(new ViewPlaying(p)));
    }
}