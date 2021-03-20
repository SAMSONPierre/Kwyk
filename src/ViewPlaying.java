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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;//fusion de WhiteBoard et CommandBoard
    private JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    private Level level;//niveau en cours
    private JProgressBar limite;
    private HashMap<String, Integer> variables=new HashMap<String, Integer>();//nom, valeur
    
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
        else{//limite des commandes si on est dans un niveau
            limite=new JProgressBar(0, level.numberOfCommands){
                public String getString(){//presentation apparente
                    return getValue()+"/"+level.numberOfCommands;
                }
            };
            limite.setStringPainted(true);
            features.add(limite);
        }
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
    
    int[] getNumbersFromHead(){//commandes, fonctions, variables
        int[] res={dragDrop.getNumbersFromHead()[0], dragDrop.getNumbersFromHead()[1], variables.size()};
        return res;
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
        private int x, y, angle;//par defaut (0,0) et orientee "->" (angle 0° sur le cercle trigo)
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
    
    public class PanelDragDropBoard extends JPanel implements MouseWheelListener{
        final int x00, y00, width, height;//position initiale, largeur, hauteur du panel
        private LinkedList<Command> commands=new LinkedList<Command>();//liste de commandes ayant ete drag sur whiteBoard
        private Command brightC;//commande allumee
        private LinkedList<NumberField> fields=new LinkedList<NumberField>();
        private NumberField brightF;//field allume
        private int lastPositionY=20, deltaY=0;//emplacement vertical des commandes, mouvement de la roulette
        private int[] numberOfFV=new int[2];//fonction, varibale
        private Bin bin;
        private JLabel errorName=new JLabel("Please enter a valid name :");
        
        PanelDragDropBoard() throws IOException{
            this.x00=440;
            this.y00=20+buttonHeight+ViewPlaying.this.getInsets().top;
            width=widthFS-460;//460=3 marges de colonne + taille blackBoard
            height=heightFS-40-buttonHeight;//40=marges haut+bas
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            this.setLayout(null);
            this.setBounds(x00, y00-ViewPlaying.this.getInsets().top, width, height);
            this.addMouseWheelListener(this);
            
            this.bin=new Bin();
            this.add(bin);
            this.setFunctionVariableButton();
            this.addAvailableCommands();
            errorName.setForeground(Color.RED);
        }
        
        void setFunctionVariableButton(){
            int x=width/2-20;
            if(level.numberOfFunctions!=0){
                JButton createF=new JButton("Create a new function");
                Dimension dF=createF.getPreferredSize();
                createF.setBounds(x-dF.width, 20, dF.width, dF.height);
                createF.addActionListener((event)->{
                    String name=JOptionPane.showInputDialog("Name of this fonction ?");
                    if(name==null) return;
                    while(name!=null && (name.equals("") || nameFunAlreadySet(name)))
                        name=JOptionPane.showInputDialog(errorName);
                    if(name!=null) addFunction(name, x-dF.width, 40+2*dF.height);
                    if(numberOfFV[0]==level.numberOfFunctions){//max atteint
                        createF.setEnabled(false);
                        createF.setBackground(Color.red);
                    }
                });
                this.add(createF);
            }
            if(level.numberOfVariables!=0){
                JButton createV=new JButton("Create a new variable");
                createV.addActionListener((event)->{
                    String name=JOptionPane.showInputDialog("Name of this variable ?");
                    if(name==null) return;
                    while(name!=null && (name.equals("") || variables.containsKey(name)))
                        name=JOptionPane.showInputDialog(errorName);
                    if(name!=null) addVariable(name);
                    if(numberOfFV[1]==level.numberOfVariables){//max atteint
                        createV.setEnabled(false);
                        createV.setBackground(Color.red);
                    }
                });
                Dimension dV=createV.getPreferredSize();
                createV.setBounds(x-dV.width, 30+dV.height, dV.width, dV.height);
                this.add(createV);
            }
        }
        
        boolean nameFunAlreadySet(String name){
            name="  "+name+"  ";//comme dans les labels
            for(Command c : commands){
                if(c instanceof CommandFunctionInit && ((CommandFunctionInit)c).nameFunction.getText().equals(name))
                    return true;
            }
            return false;
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);//WhiteBoard a gauche
            g2.fillRect(0, 0, width/2, height);
            g2.setColor(Color.LIGHT_GRAY);//CommandBoard a droite
            g2.fillRect(width, 0, width/2, height);
        }

        public void mouseWheelMoved(MouseWheelEvent e){
            int addToY=(e.getWheelRotation()<0?1:-1)*e.getScrollAmount()*10;
            if(e.getX()<this.getX()+width/2-x00){//whiteBoard
                for(Component c : ViewPlaying.this.dragDrop.getComponents()){
                    if(commands.contains(c) && c instanceof Command && !(c instanceof HookHorizontal)
                    || (c instanceof Variable && !((Variable)c).lastCreated))
                        c.setLocation(c.getX(), c.getY()+addToY);
                }
            }
            else{//commandBoard
                deltaY+=addToY;
                for(Component c : ViewPlaying.this.dragDrop.getComponents()){
                    if(!commands.contains(c) && c instanceof Command && !(c instanceof HookHorizontal)
                    || (c instanceof Variable && ((Variable)c).lastCreated))
                        c.setLocation(c.getX(), c.getY()+addToY);
                }
            }
        }
        
        
        /********************
        *   Gestion Blocs   *
        ********************/
        
        void addAvailableCommands(){//premier ajout des commandes disponibles
            //ajout dans WhiteBoard
            commands.add(new CommandStart());//toujours le premier de la liste de commandes
            this.add(commands.getFirst());
            //ajout dans CommandBoard
            for(String c : level.getAvailableCommands())
                lastPositionY+=addCommand(c, lastPositionY);
        }
        
        void addFunction(String name, int x, int y){//nouvelle fonction, ajout dans WhiteBoard
            numberOfFV[0]++;
            CommandFunctionInit fC=new CommandFunctionInit(name, x, y);
            this.add(fC);
            this.add(fC.hookV);
            this.add(fC.hookH);
            lastPositionY+=addCommandCall(fC, lastPositionY);
            commands.add(fC);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        int addCommandCall(CommandFunctionInit init, int positionY){
            CommandFunctionCall callC=new CommandFunctionCall(init, width/2+20, positionY);
            this.add(callC);
            init.caller.add(callC);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
            return callC.getHeight()+10;
        }
        
        void addVariable(String name){//nouvelle variable
            if(numberOfFV[1]++==0){
                for(NumberField f : fields) f.setBorder(f.border);
                lastPositionY+=addCommand("addition", lastPositionY);
            }
            Variable variable=new Variable(name, width/2+20, lastPositionY);
            this.add(variable);
            lastPositionY+=variable.getHeight()+10;
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        void addSameVariable(Variable origin){//regeneration des variables
            Variable var=new Variable(origin.name, width/2+20, origin.positionY);
            this.add(var);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        int addCommand(String name, int positionY){//(re)generation des commandes
            int res=10;
            switch(name){
                case "for":
                    CommandFor forC=new CommandFor(width/2+20, positionY);
                    this.add(forC);
                    this.add(forC.hookV);
                    this.add(forC.hookH);
                    res+=forC.getHeight()+forC.hookV.getHeight()+forC.hookH.getHeight();
                    break;
                case "if":
                    CommandIf ifC=new CommandIf(width/2+20, positionY);
                    this.add(ifC);
                    this.add(ifC.hookV);
                    this.add(ifC.hookH);
                    res+=ifC.getHeight()+ifC.hookV.getHeight()+ifC.hookH.getHeight();
                    break;
                case "while":
                    CommandWhile whileC=new CommandWhile(width/2+20, positionY);
                    this.add(whileC);
                    this.add(whileC.hookV);
                    this.add(whileC.hookH);
                    res+=whileC.getHeight()+whileC.hookV.getHeight()+whileC.hookH.getHeight();
                    break;
                case "drawLine":
                    CommandDrawLine drawLineC=new CommandDrawLine(width/2+20, positionY);
                    this.add(drawLineC);
                    res+=drawLineC.getHeight();
                    break;
                case "drawArc":
                    CommandDrawArc drawArcC=new CommandDrawArc(width/2+20, positionY);
                    this.add(drawArcC);
                    res+=drawArcC.getHeight();
                    break;
                case "raisePutBrush":
                    CommandRaisePutBrush raisePutC=new CommandRaisePutBrush(width/2+20, positionY);
                    this.add(raisePutC);
                    res+=raisePutC.getHeight();
                    break;
                case "moveTo":
                    CommandMoveTo moveC=new CommandMoveTo(width/2+20, positionY);
                    this.add(moveC);
                    res+=moveC.getHeight();
                    break;
                case "setAngle":
                    CommandSetAngle shiftAngleC=new CommandSetAngle(width/2+20, positionY);
                    this.add(shiftAngleC);
                    res+=shiftAngleC.getHeight();
                    break;
                case "addAngle":
                    CommandAddAngle addAngleC=new CommandAddAngle(width/2+20, positionY);
                    this.add(addAngleC);
                    res+=addAngleC.getHeight();
                    break;
                case "setColor":
                    CommandSetColor changeColorC=new CommandSetColor(width/2+20, positionY);
                    this.add(changeColorC);
                    res+=changeColorC.getHeight();
                    break;
                case "shiftColor":
                    CommandShiftColor shiftColorC=new CommandShiftColor(width/2+20, positionY);
                    this.add(shiftColorC);
                    res+=shiftColorC.getHeight();
                    break;
                case "addition":
                    CommandAddition addC=new CommandAddition(width/2+20, positionY);
                    this.add(addC);
                    res+=addC.getHeight();
                    break;
                default :
                    return 0;
            }
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
            return res;
        }
        
        
        /*******************
        *   Create Level   *
        *******************/
        
        int[] getNumbersFromHead(){//nombre de commandes/fonctions depuis Start
            int[] res=new int[2];//commandes, fonctions
            Command tmp=this.commands.getFirst().next;//deuxieme commande du code du joueur
            LinkedList<String> functions=new LinkedList<String>();
            while(tmp!=null){
                if(tmp instanceof CommandFunctionCall){//nouvelle fonction
                    String name=((CommandFunctionCall)tmp).name.getText();
                    if(!functions.contains(name)){
                        functions.add(name);
                        res[1]++;
                        Command interne=((CommandFunctionCall)tmp).function.next;
                        if(interne!=null) res[0]+=interne.getNumberFromThis();
                    }
                }
                else if(!(tmp instanceof CommandWithCommands)) res[0]++;//un cwc a toujours un hookH donc 2 commandes
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
        
        LinkedList<Command> getFunctionInterne(CommandFunctionInit init){
            LinkedList<Command> res=new LinkedList<Command>();
            Command tmp=init.next;
            while(tmp!=null){
                if(!(tmp instanceof HookHorizontal)) res.add(tmp);
                tmp=tmp.next;
            }
            return res;
        }
        
        LinkedList<Command> getCommands(){//les differentes commandes utilisees
            LinkedList<Command> res=new LinkedList<Command>();
            LinkedList<String> functions=new LinkedList<String>();
            Command tmp=this.commands.getFirst().next;
            while(tmp!=null){
                if(tmp instanceof CommandFunctionCall){//nouvelle fonction
                    CommandFunctionCall call=(CommandFunctionCall)tmp;
                    if(!functions.contains(call.name.getText())){
                        functions.add(call.name.getText());
                        for(Command c : getFunctionInterne(call.function)){
                            if(notAdd(res, c)) res.add(c);
                        }
                    }
                }
                else if(notAdd(res, tmp)) res.add(tmp);
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
            final Color color;
            final int commandH=35, commandW=70, positionY;//hauteur d une commande, largeur par defaut de hookH
            protected Command next, previous;//next a executer, previous pour ajuster l affichage
            private int mouseX, mouseY;//position initiale de la souris au moment du drag
            protected NumberField input;
            
            Command(String name, Color color, int positionY){
                this.name=name;
                this.color=color;
                this.positionY=positionY;
                this.setBackground(color);
                this.setLayout(new GridBagLayout());//pour centrer verticalement les textes
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
            }
            
            abstract boolean canExecute();
            abstract void execute(boolean executeNext);//chaque fonction les implemente
            
            
            /*****************
            * Delete Command *
            *****************/
            
            boolean toDelete(){//quand pres de la poubelle
                if(!(this instanceof CommandFunctionInit)){//ne supprime pas initialisateur de fonction
                    if(bin.getLocation().y>getLocation().y+getHeight()) return false;
                    if(bin.getLocation().y+bin.getHeight()<getLocation().y) return false;
                    if(bin.getLocation().x>getLocation().x+getWidth()) return false;
                    if(bin.getLocation().x+bin.getWidth()<getLocation().x) return false;
                    return true;
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
                if(commands.contains(this)){//sur whiteBoard
                    commands.remove(this);
                    if(input!=null){//variable
                        if(input.variable!=null) input.variable.deleteSteps();
                        else fields.remove(input);
                        if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                            NumberField field=(this instanceof CommandMoveTo)?
                                ((CommandMoveTo)this).positionY:((CommandDrawArc)this).angleScan;
                            Variable var=field.variable;
                            if(var!=null) var.deleteSteps();
                            else fields.remove(field);
                        }
                    }
                }
                else if(this instanceof CommandFunctionCall){
                    CommandFunctionCall tmp=(CommandFunctionCall)this;
                    addCommandCall(tmp.function, tmp.positionY);
                }
                else addCommand(this.name, this.positionY);
                bin.loadBin("images/closedBin.png");
                SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
                if(this.next!=null) this.next.deleteSteps();
            }
            
            
            /****************************
            *  Regeneration of Command  *
            ****************************/
            
            void newDrag(){//nouvelle commande qu on drag pour la premiere fois
                if(inWhiteBoard() && !commands.contains(this)){//premier drag sur whiteBoard
                    commands.add(this);
                    if(this instanceof CommandWithCommands) commands.add(this.next);//ajout de HookH aussi
                    if(level.numberOfVariables!=0 && input!=null){
                        fields.add(input);
                        if(numberOfFV[1]!=0) input.setBorder(input.border);
                    }
                    if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                        NumberField tmp=(this instanceof CommandDrawArc)?
                            ((CommandDrawArc)this).angleScan:((CommandMoveTo)this).positionY;
                        fields.add(tmp);
                        if(numberOfFV[1]!=0) tmp.setBorder(tmp.border);
                    }
                    //pour regenerer commande utilisee :
                    if(this instanceof CommandFunctionCall)
                        addCommandCall(((CommandFunctionCall)this).function, positionY);
                    else addCommand(this.name, this.positionY);
                }
            }

            boolean inWhiteBoard(){//est dans whiteBoard
                Point p=this.getLocation();
                return (p.x>0 && p.x<width/2 && p.y>0 && p.y<height);
            }

            
            /*****************
            * Stick together *
            *****************/
            
            void foundPrevious(){//reformation des liens previous/next
                if(brightC!=null){//s il existe
                    brightC.setNext(this);
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
                    stickVarToForeground();
                }
                if(this.next!=null) this.next.stick();
            }

            int closeCommand(){//this et c sont assez proches pour se coller
                if(!(this instanceof CommandFunctionInit)){//initialisateur de fonction sans previous
                    int i=0;
                    for(Command c : commands){
                        if(c instanceof CommandWithCommands){
                            if(closeHeight(c) && closeWidthIntern((CommandWithCommands)c)) return i;
                        }
                        else if(closeHeight(c) && closeWidth(c)) return i;
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
            

            /*****************
            *  Presentation  *
            *****************/
            
            void setToForeground(){//this a l avant-plan quand on le drag==tete de liste
                if(this instanceof CommandWithCommands){
                    CommandWithCommands tmp=(CommandWithCommands)this;
                    ViewPlaying.this.dragDrop.remove(tmp.hookV);
                    ViewPlaying.this.dragDrop.add(tmp.hookV, 0);
                }
                ViewPlaying.this.dragDrop.remove(this);
                ViewPlaying.this.dragDrop.add(this, 0);
                stickVarToForeground();
                if(this.next!=null) this.next.setToForeground();//appel recursif
            }
            
            void stickVarToForeground(){
                if(input!=null && input.variable!=null){
                    input.variable.stick(false);
                    input.variable.setToForeground();
                }
                if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                    NumberField f=(this instanceof CommandDrawArc)?
                        ((CommandDrawArc)this).angleScan:((CommandMoveTo)this).positionY;
                    if(f.variable!=null){
                        f.variable.stick(false);
                        f.variable.setToForeground();
                    }
                }
            }
            
            void switchOff(){//eteint le seul bloc a eteindre
                if(brightC!=null){
                    brightC.setBackground(brightC.color);//pour eviter decalage brighter/darker
                    brightC=null;
                }
            }

            /*****************
            * Mouse Override *
            *****************/
            
            int getNumberFromThis(){//nombre de commandes qu on drag
                int res=0;
                Command tmp=this;
                while(tmp!=null){
                    if(tmp instanceof CommandFunctionCall){
                        res++;//pour functionCall
                        Command interne=((CommandFunctionCall)tmp).function.next;
                        while(interne!=null){
                            if(!(interne instanceof HookHorizontal)) res++;
                            interne=interne.next;
                        }
                    }
                    else if(!(tmp instanceof CommandWithCommands)) res++;
                    tmp=tmp.next;
                }
                return res;
            }
            
            public void mousePressed(MouseEvent e){
                unStick();//detache si a un precedent
                mouseX=e.getX();//position initiale de souris
                mouseY=e.getY();
                this.setToForeground();//mettre ce qu on drag a l avant-plan
            }

            public void mouseDragged(MouseEvent e){
                //drag this et ses next + variables
                int x=e.getXOnScreen()-x00-mouseX-ViewPlaying.this.getX();
                int y=e.getYOnScreen()-y00-mouseY-ViewPlaying.this.getY();
                this.setLocation(x, y);
                stickVarToForeground();
                if(this.next!=null) this.next.stick();
                
                //allume et eteint les blocs selon les cas
                switchOff();//eteint tout d abord
                int nearby=closeCommand();
                if(nearby!=-1){//proche d un bloc
                    if(limite==null || getNumberFromThis()+getNumbersFromHead()[0]<=level.numberOfCommands){//attachable
                        brightC=commands.get(nearby);//allume le seul bloc a allumer
                        brightC.setBackground(brightC.getBackground().brighter());
                    }
                }
                
                //ouvre eventuellement la poubelle
                try{
                    updateBinState();
                }
                catch(IOException e1){
                    System.out.println("Couldn't update bin");
                }
            }

            public void mouseReleased(MouseEvent e){
                if(this.toDelete()){
                    try{
                        this.deleteSteps();
                    }
                    catch(IOException e1){
                        System.out.println("Couldn't delete command");
                    }
                }
                else{
                    newDrag();
                    if(brightC!=null) foundPrevious();
                    switchOff();//eteint tout
                }
                if(limite!=null) limite.setValue(getNumbersFromHead()[0]);
            }
            
            public void mouseMoved(MouseEvent e){}
            public void mouseClicked(MouseEvent e){//pour verification, a enlever apres
                //this.execute(false);
            }
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne interne Command
        

        class CommandStart extends Command{//bloc initial present que sur WhiteBoard
            CommandStart(){
                super("start", new Color(0, 204, 102), 20);
                
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
                super("Call", new Color(212, 115, 212), y);
                this.function=function;
                initializeDisplay();
                this.add(name);
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            void initializeDisplay(){
                name.setText(function.nameFunction.getText());
                setSize(getPreferredSize().width, commandH);
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
                super(name, color, y);
                hookV=new HookVertical(color, commandH, x, y+commandH+deltaY);//apres la commande
                hookH=new HookHorizontal(color, x, y+2*commandH+deltaY);//apres hookV
                this.next=hookH;
                hookH.previous=this;
            }
            
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
                super("hookHorizontal", color, y);
                this.setBounds(x, y, commandW, commandH/2);
            }
            
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
            CommandFor(int x, int y){
                super("for", new Color(230, 138, 0), x, y);
                super.input=new NumberField(this);//nombre de repetition a saisir
                
                this.add(new JLabel("  Repeat  "));
                this.add(input);
                this.add(new JLabel("  time  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int i=input.getText().length();
                if(i==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return super.canExecute() && i!=0;
            }

            void execute(boolean executeNext){
                for(int i=0; i<input.getNumber(); i++) super.execute(false);
                this.hookH.execute(true);
            }
        }//fin classe interne For


        class CommandIf extends CommandWithCommands{//classe interne
            private JComboBox variableG=new JComboBox(), operateur=new JComboBox();//a priori seulement deux listes deroulantes
            private String op="<";//par defaut
            private int varG=blackBoard.x;
            //e.g x<100 <=> variableG(varG)="x", operateur="<", variableD(varD)="100"
            
            CommandIf(int x, int y){
                super("if", new Color(179, 0, 89), x, y);
                super.input=new NumberField(this);//choix libre du joueur donc pas une liste
                
                this.variableG.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==variableG)
                            varG=variableG.getSelectedItem().equals("  x")?blackBoard.x:variableG.getSelectedItem().equals("  y")?blackBoard.y:blackBoard.angle;
                    }
                });
                this.variableG.addItem("  x");
                this.variableG.addItem("  y");
                this.variableG.addItem(" angle");

                this.operateur.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==operateur) op=(String)operateur.getSelectedItem();
                    }
                });
                String[] tmpOp={"  <", "  >", " <=", " >=", " =="};
                for(String s : tmpOp) this.operateur.addItem(s);

                this.add(new JLabel("  If  "));
                this.add(variableG);
                this.add(operateur);
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }

            boolean evaluate(String op){
            	switch(op){
                    case "  <" : return this.varG<input.getNumber();
                    case " <=" : return this.varG<=input.getNumber();
                    case "  >" : return this.varG>input.getNumber();
                    case " >=" : return this.varG>=input.getNumber();
                    case " ==" : return this.varG==input.getNumber();
            	}
            	return false;
            }
            
            boolean canExecute(){
                int i=input.getText().length();
                if(i==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return super.canExecute() && i!=0;
            }

            void execute(boolean executeNext){
            	if(evaluate(this.op)) super.execute(false);//execute Command apres hookH
                this.hookH.execute(true);
            }
        }//fin classe interne If
        
        
        class CommandWhile extends CommandWithCommands{//classe interne
            private JComboBox variableG=new JComboBox(), operateur=new JComboBox();
            private String op="<";
            private int varG=blackBoard.x, whatIsVarG=0;//pour savoir quelle valeur devra etre actualisee ; x<=>0, y<=>1, angle<=>2
            
            CommandWhile(int x, int y){
                super("while", new Color(204, 102, 102), x, y);
                super.input=new NumberField(this);

                this.variableG.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==variableG) {
                            varG=variableG.getSelectedItem().equals("  x")?blackBoard.x:variableG.getSelectedItem().equals("  y")?blackBoard.y:blackBoard.angle;
                            whatIsVarG=variableG.getSelectedItem().equals("  x")?0:variableG.getSelectedItem().equals("  y")?1:2;
                        }
                    }
                });
                this.variableG.addItem("  x");
                this.variableG.addItem("  y");
                this.variableG.addItem(" angle");

                this.operateur.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getSource()==operateur) op=(String)operateur.getSelectedItem();
                    }
                });
                String[] tmpOp={"  <", "  >", " <=", " >=", " =="};
                for(String s : tmpOp) this.operateur.addItem(s);

                this.add(new JLabel("  While  "));
                this.add(variableG);
                this.add(operateur);
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }

            boolean evaluate(String op){
            	switch(op){
                    case "  <" : return this.varG<input.getNumber();
                    case " <=" : return this.varG<=input.getNumber();
                    case "  >" : return this.varG>input.getNumber();
                    case " >=" : return this.varG>=input.getNumber();
                    case " ==" : return this.varG==input.getNumber();
            	}
            	return false;
            }
            
            boolean canExecute(){
                int i=input.getText().length();
                if(i==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return super.canExecute() && i!=0;
            }

            void execute(boolean executeNext){
            	int varG_initial=varG;
            	int limit=3000;//pour simuler la terminaison
            	while(evaluate(this.op) && limit>0){
                    super.execute(false);
                    this.varG=whatIsVarG==0?blackBoard.x:whatIsVarG==1?blackBoard.y:blackBoard.angle;
                    limit--;
            	}
            	if(limit==0) error(true);//si la terminaison a du etre simulee
            	else{
                    error(false);
                    this.hookH.execute(true);
            	}
                this.varG=varG_initial;
            }
            
            private void error(boolean b){
            	if(b){
                    this.setBackground(Color.RED.darker());
                    this.hookH.setBackground(Color.RED.darker());
                    this.hookV.setBackground(Color.RED.darker());
            	}
            	else{
                    this.setBackground(color);
                    this.hookH.setBackground(color);
                    this.hookV.setBackground(color);
            	}
            }
        }//fin classe interne While
        
        
        class CommandFunctionInit extends CommandWithCommands implements MouseListener{
            protected JLabel nameFunction;
            private CustomJButton changeName=new CustomJButton("", null, true);//pop up pour changer
            private LinkedList<CommandFunctionCall> caller=new LinkedList<CommandFunctionCall>();
            
            CommandFunctionInit(String name, int x, int y){
                super("function", new Color(212, 115, 212), x, y);
                
                try{
                    Image img=ImageIO.read(new File("images/engrenage.png"));
                    changeName.addImage(img);
                    changeName.setBackground(new Color(212, 115, 212));
                }
                catch(IOException e){}
                changeName.setPreferredSize(new Dimension(commandH-10, commandH-10));
                changeName.addActionListener((event)->{
                    String newName=JOptionPane.showInputDialog("New name ?");
                    if(newName!=null) resize(newName);
                });
                
                this.add(new JLabel("  "));//pour la presentation
                this.add(changeName);
                nameFunction=new JLabel("  "+name+"  ");
                this.add(nameFunction);
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void resize(String newName){
                this.nameFunction.setText("  "+newName+"  ");
                this.setSize(this.getPreferredSize().width, commandH);
                for(CommandFunctionCall c : caller) c.initializeDisplay();
            }
            
            void newDrag(){}//deja initialisee sur WhiteBoard
            void stick(){}//ne peut pas etre stick aux autres
            
            void execute(boolean executeNext){
                this.next.execute(true);
            }
        }


        /*****************
        *   Draw class   *
        *****************/

        class CommandDrawLine extends Command{//classe interne

            CommandDrawLine(int x, int y){
                super("drawLine", Color.CYAN.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Draw a line of  "));
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int i=input.getText().length();
                if(i==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return i!=0;
            }

            void execute(boolean executeNext){
                int hypotenuse=input.getNumber();
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
            private NumberField angleScan=new NumberField(this);//hauteur==largeur, angle scane
            private JComboBox rightLeft=new JComboBox();
            private int sens=1;//1=gauche, -1=droite
            
            CommandDrawArc(int x, int y){
                super("drawArc", Color.CYAN.darker(), y);
                super.input=new NumberField(this);//radius
                
                rightLeft.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED) sens*=-1;
                    }
                });
                rightLeft.addItem(" right ");
                rightLeft.addItem(" left ");
                
                this.add(new JLabel("  Draw an arc with a radius of  "));
                this.add(input);
                this.add(new JLabel("  and an angle of  "));
                this.add(this.angleScan);
                this.add(new JLabel("  on the  "));
                this.add(rightLeft);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int r=input.getText().length();
                if(r==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                int a=angleScan.getText().length();
                if(a==0) angleScan.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else angleScan.setBorder(null);
                return r!=0 && a!=0;
            }

            void execute(boolean executeNext){
                int rad=(input.getNumber()>=0)?input.getNumber():0;
                int angleS=angleScan.getNumber();
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
                super("raisePutBrush", Color.LIGHT_GRAY.darker(), y);
                
                choiceBox.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED) choiceRes=!choiceRes;
                    }
                });
                choiceBox.addItem(" Raise ");
                choiceBox.addItem(" Put ");
                
                this.add(new JLabel("  "));//pour la presentation
                this.add(choiceBox);
                this.add(new JLabel("  the pen  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
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
            CommandSetAngle(int x, int y){
                super("setAngle", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Set angle to  "));
                this.add(this.input);
                this.add(new JLabel("  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int a=input.getText().length();
                if(a==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return a!=0;
            }

            void execute(boolean executeNext){
                blackBoard.angle=input.getNumber();
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
                super("setColor", Color.LIGHT_GRAY.darker(), y);
                
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
                
                this.add(new JLabel("  Set color to  "));
                this.add(this.colorChoice);
                this.add(new JLabel("  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width+5, commandH);
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
            private NumberField positionY=new NumberField(this);

            CommandMoveTo(int x, int y){
                super("moveTo", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);//positionX
                
                this.add(new JLabel("  Move pen to (  "));
                this.add(this.input);
                this.add(new JLabel("  ,  "));
                this.add(this.positionY);
                this.add(new JLabel("  )  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int x=input.getText().length();
                if(x==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                int y=positionY.getText().length();
                if(y==0) positionY.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else positionY.setBorder(null);
                return x!=0 && y!=0;
            }

            void execute(boolean executeNext){
                blackBoard.x=(input.getNumber()>=0)?input.getNumber():0;
                blackBoard.y=(positionY.getNumber()>=0)?positionY.getNumber():0;
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne MoveTo


        class CommandAddAngle extends Command{//classe interne
            CommandAddAngle(int x, int y){
                super("addAngle", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Add  "));
                this.add(input);
                this.add(new JLabel("  to angle  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int a=input.getText().length();
                if(a==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return a!=0;
            }

            void execute(boolean executeNext){
                blackBoard.angle+=input.getNumber();
                blackBoard.angle%=360;
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne AddAngle
        

        class CommandShiftColor extends Command{
            CommandShiftColor(int x, int y){
                super("shiftColor", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Add  "));
                this.add(this.input);
                this.add(new JLabel("  % to color  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                int n=input.getText().length();
                if(n==0) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(null);
                return n!=0;
            }
            
            int regularize(int n){
                if(n>255) return 255;
                if(n<0) return 0;
                return n;
            }

            void execute(boolean executeNext){
                int percent=(255*input.getNumber())/100;
                int nRed=regularize(blackBoard.brushColor.getRed()+percent);
                int nGreen=regularize(blackBoard.brushColor.getGreen()+percent);
                int nBlue=regularize(blackBoard.brushColor.getBlue()+percent);
                
                blackBoard.brushColor=new Color(nRed,nGreen,nBlue);
                ViewPlaying.this.blackBoard.repaint();
                if(executeNext && next!=null) next.execute(true);
                else if(next==null) victoryMessage();
            }
        }//fin de classe interne shiftColor


        class CommandAddition extends Command{//classe interne
            NumberField add=new NumberField(this);
            
            CommandAddition(int x, int y){
                super("addition", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  "));//presentation
                this.add(input);
                this.add(new JLabel("  +  "));
                this.add(add);
                this.add(new JLabel("  "));//presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                //
                return true;
            }

            void execute(boolean executeNext){
                variables.replace(input.variable.name, input.getNumber()+add.getNumber());
            }
        }//fin de classe interne Addition
        
        
        class NumberField extends JTextField{
            protected Variable variable;//varibale qu on lui stick
            Border border=BorderFactory.createLineBorder(new Color(255, 204, 255), 2);
            final Command container;

            NumberField(Command c){
                super();
                setPreferredSize(new Dimension(35, super.getPreferredSize().height));
                this.container=c;
            }

            int getNumber(){
                if(variable!=null) return variables.get(variable.name);
                String text=getText();
                try{
                    int res=Integer.parseInt(text);
                    return res;
                }
                catch(NumberFormatException e){}
                return 0;
            }
            
            void resize(boolean larger){//true=stick, false=unstick
                int diff=(variable.getWidth()-35)*(larger?1:-1);
                setPreferredSize(new Dimension((larger)?variable.getWidth():35, getHeight()));
                container.setBounds(container.getX(), container.getY(), container.getWidth()+diff, container.getHeight());
                SwingUtilities.updateComponentTreeUI(container);
            }

            protected Document createDefaultModel(){
                return new UpperCaseDocument();
            }

            class UpperCaseDocument extends PlainDocument{
                public void insertString(int offs, String s, AttributeSet a) throws BadLocationException{
                    if(variable!=null || s==null) return;
                    if(offs==0 && s.equals("-")) super.insertString(offs, s, a);//- en premier
                    else{
                        try{
                            Integer.parseInt(s);
                            super.insertString(offs, s, a);
                        }
                        catch(NumberFormatException e){}//pas un nombre
                    }
                }
            }
        }//fin classe interne NumberField


        /*******************
        *     Variable     *
        *******************/
        
        class Variable extends JPanel implements MouseInputListener{
            final String name;
            final int positionY;
            protected boolean lastCreated=true;//true=addSameVariable
            private NumberField linkedTo;
            private int mouseX, mouseY;
            
            Variable(String name, int x, int y){
                this.name=name;
                this.positionY=y;
                variables.put(name, 0);//met dans HashMap, par defaut variable=0
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
                
                this.setBackground(new Color(255, 204, 255));
                this.setLayout(new GridBagLayout());
                this.add(new JLabel("  "+name+"  "));
                NumberField tmp=new NumberField(null);//juste pour hauteur
                this.setBounds(x, y+deltaY, getPreferredSize().width, tmp.getPreferredSize().height);
            }
            
            
            /******************
            * Delete Variable *
            ******************/
            
            boolean toDelete(){//quand pres de la poubelle
                if(bin.getLocation().y>getLocation().y+getHeight()) return false;
                if(bin.getLocation().y+bin.getHeight()<getLocation().y) return false;
                if(bin.getLocation().x>getLocation().x+getWidth()) return false;
                if(bin.getLocation().x+bin.getWidth()<getLocation().x) return false;
                return true;
            }
            
            void updateBinState() throws IOException{
            	if(this.toDelete()) bin.loadBin("images/openBin.png");
            	else bin.loadBin("images/closedBin.png");
            }
            
            void deleteSteps() throws IOException{//enleve variable du panel
                PanelDragDropBoard.this.remove(this);
                fields.remove(linkedTo);
                if(lastCreated) addSameVariable(this);//regeneration
                bin.loadBin("images/closedBin.png");
                SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
            }

            
            /******************
            * Stick & Unstick *
            ******************/

            int closeCommand(){//this et NumberField sont assez proches pour se coller
                int i=0;
                for(NumberField f : fields){
                    if(f.variable==null && closeHeight(f) && closeWidth(f)) return i;
                    i++;
                }
                return -1;
            }

            boolean closeHeight(NumberField field){//distance entre haut de field et this
                int distance=getLocationOnScreen().y-field.getLocationOnScreen().y;
                return distance>-getHeight() && distance<getHeight();
            }
            
            boolean closeWidth(NumberField field){//distance entre cote gauche de this et celui de field
                int distance=getLocationOnScreen().x-field.getLocationOnScreen().x;
                return distance>-getWidth() && distance<field.getWidth();
            }
            
            void stick(boolean needResize){//colle this a son linkedTo
                if(needResize){
                    linkedTo=brightF;
                    linkedTo.variable=this;
                    linkedTo.resize(true);//aggrandit
                    switchOff();
                    SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);
                    SwingUtilities.updateComponentTreeUI(ViewPlaying.this);
                    SwingUtilities.updateComponentTreeUI(linkedTo.container);
                    SwingUtilities.updateComponentTreeUI(linkedTo);
                    SwingUtilities.updateComponentTreeUI(this);
                }
                int x=linkedTo.container.getX()+linkedTo.getX();
                int y=linkedTo.container.getY()+linkedTo.getY();
                this.setLocation(x, y);
            }
            
            void unStick(){//decolle this (dragged) de son linkedTo
                if(linkedTo==null) return;
                linkedTo.resize(false);//retrecit
                fields.add(linkedTo);//disponible pour liaison
                linkedTo.variable=null;//suppression des liens
                linkedTo=null;
            }


            /*****************
            * Mouse Override *
            *****************/
            
            void setToForeground(){//this a l avant-plan quand on le drag==tete de liste
                ViewPlaying.this.dragDrop.remove(this);
                ViewPlaying.this.dragDrop.add(this, 0);
            }
            
            void switchOff(){//eteint le seul field a eteindre
                brightF.setBorder(brightF.border);//pour eviter decalage brighter/darker
                brightF=null;
            }

            boolean inWhiteBoard(){//est dans whiteBoard
                Point p=this.getLocation();
                return (p.x>0 && p.x<width/2 && p.y>0 && p.y<height);
            }
            
            public void mousePressed(MouseEvent e){
                unStick();//detache si etait attache
                mouseX=e.getX();//position initiale de souris
                mouseY=e.getY();
                this.setToForeground();//mettre ce qu on drag a l avant-plan
            }

            public void mouseDragged(MouseEvent e){
                int x=e.getXOnScreen()-x00-mouseX-ViewPlaying.this.getX();
                int y=e.getYOnScreen()-y00-mouseY-ViewPlaying.this.getY();
                this.setLocation(x, y);
                
                //allume et eteint les NumberField selon les cas
                if(brightF!=null) switchOff();//eteint tout d abord
                int nearby=closeCommand();
                if(nearby!=-1){//proche d un bloc
                    brightF=fields.get(nearby);//allume le seul necessaire
                    brightF.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
                }
                
                //ouvre eventuellement la poubelle
                try{
                    updateBinState();
                }
                catch(IOException e1){
                    System.out.println("Couldn't update bin");
                }
            }

            public void mouseReleased(MouseEvent e){
                if(this.toDelete()){
                    try{
                        this.deleteSteps();
                    }
                    catch(IOException e1){
                        System.out.println("Couldn't delete command");
                    }
                }
                else{
                    if(inWhiteBoard() && lastCreated){//regeneration
                        lastCreated=false;
                        addSameVariable(this);
                    }
                    if(brightF!=null) stick(true);//accroche
                }
            }
            
            public void mouseMoved(MouseEvent e){}
            public void mouseClicked(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne Variable
    }//fin classe interne PanelDragDropBoard
}