import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;

public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;//fusion de WhiteBoard et CommandBoard
    private JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    private Level level;//niveau en cours
    private JProgressBar limite;
    
    
    ViewPlaying(Player player, boolean isCreating) throws IOException{
        super(player);
        this.level=player.getLevel();
        addBoard();//ajout des tableaux, avec des marges de 20 (haut, bas et entre tableaux)
        addFeatures(isCreating);//ajout des fonctionnalites
    }
    
    void addBoard() throws IOException{
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);//taille fixee
        dragDrop=new PanelDragDropBoard();
        this.add(dragDrop);//taille relative a l ecran
    }
    
    void addFeatures(boolean isCreating){
        features.setBounds(20, 440+buttonHeight, 400, heightFS-460-buttonHeight);
        this.add(features);
        
        //voir la grille:
        JButton seeGrid=new JButton("See grid");
        seeGrid.addActionListener((event)->{
            blackBoard.gridApparent=!blackBoard.gridApparent;
            blackBoard.repaint();
        });
        features.add(seeGrid);
        
        //actionner le code
        JButton run=new JButton("Run");
        run.addActionListener((event)->this.run());
        features.add(run);
        
        //arreter le code
        JButton stop=new JButton("Stop");
        stop.addActionListener((event)->{
        	//------a faire------
        });
        features.add(stop);
        
        
        //creer un niveau -> que pour la page Create
        if(isCreating){
            JButton submit=new JButton("Submit");
            submit.addActionListener((event)->{
                String name=JOptionPane.showInputDialog(this,"Level's name ?", null);	
                super.control.submit(name, level);
            });
            features.add(submit);
        }
        
        //limite des commandes si on est dans un niveau
        if(!isCreating) {
            limite=new JProgressBar(0, level.numberOfCommands){
                public String getString(){//presentation apparente
                    return getValue()+"/"+level.numberOfCommands;
                }
            };
            limite.setStringPainted(true);
            features.add(limite);
        }
        
        //---que pour le test, a supprimer une fois le sommaire fonctionnel---
        /*JButton load=new JButton("Load");
        load.addActionListener((event)->{
            String name=JOptionPane.showInputDialog(this,"Level's name ?", null);	
            super.control.load(name);
        });
        features.add(load);*/
    }
    
    void run(){
        this.level.initializePlayerDraw();//vide le dessin du joueur
        this.blackBoard.brush.resetBrush();//remet le pinceau a l'emplacement initial
        this.dragDrop.commands.getFirst().execute(true);//s execute si tout est bon (pas de champ vide)
        //if(level.compare()) JOptionPane.showMessageDialog(this, "Victory !");
    }
    
    void victoryMessage(){
        if(level.compare()){
            int lvl=Integer.parseInt(level.name.charAt(0)+"");
            getModel().getPlayer().currentLevel[getNumberOfDirectory(level.name)][lvl]=true;
            JOptionPane.showMessageDialog(this, "Victory !");
        }
    }
    
    String[] getCommandsArray(){
        return dragDrop.listToTab(dragDrop.getCommands());
    }
    
    int getNumberFromHead(){
        return dragDrop.getNumberFromHead();
    }
    
    int getNumberOfDirectory(String name){
        File[] arrayLevels=nombreNiveau("levels/training/");
        for(int i=0; i<arrayLevels.length; i++){
            File[] arrayLevels2=nombreNiveau("levels/training/"+arrayLevels[i].getName());
            for(int j=0; j<arrayLevels2.length; j++){
                String str=arrayLevels2[j].getName().substring(0, arrayLevels2[j].getName().length()-4);
                if(str.equals(level.name)) return i;
            }

        }
        return 0;
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
        private Color brushColor;
        private boolean drawing=true;//pinceau posé par defaut

        PanelBlackBoard(){
            this.setBounds(20, 20+buttonHeight, 400, 400);//marge=20 à gauche, 20+bauteur d un bouton en haut, taille 400*400
            this.setBackground(Color.BLACK);//fond noir
            this.x=level.brushX;
            this.y=level.brushY;
            this.angle=level.brushAngle;
            this.brushColor=level.brushFirstColor;
        }
        
        
        /******************
        * Drawing & Paint *
        ******************/

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(gridApparent) paintGrid(g);//grille apparente quand on le souhaite
            Graphics2D g2=(Graphics2D)g;
            g2.setStroke(new BasicStroke(4));
            for(Vector v : level.getPattern()) paintVector(g2, v, true);//patron
            for(Vector v : level.getPlayerDraw()) paintVector(g2, v, false);//dessin du joueur, au debut vide
            paintBrush(g2, x, y, angle, brushColor);//pinceau en dernier car rotation
        }

        void paintGrid(Graphics g){//dessin de la grille
            g.setColor(Color.gray.darker());
            for(int i=100; i<400; i+=100){
                g.drawLine(i, 20, i, 400);
                g.drawLine(30, i, 400, i);
                g.drawString(Integer.toString(i), i-10, 15);
                g.drawString(Integer.toString(i), 3, i+5);
            }
            g.drawString("0", 5, 15);
        }
        
        void paintVector(Graphics2D g2, Vector v, boolean darker){
            if(darker) g2.setColor(v.color.darker());
            else g2.setColor(v.color);
            if(v instanceof Vector.VectorLine){
                Vector.VectorLine tmp=(Vector.VectorLine)v;
                g2.drawLine(tmp.x1, tmp.y1, tmp.x2, tmp.y2);
            }
            else if(v instanceof Vector.VectorArc){
                Vector.VectorArc tmp=(Vector.VectorArc)v;
                g2.drawArc(tmp.x1, tmp.y1, tmp.diameter, tmp.diameter, tmp.startAngle, tmp.scanAngle);
            }
        }
        
        void paintBrush(Graphics2D g2, int x, int y, int angle, Color color){//dessin du pinceau
            g2.translate(x, y);
            g2.rotate(Math.toRadians(-angle), 0, 0);//tourne autour du point initial
            g2.setColor(color);
            g2.draw(brush);
        }
        
        
        /*******************
        *    Brush class   *
        *******************/
        
        private class Brush extends Path2D.Double{//pinceau
            Brush(){//fleche dans le coin superieur gauche de blackBoard
            	this.resetBrush();
            }
            
            void resetBrush() {
                PanelBlackBoard.this.x=level.brushX;
            	PanelBlackBoard.this.y=level.brushY;
            	PanelBlackBoard.this.angle=level.brushAngle;
            	PanelBlackBoard.this.brushColor=level.brushFirstColor;
            	PanelBlackBoard.this.drawing=true;
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
        private Bin bin;

        PanelDragDropBoard() throws IOException{
            this.x00=440;
            this.y00=20+buttonHeight+ViewPlaying.this.getInsets().top;
            width=widthFS-460;//460=3 marges de colonne + taille blackBoard
            height=heightFS-40-buttonHeight;//40=marges haut+bas
            this.setLayout(null);
            this.setBounds(x00, y00-ViewPlaying.this.getInsets().top, width, height);
            this.addAvailableCommands();
            
            this.bin=new Bin();
            this.add(bin);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        void addAvailableCommands(){
            //ajout dans WhiteBoard
            commands.add(new CommandStart());//toujours le premier de la liste de commandes
            this.add(commands.getFirst());
            
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
                    CommandDrawLine drawLineC=new CommandDrawLine(width/2+20, positionY);
                    this.add(drawLineC);
                    return drawLineC.getHeight()+10;
                case "drawArc":
                    CommandDrawArc drawArcC=new CommandDrawArc(width/2+20, positionY);
                    this.add(drawArcC);
                    return drawArcC.getHeight()+10;
                case "raisePutBrush":
                    CommandRaisePutBrush raisePutC=new CommandRaisePutBrush(width/2+20, positionY);
                    this.add(raisePutC);
                    return raisePutC.getHeight()+10;
                case "setAngle":
                    CommandSetAngle shiftAngleC=new CommandSetAngle(width/2+20, positionY);
                    this.add(shiftAngleC);
                    return shiftAngleC.getHeight()+10;
                case "setColor":
                    CommandSetColor colorC=new CommandSetColor(width/2+20, positionY);
                    this.add(colorC);
                    return colorC.getHeight()+10;
                case "moveTo":
                    CommandMoveTo moveC=new CommandMoveTo(width/2+20, positionY);
                    this.add(moveC);
                    return moveC.getHeight()+10;
                case "addAngle":
                    CommandAddAngle addAngleC=new CommandAddAngle(width/2+20, positionY);
                    this.add(addAngleC);
                    return addAngleC.getHeight()+10;
                default://fonction
                    if(!(name.equals("hookHorizontal"))){
                        CommandFunctionInit functionC=new CommandFunctionInit(name, width/2+20, positionY);
                        this.add(functionC);
                        this.add(functionC.hookV);
                        this.add(functionC.hookH);
                        return functionC.getHeight()+functionC.hookV.getHeight()+functionC.hookH.getHeight()+10;
                    }
                    return 0;//on ne cree pas de hookHorizontal seul
            }
        }
        
        void addCommandCall(CommandFunctionInit init, int positionY){
            CommandFunctionCall callC=new CommandFunctionCall(init, width/2+20, positionY);
            this.add(callC);
            init.caller.add(callC);
        }
        
        int getNumberFromHead(){
            int res=0;
            Command tmp=this.commands.getFirst().next;//deuxieme commande du code du joueur
            while(tmp!=null){
                if(!(tmp instanceof CommandWithCommands)) res++;//un cwc a toujours un hookH donc 2 commandes
                tmp=tmp.next;
            }
            return res;
        }
        
        boolean notAdd(LinkedList<Command> list, Command test){
            for(Command c : list){
                if(c.name.equals(test.name)) return false;//deja dans la liste
            }
            return true;//pas dans la liste
        }
        
        LinkedList<Command> getCommands(){
            LinkedList<Command> res=new LinkedList<Command>();
            Command tmp=this.commands.getFirst().next;
            while(tmp!=null){
                if(notAdd(res, tmp)) res.add(tmp);
                tmp=tmp.next;
            }
            return res;
        }
        
        String[] listToTab(LinkedList<Command> list){
            int size=list.size();
            String[] res=new String[size];
            for(int i=0; i<size; i++) res[i]=list.get(i).name;
            return res;
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
        *       Bin       *
        ******************/
        
        private class Bin extends JPanel{
            BufferedImage state;

            Bin() throws IOException{
                this.setBounds(width/2+5, height-50, 40, 40);
                this.loadBin("images/closedBin.png");
            }

            void loadBin(String path) throws IOException{//ouverte ou fermee
                try{
                    this.state=ImageIO.read(new File(path));
                    this.repaint();
                }
                catch(FileNotFoundException e){
                    System.out.println("Image could not be found");
                }
            }

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(this.state,0,0,40,40,null);
            }
        }


        /******************
        *  Command class  *
        ******************/

        abstract class Command extends JPanel implements MouseInputListener{
            final String name;//if, else, for, while, ...
            private Color color;
            final int commandH=35, commandW=70;//hauteur d une commande, largeur par defaut de hookH
            protected Command next, previous;//next a executer, previous pour ajuster l affichage
            private int mouseX, mouseY;//position initiale de la souris au moment du drag
            private boolean isDragging, brighter;//drag ; a allumer -> default=false
            
            Command(String name, Color color){
                this.name=name;
                this.color=color;
                this.setBackground(color);
                this.setLayout(new GridBagLayout());//pour centrer verticalement les textes
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
            }
            
            abstract void initializeDisplay();
            abstract boolean canExecute();
            abstract void execute(boolean executeNext);//chaque fonction les implemente

            
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
            
            
            /*****************
            * Delete Command *
            ******************/
            
            boolean toDelete(){//quand pres de la poubelle
                if(!(this instanceof CommandFunctionInit)){//ne supprime pas initialisateur de fonction
                    int distanceH=bin.getLocation().y-bin.getHeight()-this.getLocation().y;
                    int distanceW=bin.getLocation().x-this.getLocation().x;
                    return distanceH>-70 && distanceH<15 && distanceW>-this.getWidth()/3 && distanceW<this.getWidth();
                }
                return false;
            }
            
            void updateBinState() throws IOException{
            	if(this.toDelete()) bin.loadBin("images/openBin.png");
            	else bin.loadBin("images/closedBin.png");
            }
            
            void deleteSteps() throws IOException{//enleve de commands et du panel
            	if(this instanceof CommandWithCommands){
                    ((CommandWithCommands)this).hookH.removeHH();
                    ((CommandWithCommands)this).hookV.removeHV();
                }
                PanelDragDropBoard.this.remove(this);
                if(commands.contains(this)) commands.remove(this);//sur whiteBoard
                else if(this instanceof CommandFunctionCall){
                    CommandFunctionCall tmp=(CommandFunctionCall)this;
                    addCommandCall(tmp.function, getPositionY(tmp.function.name));
                }
                else addCommand(this.name, getPositionY(this.name));
                SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
                bin.loadBin("images/closedBin.png");
                if(this.next!=null) this.next.deleteSteps();
            }
            
            
            /****************************
            *  Regeneration of Command  *
            ****************************/
            
            void newDrag(){//nouvelle commande qu on drag pour la premiere fois
                if(inWhiteBoard() && !commands.contains(this)){//premier drag sur whiteBoard
                    commands.add(this);
                    if(this instanceof CommandWithCommands) commands.add(this.next);//ajout de HookH aussi
                    //pour regenerer commande utilisee :
                    if(this instanceof CommandFunctionCall){
                        CommandFunctionInit init=((CommandFunctionCall)this).function;
                        addCommandCall(init, getPositionY(init.name));
                    }
                    else addCommand(this.name, getPositionY(this.name));
                    SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
                }
            }

            boolean inWhiteBoard(){//est dans whiteBoard
                Point p=this.getLocation();
                return (p.x>0 && p.x<width/2 && p.y>0 && p.y<height);
            }
            
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

            int closeCommand(){//this et c sont assez proches pour se coller
                if(!(this instanceof CommandFunctionInit)){//initialisateur de fonction sans previous
                    int i=0;
                    for(Command c : commands){
                        if(c instanceof CommandWithCommands){
                            if(this.closeHeight(c) && this.closeWidthIntern((CommandWithCommands)c))
                                return i;
                        }
                        else if(this.closeHeight(c) && this.closeWidth(c)) return i;
                        i++;
                    }
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
                Command p=this.previous;//pour parcourir les precedents
                Command n=this.next;//pour parcourir les suivants
                while(p!=null){
                    if(p instanceof CommandWithCommands) res.add((CommandWithCommands)p);
                    p=p.previous;
                }
                while(n!=null){
                    if(n instanceof CommandWithCommands) res.add((CommandWithCommands)n);
                    n=n.next;
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
            
            void setToForeground(){//this a l avant-plan quand on le drag==tete de liste
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
            
            int getNumberFromThis(){//nombre de commandes qu on drag
                int res=0;
                Command tmp=this;
                while(tmp!=null){
                    if(!(tmp instanceof CommandWithCommands)) res++;
                    tmp=tmp.next;
                }
                return res;
            }

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
                switchOff();//eteint tout d abord
                if(nearby!=-1){//proche d un bloc
                    if(limite==null || getNumberFromThis()+getNumberFromHead()<=level.numberOfCommands)//attachable
                        commands.get(nearby).brighter=true;//allume le seul necessaire
                }
                switchOn();//allume le seul bloc a allumer
                
                //ouvre eventuellement la poubelle
                try{
                    updateBinState();
                }
                catch(IOException e1){
                    System.out.println("Couldn't update bin");
                }
            }

            public void mouseReleased(MouseEvent e){
                isDragging=false;
                switchOff();//eteint tout
                try{
                    if(this.toDelete()){
                        this.deleteSteps();
                        if(limite!=null) limite.setValue(getNumberFromHead());
                        return;//sinon peut ajouter this a commands car appel a foundPrevious
                    }
                }
                catch(IOException e1){
                    System.out.println("Couldn't delete command");
                }
                newDrag();
                if(limite!=null){//pas en train de creer
                    if(getNumberFromHead()+getNumberFromThis()<=level.numberOfCommands) foundPrevious();
                    limite.setValue(getNumberFromHead());
                }
                else foundPrevious();//noue les liens avec precedent
            }
            
            public void mouseMoved(MouseEvent e){}
            public void mousePressed(MouseEvent e){}
            public void mouseClicked(MouseEvent e){//pour verification, a enlever apres
                //if(this.canExecute()) this.execute(true);
            }
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne interne Command
        

        class CommandStart extends Command{//bloc initial present que sur WhiteBoard
            CommandStart(){
                super("start", Color.GREEN.darker());
                initializeDisplay();
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Start your code here !  "));
                this.setBounds(20, 20, getPreferredSize().width, commandH);
            }
            
            public void mouseDragged(MouseEvent e){}//empeche Start d etre deplacee
            
            boolean canExecute(){//verifie qu il n y a pas de champs vide, et champs vide en rouge
                Command tmp=this.next;
                boolean ok=true;
                while(tmp!=null){
                    if(!(tmp.canExecute())) ok=false;//on ne s arrete pas
                    tmp=tmp.next;
                }
                return ok;
            }

            void execute(boolean executeNext){///execute toujours le suivant
                if(canExecute() && next!=null) next.execute(true);
            }
        }//fin classe interne Start
        
        
        class CommandFunctionCall extends Command{
            private CommandFunctionInit function;
            private JLabel name=new JLabel();
            
            CommandFunctionCall(CommandFunctionInit function, int x, int y){
                super("Call", new Color(212, 115, 212));
                this.function=function;
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                name.setText(function.nameFunction.getText());
                setSize(getPreferredSize().width, commandH);
                this.add(name);
            }
            
            boolean inFunction(Command c){
                while(c.previous!=null) c=c.previous;
                return (c==this.function);
            }
            
            int closeCommand(){//this et c sont assez proches pour se coller
                int i=0;
                for(Command c : commands){
                    if(!inFunction(c)){
                        if(c instanceof CommandWithCommands){
                            if(this.closeHeight(c) && this.closeWidthIntern((CommandWithCommands)c))
                                return i;
                        }
                        else if(this.closeHeight(c) && this.closeWidth(c)) return i;
                    }
                    i++;
                }                
                return -1;
            }
            
            boolean canExecute(){
                return function.canExecute();
            }
            
            void execute(boolean executeNext){
                function.execute(executeNext);                
                if(executeNext && next!=null) next.execute(true);
            }
        }


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
            
            void initializeDisplay(){}//implementer par ses enfants
            
            boolean canExecute(){//ne peut pas etre vide
                if(this.next==this.hookH) return false;
                return true;
            }
            
            void execute(boolean executeNext){
                Command tmp=this.next;
                while(tmp!=this.hookH){
                    tmp.execute(false);//execute du next manuellement
                    tmp=tmp.next;//en incrementant tmp
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
                            if(((CommandWithCommands)tmp).hookH==tmp.next) res+=commandH;//cwc vide
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
                
                void removeHV(){
                    PanelDragDropBoard.this.remove(this);
                }
            }//fin classe interne interne HookVertical
        }//fin classe CommandWithCommands


        class HookHorizontal extends Command{//fin de la boucle/condition
            HookHorizontal(Color color, int x, int y){
                super("hookHorizontal", color);
                this.setBounds(x, y, commandW, commandH/2);
            }
            
            void initializeDisplay(){}//n a pas de presentation
            public void mouseDragged(MouseEvent e){}//ne peut pas etre dragged seul
            
            void removeHH(){
            	PanelDragDropBoard.this.remove(this);
                commands.remove(this);
            }
            
            boolean canExecute(){
                return true;
            }

            public void execute(boolean executeNext){
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin classe HookHorizontal


        class CommandFor extends CommandWithCommands{//classe interne
            private JTextField index;//nombre de repetition a saisir

            CommandFor(int x, int y){
                super("for", Color.ORANGE.darker(), x, y);
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Repeat  "));
                this.index=new JTextField(3);//c est le joueur qui choisit
                this.add(index);
                this.add(new JLabel("  time  "));
            }
            
            boolean canExecute(){
                int i=index.getText().length();
                if(i==0) index.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else index.setBorder(null);
                return super.canExecute() && i!=0;
            }

            void execute(boolean executeNext){
                for(int i=0; i<Integer.parseInt(index.getText()); i++) super.execute(false);
                this.hookH.execute(true);
            }
        }//fin classe interne For


        class CommandIf extends CommandWithCommands{//classe interne
            private JComboBox variableG=new JComboBox();
            private JComboBox operateur=new JComboBox();//a priori seulement deux listes deroulantes
            private JTextField variableD=new JTextField(3);//choix libre du joueur donc pas une liste
            private String op="<";//par defaut
            private int varG=blackBoard.x, varD;
            //e.g x<100 <=> variableG(varG)="x", operateur="<", variableD(varD)="100"
            
            CommandIf(int x, int y){
                super("if", Color.PINK.darker(), x, y);
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  If  "));

                this.variableG.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==variableG){
                            varG=variableG.getSelectedItem().equals(" x ")?blackBoard.x:blackBoard.y;
                        }
                    }
                });
                this.variableG.addItem(" x ");
                this.variableG.addItem(" y ");

                this.operateur.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==operateur) op=(String)operateur.getSelectedItem();
                    }
                });
                String[] tmpOp={" <", " >", " <=", " >=", " =="};
                for(String s : tmpOp) this.operateur.addItem(s);

                this.add(variableG);
                this.add(operateur);
                this.add(variableD);
                this.add(new JLabel("  "));//pour la presentation
            }

            boolean evaluate(String op){
            	switch(op){
            	case " <":
                    return this.varG<this.varD;
            	case " <=":
                    return this.varG<=this.varD;
            	case " >":
                    return this.varG>this.varD;
            	case " >=":
                    return this.varG>=this.varD;
            	case " ==":
                    return this.varG==this.varD;
            	}
            	return false;
            }
            
            boolean canExecute(){
                int i=variableD.getText().length();
                if(i==0) variableD.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else variableD.setBorder(null);
                return super.canExecute() && i!=0;
            }

            void execute(boolean executeNext){
            	this.varD=Integer.parseInt(this.variableD.getText());
            	if(evaluate(this.op)) super.execute(false);//execute Command apres hookH
                this.hookH.execute(true);
            }
        }//fin classe interne If
        
        
        class CommandFunctionInit extends CommandWithCommands implements MouseListener{
            protected JLabel nameFunction;
            private CustomJButton changeName=new CustomJButton("", null, true);//pop up pour changer
            private LinkedList<CommandFunctionCall> caller=new LinkedList<CommandFunctionCall>();
            
            CommandFunctionInit(String name, int x, int y){
                super(name, new Color(212, 115, 212), x, y);
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  "));//pour la presentation
                
                try{
                    Image img=ImageIO.read(new File("images/engrenage.png"));
                    changeName.addImage(img);
                    changeName.setBackground(new Color(212, 115, 212));
                }
                catch(IOException e){}
                changeName.setPreferredSize(new Dimension(commandH-10, commandH-10));
                changeName.addActionListener((event)->{
                    String name=JOptionPane.showInputDialog("New name ?");
                    if(name!=null) resize(name);
                });
                this.add(changeName);
                
                nameFunction=new JLabel("  "+name+"  ");
                this.add(nameFunction);
            }
            
            void resize(String name){
                this.nameFunction.setText("  "+name+"  ");
                this.setSize(this.getPreferredSize().width, commandH);
                for(CommandFunctionCall c : caller) c.initializeDisplay();
            }
            
            void newDrag(){//override : pas de previous ou next au hookH
                if(inWhiteBoard() && !commands.contains(this)){
                    commands.add(this);
                    addCommandCall(this, getPositionY(this.name));//pour generer bloc d appel
                    SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
                }
            }

            void stick(){}//ne peut pas etre stick aux autres
            
            void execute(boolean executeNext){
                this.next.execute(true);
            }
        }


        /*****************
        *   Draw class   *
        *****************/

        class CommandDrawLine extends Command{//classe interne
            private JTextField distance=new JTextField(3);

            CommandDrawLine(int x, int y){
                super("drawLine", Color.CYAN.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Draw a line of  "));
                this.add(distance);
                this.add(new JLabel("  "));//pour la presentation
            }
            
            boolean canExecute(){
                int i=distance.getText().length();
                if(i==0) distance.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else distance.setBorder(null);
                return i!=0;
            }

            void execute(boolean executeNext){
                int hypotenuse=Integer.parseInt(distance.getText());
                Vector v=new Vector();//pour creer objet interne
                Point p=v.destinationLine(blackBoard.x, blackBoard.y, blackBoard.angle, hypotenuse);
                
                //ajout du vecteur dans le dessin du joueur
                if(blackBoard.drawing){
                    Vector.VectorLine trait=v.new VectorLine(blackBoard.x,
                        blackBoard.y, p.x, p.y, blackBoard.brushColor);
                    level.addToDraw(trait);
                }
                
                //nouvel emplacement du pinceau
                blackBoard.x=p.x;
                blackBoard.y=p.y;
                ViewPlaying.this.blackBoard.repaint();
                
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin classe interne DrawLine


        class CommandDrawArc extends Command{//classe interne
            private JTextField radius=new JTextField(3),angleScan=new JTextField(3);//hauteur==largeur, angle scane
            private JComboBox rightLeft=new JComboBox();
            private int sens=1;//1=gauche, -1=droite
            
            CommandDrawArc(int x, int y){
                super("drawArc", Color.CYAN.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Draw an arc with a radius of  "));
                this.add(this.radius);
                this.add(new JLabel("  and an angle of  "));
                this.add(this.angleScan);
                this.add(new JLabel("  on the  "));
                
                rightLeft.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED) sens*=-1;
                    }
                });
                rightLeft.addItem(" right ");
                rightLeft.addItem(" left ");
                this.add(rightLeft);
                
                this.add(new JLabel("  "));//pour la presentation
            }
            
            boolean canExecute(){
                int r=radius.getText().length();
                if(r==0) radius.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else radius.setBorder(null);
                int a=angleScan.getText().length();
                if(a==0) angleScan.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else angleScan.setBorder(null);
                return r!=0 && a!=0;
            }

            void execute(boolean executeNext){
                int rad=Integer.parseInt(radius.getText());
                int angleS=Integer.parseInt(angleScan.getText());
                Vector v=new Vector();
                Point center=v.destinationLine(blackBoard.x, blackBoard.y, 180+blackBoard.angle, rad);//milieu du cercle
                Point origin=v.destinationLine(center.x, center.y, blackBoard.angle-sens*90, rad);//-90 pour gauche, +90 pour droite
                Point translation=new Point(blackBoard.x-origin.x, blackBoard.y-origin.y);
                
                //ajout du vecteur dans le dessin du joueur
                if(blackBoard.drawing){
                    Point square1=v.destinationLine(center.x, center.y, 90, rad);//haut du carre
                    square1=v.destinationLine(square1.x, square1.y, 180, rad);//coin gauche du carre
                    Point square2=new Point(square1.x+translation.x, square1.y+translation.y);//carre translate
                    Vector.VectorArc arc=v.new VectorArc(square2.x, square2.y, rad*2, 
                        blackBoard.angle-90*sens, sens*angleS, blackBoard.brushColor);//-90*sens car translation
                    level.addToDraw(arc);
                }
                
                //nouvel emplacement du pinceau
                Point dest=v.destinationLine(center.x, center.y, blackBoard.angle+(angleS-90)*sens, rad);
                blackBoard.x=dest.x+translation.x;
                blackBoard.y=dest.y+translation.y;
                blackBoard.angle=(angleS*sens+blackBoard.angle)%360;
                ViewPlaying.this.blackBoard.repaint();
                
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin classe interne DrawArc


        class CommandRaisePutBrush extends Command{//classe interne
            private JComboBox choiceBox=new JComboBox();
            private boolean choiceRes=true;//raise=false, put=true
            
            CommandRaisePutBrush(int x, int y){
                super("raisePutBrush", Color.LIGHT_GRAY.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  "));//pour la presentation
                
                choiceBox.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED) choiceRes=!choiceRes;
                    }
                });
                choiceBox.addItem(" Raise ");
                choiceBox.addItem(" Put ");
                this.add(choiceBox);
                
                this.add(new JLabel("  the pen  "));
            }
            
            boolean canExecute(){
                return true;
            }

            void execute(boolean executeNext){
                blackBoard.drawing=choiceRes;
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin classe interne RaisePutBrush


        class CommandSetAngle extends Command{//classe interne
            JTextField angle=new JTextField(3);//meilleur moyen de choisir l angle ? --------a faire--------

            CommandSetAngle(int x, int y){
                super("setAngle", Color.LIGHT_GRAY.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Set angle to  "));
                this.add(this.angle);
                this.add(new JLabel("  "));
            }
            
            boolean canExecute(){
                int a=angle.getText().length();
                if(a==0) angle.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else angle.setBorder(null);
                return a!=0;
            }

            void execute(boolean executeNext){
                blackBoard.angle=(Integer.parseInt(angle.getText()));
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne ShiftAngle


        class CommandSetColor extends Command{//classe interne
            private JComboBox colorChoice=new JComboBox();
            final Color[] palette={Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.RED,Color.WHITE,Color.YELLOW};
            private Color colorRes=level.brushFirstColor;

            CommandSetColor(int x, int y){
                super("setColor", Color.LIGHT_GRAY.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width+5, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Set color to  "));
                
                colorChoice.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED){
                            for(int i=0; i<7; i++){
                                if(palette[i].equals(colorChoice.getSelectedItem())){
                                    colorRes=palette[i];
                                    return;
                                }
                            }
                        }
                    }
                });
                for(int i=0; i<7; i++) colorChoice.addItem(palette[i]);
                colorChoice.setRenderer(new ColorComboRenderer());
                this.add(this.colorChoice);
                
                this.add(new JLabel("  "));
            }
            
            boolean canExecute(){
                return true;
            }

            void execute(boolean executeNext){
                blackBoard.brushColor=colorRes;
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
            
            
            class ColorComboRenderer extends JPanel implements ListCellRenderer{
                Color main=Color.BLUE;//couleur en tete d affichage
                
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
            }//fin de classe interne interne ColorComboRenderer
        }//fin de classe interne ChangeColor


        class CommandMoveTo extends Command{//classe interne
            private JTextField positionX=new JTextField(3), positionY=new JTextField(3);

            CommandMoveTo(int x, int y){
                super("moveTo", Color.LIGHT_GRAY.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Move pen to (  "));
                this.add(this.positionX);
                this.add(new JLabel("  ,  "));
                this.add(this.positionY);
                this.add(new JLabel("  )  "));
            }
            
            boolean canExecute(){
                int x=positionX.getText().length();
                if(x==0) positionX.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else positionX.setBorder(null);
                int y=positionY.getText().length();
                if(y==0) positionY.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else positionY.setBorder(null);
                return x!=0 && y!=0;
            }

            void execute(boolean executeNext){
                blackBoard.x=(Integer.parseInt(positionX.getText()));
                blackBoard.y=(Integer.parseInt(positionY.getText()));
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne MoveTo


        class CommandAddAngle extends Command{//classe interne
            private JTextField angle=new JTextField(3);

            CommandAddAngle(int x, int y){
                super("addAngle", Color.LIGHT_GRAY.darker());
                initializeDisplay();
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                this.add(new JLabel("  Add  "));
                this.add(this.angle);
                this.add(new JLabel("  to angle  "));
            }
            
            boolean canExecute(){
                int a=angle.getText().length();
                if(a==0) angle.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else angle.setBorder(null);
                return a!=0;
            }

            void execute(boolean executeNext){
                blackBoard.angle+=(Integer.parseInt(angle.getText()));
                blackBoard.angle%=360;
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne AddAngle


        //autre classe de dessin...
    }//fin classe interne PanelDragDropBoard
}