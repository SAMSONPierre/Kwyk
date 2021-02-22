import java.util.LinkedList;

abstract class Command{
    final String name;//if, else, for, while, ...
    protected static LinkedList<Vector> dessin=new LinkedList<Vector>();//execution du code du joueur -> liste de traits
    
    Command(String name){
        this.name=name;
    }
    
    LinkedList getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
        return dessin;
    }
    
    abstract void execute();//chaque fonction l implemente
    
    
    /*****************
    * Classe interne *
    *****************/
    
    class CommandFor extends Command{//classe interne
        int param;
        Command[]commands;

        CommandFor(int param, Command[]commands){
            super("for");
            this.param=param;
            this.commands=commands;
        }

        void execute(){
            for(int i=0; i<this.param; i++){
                for(Command c:this.commands) c.execute();
            }
        }
    }//fin classe interne For
    
    
    //--------a faire--------
    class CommandIf extends Command{//classe interne
        CommandIf(){
            super("if");
        }
        
        void execute(){
            
        }
    }//fin classe interne If
}