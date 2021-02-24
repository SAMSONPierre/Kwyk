import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

//autres fonctionnalités (boutons) à mettre dans l espace en bas de BlackBoard (x€[20 ; 420] et y€[440+buttonHeight+getInsets().top ; height])
public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;//fusion de WhiteBoard et CommandBoard
    private JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    
    private LinkedList<Vector> dessin=new LinkedList<Vector>();//execution du code du joueur -> liste de traits
    private Brush brush;//pinceau en commun
    
    ViewPlaying(Player player){
        super(player);
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();//plein écran
        this.heightFS=r.height-this.getInsets().top;//getInsets().top=barre supérieur de la fenetre
        this.widthFS=r.width;        
        addBoard();//ajout des 3 tableaux, avec des marges de 20 (haut, bas et entre tableaux)
        addFeatures();//ajout des fonctionnalités
    }
    
    void addBoard(){        
        //taille fixee
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);
        
        //taille relative a l ecran
        int height=heightFS-40-buttonHeight;//40=marges haut+bas
        int width=widthFS-460;//460=3 marges de colonne + taille blackBoard
        dragDrop=new PanelDragDropBoard();
        this.add(dragDrop);
    }
    
    void addFeatures(){
        features.setBounds(20, 440+buttonHeight, 400, heightFS-460-buttonHeight);
        this.add(features);
        
        JButton seeGrid=new JButton("See grid");
        seeGrid.addActionListener((event)->{
            blackBoard.gridApparent=!blackBoard.gridApparent;
            blackBoard.repaint();
        });
        features.add(seeGrid);
    }

    void setBrush(Brush b){
        brush=b;
    }
    
    PanelDragDropBoard.Command getStart(){
        return this.dragDrop.commands.getFirst();
    }

    LinkedList getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
        return dessin;
    }
    
    
    /***********************
    *      BlackBoard      *
    ***********************/
    
    public class PanelBlackBoard extends JPanel{
        protected boolean gridApparent=true;//par defaut, on voit la grille

        PanelBlackBoard(){
            this.setBounds(20, 20+buttonHeight, 400, 400);//marge=20 à gauche, 20+bauteur d un bouton en haut, taille 400*400
            this.setBackground(Color.BLACK);//fond noir
            
            //ajout du pinceau :
            //--------a faire-------
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            if(gridApparent) paintGrid(g);

            //test
            g2.setStroke(new BasicStroke(5));
            g.setColor(Color.red.darker());
            g.drawLine(100, 200, 100, 300);
            g.drawLine(100, 300, 300, 300);
            g.drawLine(300, 300, 300, 200);
            g.drawLine(300, 200, 100, 200);

            g.setColor(Color.red);
            g.drawLine(100, 200, 100, 300);
            g.drawLine(100, 300, 200, 300);
        }

        void paintGrid(Graphics g) {
            g.setColor(Color.gray);
            g.drawLine(100, 0, 100, 400);
            g.drawLine(200, 0, 200, 400);
            g.drawLine(300, 0, 300, 400);
            g.drawLine(0, 100, 400,100 );
            g.drawLine(0, 200, 400,200 );
            g.drawLine(0, 300, 400,300 );
        }
    }
    
    
    /************************
    *     DragDropBoard     *
    ************************/
    
    public class PanelDragDropBoard extends JPanel{
        final int x00, y00, width, height;
        private LinkedList<Command> commands=new LinkedList<Command>();//liste de commandes ayant ete drag sur whiteBoard

        PanelDragDropBoard(){
            this.x00=440;
            this.y00=20+buttonHeight+ViewPlaying.this.getInsets().top;
            height=heightFS-40-buttonHeight;//40=marges haut+bas
            width=widthFS-460;//460=3 marges de colonne + taille blackBoard

            this.setLayout(null);
            this.setBounds(x00, y00-ViewPlaying.this.getInsets().top, width, height);

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
            private boolean isDragging=false;

            Command(String name){
                this.name=name;
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
                this.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
            }

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
                Command toStick=addToList();
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

            Command addToList(){//retourne Command a laquelle on doit coller
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

            boolean inWhiteBoard(){//est dans whiteBoard
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
}
