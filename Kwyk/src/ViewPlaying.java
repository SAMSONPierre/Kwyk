import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.LinkedList;
import javax.swing.BorderFactory;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.MouseInputListener;

//autres fonctionnalites (boutons) à mettre dans l espace en bas de BlackBoard (x€[20 ; 420] et y€[440+buttonHeight+getInsets().top ; height])
public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;//fusion de WhiteBoard et CommandBoard
    private JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    private Level level;//niveau en cours
    
    private LinkedList<Vector> dessin=new LinkedList<Vector>();//execution du code du joueur -> liste de traits
    
    ViewPlaying(Player player){
        super(player);
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();//plein écran
        this.heightFS=r.height-this.getInsets().top;//getInsets().top=barre supérieur de la fenetre
        this.widthFS=r.width;      
        this.level=player.getLevel();
        addBoard();//ajout des tableaux, avec des marges de 20 (haut, bas et entre tableaux)
        addFeatures();//ajout des fonctionnalites
    }
    
    void addBoard(){
        //taille fixee
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);
        
        //taille relative a l ecran
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
    
    PanelDragDropBoard.Command getStart(){
        return this.dragDrop.commands.getFirst();
    }

    LinkedList<Vector> getDessin(){//a appeler apres la fin de l empilement, pour comparer au patron et dessiner sur BlackBoard
        return dessin;
    }
    
    
    /**********************  Classes internes  **********************/
    
    /***********************
    *      BlackBoard      *
    ***********************/
    
    public class PanelBlackBoard extends JPanel{
        protected boolean gridApparent=true;//par defaut, on voit la grille
        private Brush brush=new Brush();//fleche vide
        private int x, y;//par defaut a (0,0)==coin superieur gauche de blackBoard
        private int angle;//par defaut, orientee "->" (angle 0° sur le cercle trigo)
        private Color color;
        private boolean drawing=true;//pinceau posé par defaut

        PanelBlackBoard(){
            this.setBounds(20, 20+buttonHeight, 400, 400);//marge=20 à gauche, 20+bauteur d un bouton en haut, taille 400*400
            this.setBackground(Color.BLACK);//fond noir
            this.x=level.brushX;
            this.y=level.brushY;
            this.angle=level.brushAngle;
            this.color=level.brushFirstColor;
        }
        
        
        /******************
        * Drawing & Paint *
        ******************/

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            if(gridApparent) paintGrid(g);//grille apparente quand on le souhaite
            paintPattern(g2, level.getPattern());//patron
            paintBrush(g2, x, y, angle, color);//pinceau
        }
        
        void paintPattern(Graphics2D g2, Vector[] pattern){//dessin du patron
            g2.setStroke(new BasicStroke(5));
            for(Vector v : pattern){
                g2.setColor(v.color.darker());
                if(v instanceof Vector.VectorLine){
                    Vector.VectorLine tmp=(Vector.VectorLine)v;
                    g2.drawLine(tmp.x1, tmp.y1, tmp.x2, tmp.y2);
                }
                else if(v instanceof Vector.VectorArc){
                    Vector.VectorArc tmp=(Vector.VectorArc)v;
                    g2.drawArc(tmp.x1, tmp.y1, tmp.width, tmp.height, tmp.startAngle, tmp.scanAngle);
                }
            }
        }

        void paintGrid(Graphics g) {//dessin de la grille
            g.setColor(Color.gray);
            g.drawLine(100, 20, 100, 400);
            g.drawLine(200, 20, 200, 400);
            g.drawLine(300, 20, 300, 400);
            g.drawLine(30, 100, 400, 100);
            g.drawLine(30, 200, 400, 200);
            g.drawLine(30, 300, 400, 300);
            g.drawString("0", 5, 15);
            g.drawString("100", 90, 15);
            g.drawString("200", 190, 15);
            g.drawString("300", 290, 15);
            g.drawString("100", 3, 105);
            g.drawString("200", 3, 205);
            g.drawString("300", 3, 305);
        }
        
        void paintBrush(Graphics2D g2, int x, int y, int angle, Color color){//dessin du pinceau
            g2.setStroke(new BasicStroke(4));
            g2.translate(x, y);
            g2.rotate(Math.toRadians(angle), 0, 0);//tourne autour du point initial
            g2.setColor(color);
            g2.draw(brush);
        }
        
        
        /*******************
        *   Setter brush   *
        *******************/
        
        void changePosition(int x, int y){
            this.x=x;
            this.y=y;
        }
        
        void changeX(int x){
            this.x=x;
        }
        
        void changeY(int y){
            this.y=y;
        }
        
        void changeOrientation(int angle){
            this.angle=angle;
        }
        
        void changeColor(Color color){
            this.color=color;
        }
        
        void changeDrawing(boolean b){
            this.drawing=b;
        }
        
        
        /*******************
        *   Getter brush   *
        *******************/
        
        int getBrushX(){
            return this.x;
        }
        
        int getBrushY(){
            return this.y;
        }
        
        
        /*******************
        *    Brush class   *
        *******************/
        
        private class Brush extends Path2D.Double{//pinceau
            Brush(){//fleche dans le coin superieur gauche de blackBoard
                moveTo(-18,-5);
                lineTo(2,-5);
                lineTo(1,-12);
                lineTo(18,0);
                lineTo(1,12);
                lineTo(2,5);
                lineTo(-18,5);
                lineTo(-18,-5);
            }
        }//fin classe interne interne Brush
    }//fin classe interne PanelBlackBoard
    
    
    /************************
    *     DragDropBoard     *
    ************************/
    
    public class PanelDragDropBoard extends JPanel{
        final int x00, y00, width, height;//position initiale, largeur, hauteur du panel
        private LinkedList<Command> commands=new LinkedList<Command>();//liste de commandes ayant ete drag sur whiteBoard
        private int[] commandPositionY;//nom de leur position verticale, pour regenerer les commandes

        PanelDragDropBoard(){
            this.x00=440;
            this.y00=20+buttonHeight+ViewPlaying.this.getInsets().top;
            height=heightFS-40-buttonHeight;//40=marges haut+bas
            width=widthFS-460;//460=3 marges de colonne + taille blackBoard
            this.setLayout(null);
            this.setBounds(x00, y00-ViewPlaying.this.getInsets().top, width, height);
            this.addAvailableCommands();
        }
        
        void addAvailableCommands(){
            //ajout dans WhiteBoard
            CommandStart start=new CommandStart();
            commands.add(start);//toujours le premier de la liste de commandes
            this.add(start);
            
            //ajout dans CommandBoard
            this.commandPositionY=new int[level.getAvailableCommands().length];
            int i=0;
            int positionY=20;
            for(String c : level.getAvailableCommands()){//ajout commandes disponibles
                this.commandPositionY[i++]=positionY;
                positionY+=addCommand(c, positionY);
            }
        }
        
        int addCommand(String name, int positionY){
            switch(name){
                case "for":
                    CommandFor forC=new CommandFor(width/2+20, positionY);
                    this.add(forC);
                    this.add(forC.hookV);
                    this.add(forC.hookH);
                    return forC.getHeight()+forC.hookV.getHeight()+forC.hookH.getHeight()+10;
                case "if":
                    CommandIf ifC=new CommandIf(width/2+20, positionY);
                    this.add(ifC);
                    this.add(ifC.hookV);
                    this.add(ifC.hookH);
                    return ifC.getHeight()+ifC.hookV.getHeight()+ifC.hookH.getHeight()+10;
                case "drawLine":
                    CommandDrawLine drawC=new CommandDrawLine(width/2+20, positionY);
                    this.add(drawC);
                    return drawC.getHeight()+10;
                case "changeColor":
                    CommandChangeColor colorC=new CommandChangeColor(width/2+20, positionY);
                    this.add(colorC);
                    return colorC.getHeight();
                //--------a continuer-------
            }            
            return 0;
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            //WhiteBoard a gauche
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width/2, height);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(1, 1, width-3, height-3);//contour du whiteBoard

            //CommandBoard a droite
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(width, 0, width/2, height);
        }


        /******************
        *  Command class  *
        ******************/

        abstract class Command extends JPanel implements MouseInputListener{
            final String name;//if, else, for, while, ...
            private Color color;
            final int commandH=35;//hauteur d une commande
            final int commandW=70;//largeur par defaut de hookH
            protected Command next;//commande suivante qu on va executer
            protected Command previous;//commande precedente, pour ajuster affichage
            private int mouseX, mouseY;//position initiale de la souris au moment du drag
            private boolean isDragging=false;
            private boolean brighter=false;//commande a allumer ou non (sur laquelle on colle)
            
            Command(String name, Color color){
                this.name=name;
                this.color=color;
                this.setBackground(color);
                this.setLayout(new GridBagLayout());//pour centrer verticalement les textes
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
            }

            abstract void execute();//chaque fonction l implemente

            
            /******************
            *  Switch On/Off  *
            ******************/
            
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
                        c.setBackground(c.color);//pour eviter decalage brighter/darker
                        c.brighter=false;
                        return;
                    }
                }
            }

            
            /****************************
            *  Regeneration of Command  *
            ****************************/
            
            int getPositionY(String name){
                for(int i=0; i<level.getAvailableCommands().length; i++){
                    if(name.equals(level.getAvailableCommands()[i])) return commandPositionY[i];
                }
                return -1;
            }

            
            /*****************
            * Stick together *
            *****************/
            
            void foundPrevious(){//reformation des liens previous/next
                if(inWhiteBoard() && !commands.contains(this)){//premier drag sur whiteBoard
                    commands.add(this);
                    if(this instanceof CommandWithCommands)
                        commands.add(this.next);//ajout de HookH aussi
                    
                    //pour regenerer la commande utilisee
                    addCommand(this.name, getPositionY(this.name));
                    SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
                }
                int closeIndex=closeCommand();//cherche index du precedent
                if(closeIndex!=-1){//s il existe
                    Command thisPrevious=commands.get(closeIndex);
                    thisPrevious.setNext(this);
                    //update apres ajout de this :
                    updateHookVRec(getTmpCwc());//adapte hauteur de tous les hookV recursivement
                    updateAllLocation();//met a jour position des blocs impactes
                }
            }
            
            void setNext(Command newNext){//changement pour que this.next=newNext, s utilise dans foundPrevious()
                if(this.next!=null){//insertion dans la liste chainee
                    Command end=newNext;//fin de ce qu on drag
                    while(end.next!=null) end=end.next;
                    end.next=this.next;
                    this.next.previous=end;
                }
                this.next=newNext;
                newNext.previous=this;                
            }

            void stick(){//colle this a son precedent
                if(!(this instanceof HookHorizontal)){//deplacement de HookH gerer dans celui de CWC
                    int positionX=previous.getLocation().x;//emplacement horizontal de this
                    if(previous instanceof CommandWithCommands)
                        positionX+=((CommandWithCommands)previous).hookV.getWidth();//colle a l interieur
                    this.setLocation(positionX, previous.getLocation().y+previous.getHeight());
                }
                if(this.next!=null) this.next.stick();
            }

            boolean inWhiteBoard(){//est dans whiteBoard
                Point p=this.getLocation();
                return (p.x>0 && p.x<width/2 && p.y>0 && p.y<height);
            }

            int closeCommand(){//this et c sont assez proches pour se coller
                int i=0;
                for(Command c : commands){
                    if(c instanceof CommandWithCommands){
                        if(this.closeHeight(c) && this.closeWidthIntern((CommandWithCommands)c))
                            return i;
                    }
                    else if(this.closeHeight(c) && this.closeWidth(c)) return i;
                    i++;
                }
                return -1;
            }

            boolean closeHeight(Command c){//distance entre bas de c et haut de this
                int distance=this.getLocation().y-c.getLocation().y-c.getHeight();
                return distance>0 && distance<15;
            }

            boolean closeWidth(Command c){//distance entre cote gauche de this et celui de c
                int distance=this.getLocation().x-c.getLocation().x;
                return distance>-5 && distance<15;
            }

            boolean closeWidthIntern(CommandWithCommands c){//closeWidth pour CommandWithcCommands
                int distance=this.getLocation().x-c.getLocation().x;
                return distance>c.hookV.getWidth() && distance<c.hookV.getWidth()+20;
            }


            /****************
            *    Unstick    *
            ****************/

            void unStick(){//decolle this (dragged) de son previous
                if(this.previous==null) return;//commande seule
                Command nextHookH=inCWC();//fin du bloc imbriquant ou null
                if(nextHookH!=null){//this etait dans un bloc imbriquant
                    Command tmpPrevious=this.previous;//pour update le bloc imbriquant ensuite
                    this.previous.next=nextHookH;//accroche precedent avec hookH du cwc
                    nextHookH.previous.next=null;//detache la fin du drag
                    nextHookH.previous=this.previous;//accroche hookH avec precedent
                    this.previous=null;//detache debut du drag
                    //update apres depart de this :
                    tmpPrevious.updateHookVRec(tmpPrevious.getTmpCwc());//taille des hookV
                    tmpPrevious.updateAllLocation();//position des blocs
                }
                else{//on decroche tout a partir de this
                    this.previous.next=null;
                    this.previous=null;
                }
            }
            
            Command inCWC(){//cherche fin du bloc imbriquant
                Command tmp=this.next;
                while(tmp!=null){
                    if(tmp instanceof HookHorizontal){
                        if(tmp.getX()<this.getX()) return tmp;
                    }
                    tmp=tmp.next;
                }
                return null;
            }


            /********************
            * Update of command *
            ********************/
            
            void updateAllLocation(){//met a jour localisation depuis tete de liste
                Command head=this;
                while(head.previous!=null) head=head.previous;
                if(head!=this && head.next!=null) head.next.stick();//met a jour recursivement blocs apres head
            }
            
            LinkedList<CommandWithCommands> getTmpCwc(){//renvoie toutes les cwc liees a this
                LinkedList<CommandWithCommands> res=new LinkedList<CommandWithCommands>();
                if(this instanceof CommandWithCommands) res.add((CommandWithCommands)this);
                Command tmp=this.previous;//pour parcourir les precedents
                Command tmp2=this.next;//pour parcourir les suivants
                while(tmp!=null){
                    if(tmp instanceof CommandWithCommands) res.add((CommandWithCommands)tmp);
                    tmp=tmp.previous;
                }
                while(tmp2!=null){
                    if(tmp2 instanceof CommandWithCommands) res.add((CommandWithCommands)tmp2);
                    tmp2=tmp2.next;
                }
                return res;
            }
            
            CommandWithCommands innerHook(){//cherche les bloc imbriquants internes
                Command tmp=this.next;//this est forcement une cwc
                while(!(tmp instanceof HookHorizontal)){//donc s arrete
                    if(tmp instanceof CommandWithCommands) return (CommandWithCommands)tmp;
                    tmp=tmp.next;
                }
                return null;//pas de bloc imbriquante a l interieur de this, on peut ajuster this
            }
            
            void updateHookVRec(LinkedList<CommandWithCommands> list){//met a jour les hookV recursivement
                if(!(list.isEmpty())){
                    CommandWithCommands first=list.getFirst();
                    list.remove(first);
                    CommandWithCommands inner=first.innerHook();//bloc imbriquante dans commande interne
                    if(inner!=null) inner.updateHookVRec(list);
                    first.updateHookV();//redimensionnement de first
                    updateHookVRec(list);
                }
            }
            
            void setToForeground(){//this a l avant-plan quand on le drag
                if(this instanceof CommandWithCommands){
                    CommandWithCommands tmp=(CommandWithCommands)this;
                    ViewPlaying.this.dragDrop.remove(tmp.hookV);
                    ViewPlaying.this.dragDrop.add(tmp.hookV, 0);
                }
                ViewPlaying.this.dragDrop.remove(this);
                ViewPlaying.this.dragDrop.add(this, 0);
                if(this.next!=null) this.next.setToForeground();//appel recursif
            }


            /*****************
            * Mouse Override *
            *****************/

            public void mouseDragged(MouseEvent e){
                if(!isDragging){//ce qu on fait au premier click
                    unStick();//detache si a un precedent
                    mouseX=e.getX();//position initiale de souris
                    mouseY=e.getY();
                    isDragging=true;
                    this.setToForeground();//mettre ce qu on drag a l avant-plan
                }
                
                //drag this et ses next
                int x=e.getXOnScreen()-x00-mouseX-ViewPlaying.this.getX();
                int y=e.getYOnScreen()-y00-mouseY-ViewPlaying.this.getY();
                this.setLocation(x, y);
                if(this.next!=null) this.next.stick();
                
                //allume et eteint les blocs selon les cas
                int nearby=closeCommand();
                if(nearby!=-1){//proche d un bloc
                    switchOff();//eteint tout
                    commands.get(nearby).brighter=true;//allume le seul necessaire
                }
                else switchOff();//proche de personne, tout a eteindre
                switchOn();//allume le seul bloc a allumer
            }

            public void mouseReleased(MouseEvent e){
                isDragging=false;
                switchOff();//eteint tout
                foundPrevious();//noue les liens avec precedent
            }
            
            public void mouseMoved(MouseEvent e){}            
            public void mousePressed(MouseEvent e){}
            public void mouseClicked(MouseEvent e){//pour verification, a enlever apres
                System.out.println(this.name);
                if(this.previous!=null) this.previous.mouseClicked(e);
                else System.out.println("debut");
            }
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne interne Command
        

        class CommandStart extends Command{//bloc initial present que sur WhiteBoard
            CommandStart(){
                super("start", Color.GREEN.darker());
                this.add(new JLabel("   Start your code here !   "));
                this.setBounds(20, 20, getPreferredSize().width, commandH);
                this.setBackground(Color.GREEN.darker());
            }

            void execute(){
                super.next.execute();
            }
            
            public void mouseDragged(MouseEvent e){}//empeche Start d etre deplacee
        }//fin classe interne Start


        /*********************
        * Bloc with commands *
        *********************/
        
        class CommandWithCommands extends Command{
            final HookVertical hookV;//juste une accroche verticale
            final HookHorizontal hookH;//next si pas de commandes internes
            
            CommandWithCommands(String name, Color color, int x, int y){
                super(name, color);
                hookV=new HookVertical(color, commandH, x, y+commandH);//apres la commande
                hookH=new HookHorizontal(color, x, y+2*commandH);//apres hookV
                this.next=hookH;
                hookH.previous=this;
            }
            
            void execute(){
                Command tmp=this.next;
                while(tmp!=this.hookH){
                    tmp.execute();
                    tmp=tmp.next;
                }
            }//sera complete par ses enfants
                        
            void updateHookV(){//met a jour hookV et accroche les hook a this
                int res=0;
                Command tmp=this.next;
                if(tmp==this.hookH) res=commandH;//cwc vide
                else{
                    while(tmp!=this.hookH){//tant qu on n a pas la fin du bloc imbriquant
                        res+=tmp.getHeight();
                        if(tmp instanceof CommandWithCommands){
                            if(((CommandWithCommands)tmp).hookH==tmp.next) res+=commandH;
                        }
                        tmp=tmp.next;
                    }                    
                }
                hookV.setBounds(res, hookH);
            }
            
            public void setLocation(int x, int y){
                super.setLocation(x, y);
                hookV.setLocation(x, y+getHeight());
                hookH.setLocation(x, hookV.getY()+hookV.getHeight());
            }


            class HookVertical extends JPanel{//accroche verticale des blocs imbriquants
                HookVertical(Color color, int commandH, int x, int y){
                    this.setBackground(color);
                    this.setBounds(x, y, 15, commandH);
                }
                
                void setBounds(int height, HookHorizontal hookH){
                    setBounds(getX(), getY(), getWidth(), height);
                    hookH.setLocation(getX(), getY()+height);
                }
            }//fin classe HookVertical
        }//fin classe CommandWithCommands


        class HookHorizontal extends Command{//fin de la boucle/condition
            HookHorizontal(Color color, int x, int y){
                super("hookHorizontal", color);
                this.setBounds(x, y, commandW, commandH/2);
            }

            public void execute(){
                if(next!=null) next.execute();
            }

            public void mouseDragged(MouseEvent e){}//ne peut pas etre dragged seul
        }//fin classe HookHorizontal


        class CommandFor extends CommandWithCommands{//classe interne
            private JTextField index;//nombre de repetition a saisir

            CommandFor(int x, int y){
                super("for", Color.ORANGE.darker(), x, y);
                this.add(new JLabel("   Repeat   "));
                this.index=new JTextField(2);//par defaut a 0, c est le joueur qui choisit
                this.add(index);
                this.add(new JLabel("   time   "));
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
                for(int i=0; i<Integer.parseInt(index.getText()); i++)
                    super.execute();
                this.hookH.execute();
            }
        }//fin classe interne For


        class CommandIf extends CommandWithCommands implements ActionListener{//classe interne
            private JComboBox<String> variableG, operateur;//a priori seulement deux listes deroulantes
            private JTextField variableD;//choix libre du joueur donc pas une liste
           
            private String op; 
            private int varG,varD;
            //e.g x < 100 <=> variableG(varG)="x", operateur="<", variableD(varD)="100"
            
            CommandIf(int x, int y){
                super("if", Color.PINK.darker(), x, y);
                this.add(new JLabel("   If   "));

                String[]variables={" x "," y "}; //o
                this.variableG=new JComboBox<String>(variables);
                this.variableG.addActionListener(this);

                String[]tests= {" < "," > "," <= "," >= "," == "};
                this.operateur=new JComboBox<String>(tests);
                this.operateur.addActionListener(this);

                this.variableD=new JTextField(2);


                this.add(variableG);
                this.add(operateur);
                this.add(variableD);
                this.add(new JLabel("   "));//pour la presentation
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
            	this.varD=Integer.parseInt(this.variableD.getText());
            	if(evaluate(this.op)) super.execute();//execute Command apres hookH
                this.hookH.execute();
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
            
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cb=(JComboBox<String>)e.getSource();
                if(cb.equals(this.variableG)) {
                    this.varG=((String)cb.getSelectedItem()).equals("x")?blackBoard.x:blackBoard.y;
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
            private JTextField distance=new JTextField(2);

            CommandDrawLine(int x, int y){
                super("drawLine", Color.CYAN.darker());
                this.add(new JLabel("   Draw a line of  "));
                this.add(distance);
                this.add(new JLabel("   pixels   "));
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
                //ajout du vecteur dans le dessin du joueur
                if(blackBoard.drawing){
                    Vector tmp=new Vector();//pour creer objet interne
                    int x2=0;
                    int y2=0;
                    Vector.VectorLine trait=tmp.new VectorLine(
                        blackBoard.x, blackBoard.y, x2, y2, blackBoard.color);
                    dessin.add(trait);
                }
                
                //nouvel emplacement du pinceau
                //--------a faire--------
                
                
                if(next!=null) next.execute();
            }
        }//fin classe interne DrawLine


        //--------a faire--------
        class CommandDrawArc extends Command{//classe interne            
            CommandDrawArc(int x, int y){
                super("drawArc", Color.CYAN.darker());
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
                if(blackBoard.drawing){
                    
                }
                
                
                if(next!=null) next.execute();
            }
        }//fin classe interne DrawArc


        class CommandRaisePutBrush extends Command{//classe interne
            private JComboBox<String> choiceBox;
            private boolean choiceRes;
            
            CommandRaisePutBrush(int x, int y){
                super("raisePutBrush", Color.CYAN.darker());
                String[] choice={"Raise", "Put"};
                this.choiceBox=new JComboBox<String>(choice);
                this.add(choiceBox);
                this.add(new JLabel("   the pen   "));
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
                blackBoard.drawing=choiceRes;
                if(next!=null) next.execute();
            }
            
            public void actionPerformed(ActionEvent e){
                if(e.getSource()==this.choiceBox)
                    choiceRes=((String)choiceBox.getSelectedItem()).equals("Raise");
            }
        }//fin classe interne RaiseBrush


        class CommandChangeAngle extends Command{//classe interne
            JTextField angle=new JTextField();//meilleur moyen de choisir l angle ? --------a faire--------

            CommandChangeAngle(int x, int y){
                super("changeAngle", Color.CYAN.darker());
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }

            void execute(){
                blackBoard.angle=(Integer.parseInt(angle.getText()));
                if(next!=null) next.execute();
            }
        }//fin de classe interne ChangeAngle


        class CommandChangeColor extends Command{//classe interne
            private JComboBox colorChoice=new JComboBox();
            final Color[] palette={Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA,
                Color.ORANGE, Color.RED, Color.WHITE, Color.YELLOW};
            private Color colorRes;

            CommandChangeColor(int x, int y){
                super("changeColor", Color.CYAN.darker());
                this.add(new JLabel("   Change color to   "));
                
                for(int i=0; i<8; i++) colorChoice.addItem(palette[i]);
                colorChoice.setRenderer(new ColorComboRenderer());
                this.add(this.colorChoice);
                
                this.add(new JLabel("   "));
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            public void actionPerformed(ActionEvent e){
                if(e.getSource()==this.colorChoice){
                    for(Color c : palette){
                        if(colorChoice.getSelectedItem().equals(c)) colorRes=c;
                    }
                }
            }

            void execute(){
                blackBoard.color=colorRes;
                if(next!=null) next.execute();
            }
            
            
            class ColorComboRenderer extends JPanel implements ListCellRenderer{
                Color main=Color.BLUE;
                
                ColorComboRenderer(){
                    super();
                    this.setPreferredSize(new Dimension(30,15));
                    setBorder(new CompoundBorder(new LineBorder(Color.WHITE), new LineBorder(Color.BLACK)));
                }
                
                public Component getListCellRendererComponent(JList list, Object obj, int row, boolean sel, boolean hasFocus){
                    if(obj instanceof Color) main=(Color)obj;
                    return this;
                  }
                
                public void paint(Graphics g){
                    setBackground(main);
                    super.paint(g);
                }
            }
        }//fin de classe interne ChangeAngle


        //autre classe de dessin...
    }//fin classe interne PanelDragDropBoard
}