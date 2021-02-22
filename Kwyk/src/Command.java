import java.util.LinkedList;

abstract class Command{
    String name;//if, else, for, while, ...
    protected static LinkedList<Vector> dessin=new LinkedList<Vector>();//execution du code du joueur -> liste de traits
    
    abstract void execute();
    
    LinkedList getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
        return dessin;
    }
    
    
    class CommandFor extends Command{
        int param;
        Command[]commands;

        CommandFor(int param, Command[]commands){
            this.name="for";
            this.param=param;
            this.commands=commands;
        }

        void execute(){
            for(int i=0; i<this.param; i++) {
                for(Command c:this.commands) c.execute();
            }
        }
    }
    
    
    class CommandIf extends Command{
        
        void execute(){
            
        }
    }
}