import javax.swing.JList;
import javax.swing.JTextField;

abstract class CommandDessin extends Command{//empile les vecteurs
    static Brush brush;//pinceau en commun
    
    CommandDessin(String name){
        super(name);
    }
    
    
    /*****************
    * Classe interne *
    *****************/
    
    class CommandDrawLine extends CommandDessin{//classe interne
        JTextField distance=new JTextField();//ou JList ? ou bien voir selon les cas ?
        
        CommandDrawLine(){
            super("drawLine");
        }
        
        void execute(){//ajout du trait dans le tableau de trait
            if(brush.getDrawing()){
                Vector tmp=new Vector();
                Vector.VectorLine trait=tmp.new VectorLine(brush.getX(), brush.getY(), brush.getAngle(), Integer.parseInt(distance.getText()), brush.getColor());
                dessin.add(trait);
            }
            //nouvel emplacement du pinceau
            //--------a faire--------
        }
    }//fin classe interne DrawLine
    
    
    //--------a faire--------
    class CommandDrawArc extends CommandDessin{//classe interne
        CommandDrawArc(){
            super("drawArc");
        }
        
        void execute(){
            
        }
    }//fin classe interne DrawArc
    
    
    class CommandRaiseBrush extends CommandDessin{//classe interne
        CommandRaiseBrush(){
            super("raiseBrush");
        }
        
        void execute(){
            brush.setNotDrawing();
        }
    }//fin classe interne RaiseBrush
    
    
    class CommandPutBrush extends CommandDessin{//classe interne
        CommandPutBrush(){
            super("putBrush");
        }
        
        void execute(){
            brush.setDrawing();
        }
    }//fin classe interne PutBrush
    
    
    class CommandChangeAngle extends CommandDessin{//classe interne
        JTextField angle=new JTextField();//meilleur moyen de choisir l angle ? --------a faire--------
        
        CommandChangeAngle(){
            super("changeAngle");
        }
        
        void execute(){
            brush.changeAngle(Integer.parseInt(angle.getText()));
        }
    }//fin de classe interne ChangeAngle
    
    
    class CommandChangeColor extends CommandDessin{//classe interne
        JList color=new JList();//palette de couleur a definir --------a faire--------
        
        CommandChangeColor(){
            super("changeColor");
        }
        
        void execute(){
            //brush.changeColor();
        }
    }//fin de classe interne ChangeAngle
    
    
    //autre classe de dessin...
}