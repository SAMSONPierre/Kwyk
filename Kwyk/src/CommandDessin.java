import javax.swing.JTextField;

abstract class CommandDessin extends Command{//empile les vecteurs
    static Brush brush;
    Vector tmp=new Vector();
    
    
    class CommandDrawLine{
        JTextField distance=new JTextField();//JList
        
        void execute(){//ajout du trait dans le tableau de trait
            Vector.VectorLine trait=tmp.new VectorLine(brush.x, brush.y, brush.angle, Integer.parseInt(distance.getText()), brush.color);
            dessin.add(trait);
        }
    }
    
    
    class CommandDrawArc{//pour plus tard :)
        
    }
    
    //...
}