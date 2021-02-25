import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

//autres fonctionnalites (boutons) à mettre dans l espace en bas de BlackBoard (x€[20 ; 420] et y€[440+buttonHeight+getInsets().top ; height])
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

    LinkedList<Vector> getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
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

            CommandStart start=new CommandStart();
            commands.add(start);
            this.add(start);

            //test
            CommandFor forC=new CommandFor(width/2+20, 20);
            this.add(forC);
            CommandFor forC2=new CommandFor(width/2+20, 100);
            this.add(forC2);
            CommandDrawLine draw=new CommandDrawLine(width/2+20, 60);
            this.add(draw);
            CommandIf ifC=new CommandIf(width/2+20, 140);
            this.add(ifC);
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
            final int defaultH=35;//hauteur par defaut d un bouton
            private Command next;
            private int mouseX, mouseY;
            private boolean isDragging=false;
            private boolean brighter=false;

            Command(String name){
                this.name=name;
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
                this.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
            }

            abstract void execute();//chaque fonction l implemente
            
            void switchOn(){//allume le seul bloc a allumer
                for(Command c : commands){
                    if(c.brighter){
                        c.setBackground(c.getBackground().brighter());
                        return;
                    }
                }
            }
            
            void switchOff(){//eteint le seul bloc a eteindre
                for(Command c : commands){
                    if(c.brighter){
                        c.setBackground(c.getBackground().darker());
                        c.brighter=false;
                        return;
                    }
                }
            }

            public void mouseDragged(MouseEvent e){
                unStick();//si on detache 
                if(!isDragging){//set la position initiale de la souris dans le bloc
                    mouseX=e.getX();
                    mouseY=e.getY();
                    isDragging=true;
                }
                //drag this et ses next
                int x=e.getXOnScreen()-x00-mouseX;
                int y=e.getYOnScreen()-y00-mouseY;
                this.setLocation(x, y);
                Command tmp=this.next;
                int distance=0;
                while(tmp!=null){
                    distance+=tmp.getHeight();
                    tmp.setLocation(x, y+distance);
                    tmp=tmp.next;
                }
                
                //allume et eteint les blocs selon les cas
                int nearby=closeCommand();
                if(nearby!=-1){//proche d un bloc
                    switchOff();//eteint tout
                    commands.get(nearby).brighter=true;//allume le seul necessaire
                }
                else switchOff();//proche de personne, tout a eteindre
                switchOn();//allume le bloc a allumer
            }

            public void mouseReleased(MouseEvent e){
                isDragging=false;
                unStick();
                Command toStick=addToList();
                if(toStick!=null) stick(toStick);//accroche ensemble
            }

            public void mouseMoved(MouseEvent e){}            
            public void mousePressed(MouseEvent e){}
            public void mouseClicked(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}

            /*****************
            * Stick together *
            *****************/

            void stick(Command toStick){
                this.setLocation(toStick.getLocation().x, toStick.getLocation().y+toStick.getHeight());
                Command tmp=this.next;
                int distance=0;
                while(tmp!=null){
                    distance+=tmp.getHeight();
                    tmp.setLocation(toStick.getLocation().x, toStick.getLocation().y+toStick.getHeight()+distance);
                    tmp=tmp.next;
                }
                switchOff();
            }
            
            Command addToList(){//retourne Command a laquelle on doit coller
                if(this.inWhiteBoard() && !commands.contains(this)) commands.add(this);//dans la liste des commandes
                if(this.isNotNext()){//next de personne
                    int closeIndex=closeCommand();//cherche apres qui se positionner
                    if(closeIndex!=-1){
                        Command c=commands.get(closeIndex);
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
                    if(this.closeHeight(c) && this.closeWidth(c)){
                        if(c.next==null) return i;
                        return -1;//le bloc proche contient deja un next donc on ne peut pas les coller
                    }
                    i++;
                }
                return -1;
            }

            boolean closeHeight(Command c){//distance entre bas de c et haut de this
                int distance=this.getLocation().y-c.getLocation().y-defaultH;
                return distance>0 && distance<15;
            }

            boolean closeWidth(Command c){//distance entre cote gauche de this et celui de c
                int distance=this.getLocation().x-c.getLocation().x;
                return distance>-5 && distance<15;
            }


            /****************
            *    Unstick    *
            ****************/

            void unStick(){//enleve this du next du bloc auquel il etait colle
                if(!isNotNext() && closeCommand()==-1)//suivant d un bloc mais se detache
                    whoseNext().next=null;
            }

            Command whoseNext(){//cherche Command qui contient this comme next
                for(Command c : commands) if(c.next==this) return c;
                return null;//a priori jamais 
            }
        }


        class CommandStart extends Command{//bloc initial present que sur WhiteBoard
            CommandStart(){
                super("start");
                this.add(new JLabel("Start your code here !"));
                this.setBounds(20, 20, this.getPreferredSize().width, super.defaultH);
                this.setBackground(Color.GREEN.darker());
            }
            
            public void mouseDragged(MouseEvent e){}//empeche Start d etre deplacee

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
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
                //this.setBackground();
            }

            void execute(){
                for(int i=0; i<Integer.parseInt(index.getText()); i++){
                    for(Command c:this.commands) c.execute();
                }
            }
        }//fin classe interne For


        //--------a faire--------
        class CommandIf extends Command implements ActionListener{//classe interne
        	protected JComboBox<String> variableG, operateur;//a priori seulement deux listes deroulantes
            protected JTextField variableD;//choix libre du joueur donc pas une liste
           
            protected String op; 
            protected int varG,varD;
            //e.g x < 100 <=> variableG(varG)="x", operateur="<", variableD(varD)="100"
            
        	CommandIf(int x, int y){
                super("if");
                this.add(new JLabel("If "));
                
                String[]variables={"x","y"}; //o
                this.variableG=new JComboBox<String>(variables);
                this.variableG.addActionListener(this);
                
                String[]tests= {"<",">","<=",">=","=="};
                this.operateur=new JComboBox<String>(tests);
                this.operateur.addActionListener(this);
                
                this.variableD=new JTextField("0");
                
                
                this.add(variableG);
                this.add(operateur);
                this.add(variableD);
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){
            	this.varD=Integer.parseInt(this.variableD.getText());
            	if(evaluate(this.op)) {
            		for(Command c:commands) c.execute();
            	}
            }

            boolean evaluate(String op) {
            	switch(op) {
            	case "<":
            		return this.varG<this.varD;
            	case "<=":
            		return this.varG<=this.varD;
            	case ">":
            		return this.varG>this.varD;
            	case ">=":
            		return this.varG>=this.varD;
            	case "==":
            		return this.varG==this.varD;
            	}
            	return false;
            }
            
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb=(JComboBox<String>)e.getSource();
				if(cb.equals(this.variableG)) {
					this.varG=((String)cb.getSelectedItem()).equals("x")?ViewPlaying.this.brush.getX():ViewPlaying.this.brush.getY();
				}
				else if(cb.equals(this.operateur)) {
					this.op=(String)cb.getSelectedItem();
				}
				
			}
        }//fin classe interne If


        /*****************
        *   Draw class   *
        *****************/

        class CommandDrawLine extends Command{//classe interne
            JTextField distance=new JTextField();//ou JList ? ou bien voir selon les cas ?

            CommandDrawLine(int x, int y){
                super("drawLine");
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
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
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){

            }
        }//fin classe interne DrawArc


        class CommandRaiseBrush extends Command{//classe interne
            CommandRaiseBrush(int x, int y){
                super("raiseBrush");
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){
                brush.setNotDrawing();
            }
        }//fin classe interne RaiseBrush


        class CommandPutBrush extends Command{//classe interne
            CommandPutBrush(int x, int y){
                super("putBrush");
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){
                brush.setDrawing();
            }
        }//fin classe interne PutBrush


        class CommandChangeAngle extends Command{//classe interne
            JTextField angle=new JTextField();//meilleur moyen de choisir l angle ? --------a faire--------

            CommandChangeAngle(int x, int y){
                super("changeAngle");
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){
                brush.changeAngle(Integer.parseInt(angle.getText()));
            }
        }//fin de classe interne ChangeAngle


        class CommandChangeColor extends Command{//classe interne
            JList color=new JList();//palette de couleur a definir --------a faire--------

            CommandChangeColor(int x, int y){
                super("changeColor");
                this.setBounds(x, y, this.getPreferredSize().width, super.defaultH);
            }

            void execute(){
                //brush.changeColor();
            }
        }//fin de classe interne ChangeAngle


        //autre classe de dessin...
    }
}
