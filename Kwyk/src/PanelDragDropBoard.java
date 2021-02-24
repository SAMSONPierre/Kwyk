import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

public class PanelDragDropBoard extends JPanel{
    private static LinkedList<Command> commands=new LinkedList<Command>();//liste de commandes ayant ete drag sur whiteBoard
    final int x00, y00, width, height;
    protected static LinkedList<Vector> dessin=new LinkedList<Vector>();//execution du code du joueur -> liste de traits
    static Brush brush;//pinceau en commun
    
    PanelDragDropBoard(int width, int height, int buttonHeight, int top){
        this.x00=440;
        this.y00=20+buttonHeight+top;
        this.width=width;
        this.height=height;
        
        this.setLayout(null);
        this.setBounds(x00, y00-top, width, height);
        
        CommandStart start=new CommandStart(20, 20);
        commands.add(start);
        this.add(start);
        
        //test
        CommandFor forC=new CommandFor(width/2+20, 20);
        this.add(forC);
    }
    
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        //WhiteBoard a droite
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width/2, height);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(1, 1, width-3, height-3);//contour du whiteBoard
        
        //CommandBoard a gauche
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(width, 0, width/2, height);
    }
    
    
    /******************
    *  Command class  *
    ******************/
    
    abstract class Command extends JPanel implements MouseInputListener{
        final String name;//if, else, for, while, ...
        private Command next;
        private int mouseX, mouseY;
        boolean isDragging=false;

        Command(String name){
            this.name=name;
            this.addMouseMotionListener(this);
            this.addMouseListener(this);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
        }

        /*LinkedList getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
            return dessin;
        }*/

        abstract void execute();//chaque fonction l implemente

        public void mouseDragged(MouseEvent e){            
            if(!isDragging){
                mouseX=e.getX();
                mouseY=e.getY();
                isDragging=true;
            }
            int x=e.getXOnScreen()-x00-mouseX;    
            int y=e.getYOnScreen()-y00-mouseY;
            this.setLocation(x, y);
        }
        
        public void mouseReleased(MouseEvent e){
            isDragging=false;
            unStick();
            Command toStick=addToCommands();
            if(toStick!=null){//accroche deux blocs
                this.setLocation(toStick.getLocation().x, toStick.getLocation().y+toStick.getPreferredSize().height);
            }
            System.out.println(commands.size());
            System.out.println(commands.getFirst().next);
        }
        
        public void mouseClicked(MouseEvent e){}
        public void mouseMoved(MouseEvent e){}
        public void mouseEntered(MouseEvent e){}
        public void mousePressed(MouseEvent e){}
        public void mouseExited(MouseEvent e){}
        
        /*****************
        * Stick together *
        *****************/
        
        Command addToCommands(){//true si ajout au next d une Command
            if(this.inWhiteBoard() && !commands.contains(this)) commands.add(this);//dans la liste des commandes
            if(this.isNotNext()){
                int stickTo=closeCommand();//cherche apres qui se positionner
                if(stickTo!=-1){
                    Command c=commands.get(stickTo);
                    if(c.next==null) c.next=this;//place suivante libre
                    else{//deplacer le suivant
                        this.next=c.next;
                        c.next=this;
                    }
                    return c;
                }
            }
            return null;
        }
        
        boolean inWhiteBoard(){//est sur whiteBoard
            Point p=this.getLocation();
            return (p.x>0 && p.x<width/2 && p.y>0 && p.y<height);
        }
        
        boolean isNotNext(){//n est le suivant de personne, donc peut-etre colle a un bloc
            for(Command c : commands){
                if(c.next==this) return false;
            }
            return true;
        }
        
        int closeCommand(){//this et c sont assez proches pour se coller
            int i=0;
            for(Command c : commands){
                if(this.closeHeight(c) && this.closeWidth(c)) return i;
                i++;
            }
            return -1;
        }
        
        boolean closeHeight(Command c){//distance entre bas de c et haut de this
            int distance=this.getLocation().y-c.getLocation().y-c.getHeight();
            return distance>0 && distance<10;
        }
        
        boolean closeWidth(Command c){//distance entre cote gauche de this et celui de c
            int distance=this.getLocation().x-c.getLocation().x;
            return distance>-5 && distance<10;
        }
        
        
        /****************
        *    Unstick    *
        ****************/
        
        void unStick(){//enleve this du next du bloc auquel il etait colle
            if(!isNotNext() && closeCommand()==-1){//suivant d un bloc mais se detache
                whoseNext().next=null;
            }
        }
        
        Command whoseNext(){//cherche Command qui contient this comme next
            for(Command c : commands){
                if(c.next==this) return c;
            }
            return null;//a priori jamais 
        }
    }
    
    
    class CommandStart extends Command{//bloc initial present que sur WhiteBoard
        CommandStart(int x, int y){
            super("start");
            this.add(new JLabel("Start your code here !"));
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
            this.setBackground(Color.GREEN.darker());
        }

        void execute(){
            super.next.execute();
        }
    }//fin classe interne Start
    
    
    class CommandFor extends Command{//classe interne
        protected JTextField index;//nombre de repetition a saisir
        protected Command[]commands;//commandes dans for, s initialise apres avoir run le code

        CommandFor(int x, int y){
            super("for");
            this.add(new JLabel("Repeat "));
            this.index=new JTextField("0");//par defaut a 0, c est le joueur qui choisit
            this.add(index);
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
            //this.setBackground();
        }

        void execute(){
            for(int i=0; i<Integer.parseInt(index.getText()); i++){
                for(Command c:this.commands) c.execute();
            }
        }
    }//fin classe interne For
    
    
    //--------a faire--------
    class CommandIf extends Command{//classe interne
        CommandIf(int x, int y){
            super("if");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }
        
        void execute(){
            
        }
    }//fin classe interne If

    
    /*****************
    *   Draw class   *
    *****************/
    
    class CommandDrawLine extends Command{//classe interne
        JTextField distance=new JTextField();//ou JList ? ou bien voir selon les cas ?

        CommandDrawLine(int x, int y){
            super("drawLine");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
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
    class CommandDrawArc extends Command{//classe interne
        CommandDrawArc(int x, int y){
            super("drawArc");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }

        void execute(){

        }
    }//fin classe interne DrawArc


    class CommandRaiseBrush extends Command{//classe interne
        CommandRaiseBrush(int x, int y){
            super("raiseBrush");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }

        void execute(){
            brush.setNotDrawing();
        }
    }//fin classe interne RaiseBrush


    class CommandPutBrush extends Command{//classe interne
        CommandPutBrush(int x, int y){
            super("putBrush");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }

        void execute(){
            brush.setDrawing();
        }
    }//fin classe interne PutBrush


    class CommandChangeAngle extends Command{//classe interne
        JTextField angle=new JTextField();//meilleur moyen de choisir l angle ? --------a faire--------

        CommandChangeAngle(int x, int y){
            super("changeAngle");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }

        void execute(){
            brush.changeAngle(Integer.parseInt(angle.getText()));
        }
    }//fin de classe interne ChangeAngle


    class CommandChangeColor extends Command{//classe interne
        JList color=new JList();//palette de couleur a definir --------a faire--------

        CommandChangeColor(int x, int y){
            super("changeColor");
            this.setBounds(x, y, this.getPreferredSize().width, this.getPreferredSize().height);
        }

        void execute(){
            //brush.changeColor();
        }
    }//fin de classe interne ChangeAngle


    //autre classe de dessin...
}