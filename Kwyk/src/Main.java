import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.LinkedList;

public class Main {
    public static void main(String[]a){
        //EventQueue.invokeLater(()-> new Control(new ViewLogin()));
        
        //EventQueue.invokeLater(()-> new Control(new ViewPlaying(new Player("test"))));
        
        //Level(Player p, int x, int y, int angle, Color color, String[] c, Vector[] v)
        Player p=new Player("user");
        String[]c={"for", "if", "drawLine", "drawArc", "raisePutBrush", "changeAngle", "changeColor", "moveTo"};
        LinkedList<Vector> v=new LinkedList<Vector>();
        Vector tmp=new Vector();
        v.add(tmp.new VectorLine(100, 200, 100, 300, Color.RED));
        v.add(tmp.new VectorLine(100, 300, 300, 300, Color.RED));
        v.add(tmp.new VectorLine(300, 300, 300, 200, Color.RED));
        v.add(tmp.new VectorLine(300, 200, 100, 200, Color.RED));
        
        Level l=new Level(p, 200, 200, 0, Color.BLUE, 0, null, c, v);
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