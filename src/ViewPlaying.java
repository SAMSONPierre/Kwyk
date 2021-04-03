import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    final JLabel errorName=new JLabel("Please enter a valid name :");
    private Level level;//niveau en cours
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;//fusion de WhiteBoard et CommandBoard
    private JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    private JButton run=new JButton("Run"), stop=new JButton("Stop"), reset=new JButton("Reset");
    private Timer timer=new Timer(30, null);//vitesse par defaut
    private JSlider slider=new JSlider();//regulation de la vitesse
    private PanelDragDropBoard.Command runC;//la commande en execution
    private JProgressBar limite;
    private HashMap<String, Integer> variables=new HashMap<String, Integer>();//nom, valeur
    private LinkedList<JLabel[]> varDisplay=new LinkedList<JLabel[]>();//affichage des variables
    
    ViewPlaying(Player player, boolean isCreating) throws IOException{
        super(player);
        errorName.setForeground(Color.RED);
        this.level=player.getLevel();
        addBoard();//ajout des tableaux, avec des marges de 20 (haut, bas et entre tableaux)
        addFeatures(isCreating, player.username.equals("GM"));//ajout des fonctionnalites
        if(level.functions!=null) dragDrop.loadFunctions();
        if(level.mainCode!=null) dragDrop.loadMainCode();
    }
    
    void addBoard() throws IOException{
        blackBoard=new PanelBlackBoard();
        this.add(blackBoard);//taille fixee
        //setUIFont(new javax.swing.plaf.FontUIResource("Times New Roman", Font.BOLD, 14));
        dragDrop=new PanelDragDropBoard();
        this.add(dragDrop);//taille relative a l ecran
    }

    boolean inWhiteBoard(Component c){//est dans whiteBoard
        Point p=c.getLocation();
        return (p.x>0 && p.x<dragDrop.width/2 && p.y>0 && p.y<dragDrop.height);
    }
    
    void addFeatures(boolean isCreating, boolean isGM){
        features.setBounds(20, 440+buttonHeight, 400, heightFS-460-buttonHeight);
        this.add(features);
        
        JButton seeGrid=new JButton("Hide grid");//voir la grille
        seeGrid.addActionListener((event)->{
            blackBoard.gridApparent=!blackBoard.gridApparent;
            seeGrid.setText(blackBoard.gridApparent?"Hide grid":"Show grid");
            blackBoard.repaint();
        });
        features.add(seeGrid);
        
        run.addActionListener((event)->run());
        stop.addActionListener((event)->stop());
        reset.addActionListener((event)->reset());
        features.add(run);
        features.add(stop);
        features.add(reset);
        stop.setVisible(false);//apparait apres avoir clique sur run
        reset.setVisible(false);//idem, pour stop
        
        Hashtable<Integer, JLabel> labels=new Hashtable<>();
        labels.put(0, new JLabel("Slower"));
        labels.put(100, new JLabel("Faster"));
        slider.setLabelTable(labels);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                JSlider source=(JSlider)e.getSource();
                if(!source.getValueIsAdjusting()){
                    int speed=(int)source.getValue();
                    if(speed==0) ViewPlaying.this.stop();
                    else if(speed==100) ViewPlaying.this.timer.setDelay(0);//ne saute pas vraiment l'animation...
                    else{
                        if(speed>50)//pour qu'on puisse voir un peu plus la difference
                            ViewPlaying.this.timer.setDelay(1000/speed);
                        else ViewPlaying.this.timer.setDelay(1500/speed);
                    }
                }
            }
        });
        features.add(slider);
        
        if(isCreating){//creer un niveau -> que pour la page Create
            JCheckBox saveCode=new JCheckBox("Save main code");
            JCheckBox saveFun=new JCheckBox("Save functions");
            JButton submit=new JButton("Submit");
            submit.addActionListener((event)->{
                String name=JOptionPane.showInputDialog(this,"Level's name ?", null);
                while(name!=null && (name.equals("") || !name.matches("^[a-zA-Z0-9]*$")))
                    name=JOptionPane.showInputDialog(this,errorName, null);
                if(name!=null) control.submit(name, level, saveCode.isSelected()?dragDrop.convertStart():null,
                    saveFun.isSelected()?dragDrop.convertFunctions():null);
            });
            features.add(submit);
            if(isGM){
                features.add(saveCode);
                features.add(saveFun);
            }
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
        updateVariableDisplay();
        run.setVisible(false);
        stop.setVisible(true);
        runC=dragDrop.commands.getFirst();
        runC=runC.canExecute()?runC.next:null;
        ActionListener taskPerformed=new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(runC!=null) runC=runC.execute();
                else{
                    stop();//arret automatique
                    victoryMessage();
                }
            }
        };
        timer.addActionListener(taskPerformed);
        timer.start();
    }
    
    void stop(){
        timer.stop();
        stop.setVisible(false);
        reset.setVisible(true);
        for(ActionListener action : timer.getActionListeners()) timer.removeActionListener(action);
    }
    
    void reset(){
        level.initializePlayerDraw();//vide le dessin du joueur
        blackBoard.brush.resetBrush();//remet le pinceau a l'emplacement initial
        blackBoard.brush2=false;//pas de symetrie
        if(!variables.isEmpty()){
            for(String key : variables.keySet()) variables.replace(key, 0);
            updateVariableDisplay();
        }
        for(PanelDragDropBoard.Command c : dragDrop.commands) c.reset();
        repaint();
        reset.setVisible(false);
        run.setVisible(true);
    }
            
    void updateVariableDisplay(){
        for(JLabel[] label : varDisplay){
            label[1].setText(variables.get(label[0].getText()).toString());
            features.add(label[0]);
            features.add(label[1]);
        }
        SwingUtilities.updateComponentTreeUI(features);
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

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
            UIManager.put(key, f);
        }
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
        private boolean drawing=true, brush2;//pinceau posé par defaut, 2e pinceau de symetrie

        PanelBlackBoard(){
            this.setBounds(20, 20+buttonHeight, 400, 400);//marge gauche=20, 20+hauteur d un bouton en haut, taille 400*400
            this.setBackground(Color.BLACK);//fond noir
            this.x=level.brushX;
            this.y=level.brushY;
            this.angle=level.brushAngle;
            this.brushColor=level.brushFirstColor;
        }
        
        
        /***** Drawing & Paint *****/

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(gridApparent) paintGrid(g);//grille apparente quand on le souhaite
            if(brush2) paintSymmetry(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setStroke(new BasicStroke(4));
            for(Vector v : level.pattern) paintVector(g2, v, true);//patron
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
        
        void paintSymmetry(Graphics g){
            g.setColor(Color.RED);
            g.drawLine(200, 0, 200, 400);
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
        
        
        /************************
        *      Brush Class      *
        ************************/
        
        private class Brush extends Path2D.Double{//pinceau
            Brush(){//fleche dans le coin superieur gauche de blackBoard
            	this.resetBrush();
            }
            
            void resetBrush(){
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
        private int lastPositionY=20, deltaY=0, numberOfFunction=0;//positionY libre, mouvement de la roulette
        private Bin bin;
        final Border borderV=BorderFactory.createLineBorder(new Color(255, 204, 255), 2);
        
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
        }
        
        int countConvert(Command c){
            int res=0;
            while(c!=null){
                res++;
                c=c.next;
            }
            return res;
        }
        
        String[] convertStart(){
            Command tmp=commands.getFirst();
            int size=countConvert(tmp);
            String[] res=new String[size];
            size=0;
            while(tmp!=null){
                res[size++]=(tmp instanceof CommandFunctionCall)?
                    ((CommandFunctionCall)tmp).name.getText():tmp.name;
                tmp=tmp.next;
            }
            return res;
        }
        
        String[] convertFunctions(){
            int size=0;
            for(Command c : commands){
                if(c instanceof CommandFunctionInit) size+=countConvert(c);
            }
            String[] res=new String[size];
            size=0;
            for(Command c : commands){
                if(c instanceof CommandFunctionInit){
                    res[size++]=((CommandFunctionInit)c).nameFunction.getText();
                    Command tmp=((CommandFunctionInit)c).next;
                    while(tmp!=null){
                        res[size++]=tmp.name;
                        tmp=tmp.next;
                    }
                }
            }
            return res;
        }
        
        void loadMainCode(){//charge du code accroche a Start
            String[] toLoad=level.mainCode;
            if(toLoad.length==0) return;
            LinkedList<Command> saveLast=new LinkedList<Command>();
            saveLast.add(commands.getFirst());
            for(int i=1; i<toLoad.length; i++){
                Command last=saveLast.removeLast();
                if(toLoad[i].equals("hookHorizontal")){
                    saveLast.getLast().previous=last;
                    last.next=saveLast.getLast();
                }
                else{
                    Command command=addLaunch(toLoad[i]);
                    this.add(command);//visible
                    commands.add(command);//accessible
                    if(command instanceof CommandWithCommands){
                        CommandWithCommands cwc=(CommandWithCommands)command;
                        this.add(cwc.hookV);
                        this.add(cwc.hookH);
                        if(!(cwc instanceof CommandFunctionInit)) commands.add(cwc.hookH);
                        else{
                            command=new CommandFunctionCall((CommandFunctionInit)command, 0, 0);
                            this.add(command);
                            commands.add(command);
                        }
                    }
                    command.previous=last;//liens
                    last.next=command;
                    if(last instanceof CommandWithCommands)
                        saveLast.addLast(((CommandWithCommands)last).hookH);
                    saveLast.addLast(command);//prochain qui sera lie
                }
            }
            Command second=commands.getFirst().next;
            second.updateHookVRec(second.getTmpCwc());//met a jour les hookV
            second.updateAllLocation();//accroche correctement
            limite.setValue(getNumbersFromHead()[0]);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        void loadFunctions(){
            String[] toLoad=level.functions;
            if(toLoad.length==0) return;
            LinkedList<CommandFunctionInit> functionHead=new LinkedList<CommandFunctionInit>();
            LinkedList<Command> saveLast=new LinkedList<Command>();
            saveLast.add(new Command("", Color.BLACK, 0));//inutile, pour eviter erreur liste vide
            for(int i=0; i<toLoad.length; i++){
                Command last=saveLast.removeLast();
                if(toLoad[i].equals("hookHorizontal")){
                    saveLast.getLast().previous=last;
                    last.next=saveLast.getLast();
                }
                else{
                    Command command=addLaunch(toLoad[i]);
                    this.add(command);//visible
                    commands.add(command);//accessible
                    if(command instanceof CommandWithCommands){
                        CommandWithCommands cwc=(CommandWithCommands)command;
                        this.add(cwc.hookV);
                        this.add(cwc.hookH);
                        if(!(cwc instanceof CommandFunctionInit)) commands.add(cwc.hookH);
                        else{
                            last=null;
                            for(Command c : saveLast) saveLast.remove(c);
                            functionHead.add(((CommandFunctionInit)command));
                            lastPositionY+=addCommandCall((CommandFunctionInit)command, lastPositionY);
                        }
                    }
                    if(last!=null){//dans la fonction
                        command.previous=last;//liens
                        last.next=command;
                        if(last instanceof CommandWithCommands)
                            saveLast.addLast(((CommandWithCommands)last).hookH);
                    }
                    saveLast.addLast(command);//prochain qui sera lie
                }
            }
            for(CommandFunctionInit c : functionHead){
                Command second=c.next;
                second.updateHookVRec(second.getTmpCwc());//met a jour les hookV
                second.updateAllLocation();//accroche correctement
            }
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        void setFunctionVariableButton(){
            if(level.numberOfVariables!=0){
                JButton createV=new JButton("Create a new variable");
                JButton removeV=new JButton("Remove one variable");
                Dimension dC=createV.getPreferredSize();
                Dimension dR=removeV.getPreferredSize();
                createV.setBounds(width/2-20-dC.width, 20, dC.width, dC.height);
                removeV.setBounds(createV.getX(), 30+dR.height, dR.width, dR.height);
                
                createV.addActionListener((event)->{
                    String name=JOptionPane.showInputDialog("Name of this variable ?");
                    while(name!=null && ((name.equals("") || name.contains(" ") || name.equals("x")
                       || name.equals("y") || name.equals("angle") || variables.containsKey(name))))
                        name=JOptionPane.showInputDialog(errorName);
                    if(name!=null){
                        addVariable(name);
                        removeV.setEnabled(true);
                    }
                    if(variables.size()==level.numberOfVariables){//max atteint
                        createV.setEnabled(false);
                        createV.setBackground(Color.red);
                    }
                });
                removeV.addActionListener((event)->{
                    Object[] choice=new String[variables.size()];
                    int i=0;
                    for(String name : variables.keySet()) choice[i++]=name;
                    Object name=JOptionPane.showInputDialog(null, "Name of this variable ?",
                        "", JOptionPane.QUESTION_MESSAGE, null, choice, choice[0]);
                    if(name==null) return;//bouton annuler
                    removeVariable(name.toString());
                    if(variables.size()+1==level.numberOfVariables){//etait au max
                        createV.setEnabled(true);
                        createV.setBackground(null);
                    }
                    removeV.setEnabled(!variables.isEmpty());//plus de variable
                });
                this.add(createV);
                this.add(removeV);
                removeV.setEnabled(false);
            }
            if(level.numberOfFunctions!=0 && level.mainCode==null){
                JButton createF=new JButton("Create a new function");
                Dimension dF=createF.getPreferredSize();
                createF.setBounds(width/2-20-dF.width, 50+2*dF.height, dF.width, dF.height);
                createF.addActionListener((event)->{
                    String name=JOptionPane.showInputDialog("Name of this fonction ?");
                    while(name!=null && (name.equals("") || nameFunAlreadySet(name)))
                        name=JOptionPane.showInputDialog(errorName);
                    if(name!=null) addFunction(name, width/2-20-dF.width, 60+3*dF.height);
                    if(numberOfFunction==level.numberOfFunctions){//max atteint
                        createF.setEnabled(false);
                        createF.setBackground(Color.red);
                    }
                });
                this.add(createF);
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

        public void mouseWheelMoved(MouseWheelEvent e){
            int addToY=(e.getWheelRotation()<0?1:-1)*e.getScrollAmount()*10;
            if(e.getX()<this.getX()+width/2-x00){//whiteBoard
                for(Component c : ViewPlaying.this.dragDrop.getComponents()){
                    if(c instanceof Command && commands.contains(c) && !(c instanceof HookHorizontal)
                    || (c instanceof Variable && !((Variable)c).lastCreated))
                        c.setLocation(c.getX(), c.getY()+addToY);
                }
            }
            else{//commandBoard
                deltaY+=addToY;
                for(Component c : ViewPlaying.this.dragDrop.getComponents()){
                    if(c instanceof Command && !commands.contains(c) && !(c instanceof HookHorizontal)
                    || (c instanceof Variable && ((Variable)c).lastCreated))
                        c.setLocation(c.getX(), c.getY()+addToY);
                }
            }
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);//WhiteBoard a gauche
            g2.fillRect(0, 0, width/2, height);
            g2.setColor(Color.LIGHT_GRAY);//CommandBoard a droite
            g2.fillRect(width, 0, width/2, height);
        }
            
        public boolean isOptimizedDrawingEnabled(){//empecher foreground automatique
            return false;
        }
        
        void setToForeground(Component c){
            this.remove(c);
            this.add(c, 0);
        }
        
        
        /***** Gestion Blocs *****/
        
        void addAvailableCommands(){//premier ajout des commandes disponibles
            //ajout dans WhiteBoard
            commands.add(new CommandStart());//toujours le premier de la liste de commandes
            this.add(commands.getFirst());
            //ajout dans CommandBoard
            for(String c : level.availableCommands) lastPositionY+=addCommand(c, lastPositionY);
        }
        
        int addCommand(String name, int positionY){//(re)generation des commandes
            Command toAdd;
            switch(name){
                case "for":
                    toAdd=new CommandFor(width/2+20, positionY);
                    break;
                case "if":
                    toAdd=new CommandIf(width/2+20, positionY);
                    break;
                case "while":
                    toAdd=new CommandWhile(width/2+20, positionY);
                    break;
                case "drawLine":
                    toAdd=new CommandDrawLine(width/2+20, positionY);
                    break;
                case "drawArc":
                    toAdd=new CommandDrawArc(width/2+20, positionY);
                    break;
                case "raisePutBrush":
                    toAdd=new CommandRaisePutBrush(width/2+20, positionY);
                    break;
                case "moveTo":
                    toAdd=new CommandMoveTo(width/2+20, positionY);
                    break;
                case "setAngle":
                    toAdd=new CommandSetAngle(width/2+20, positionY);
                    break;
                case "addAngle":
                    toAdd=new CommandAddAngle(width/2+20, positionY);
                    break;
                case "setColor":
                    toAdd=new CommandSetColor(width/2+20, positionY);
                    break;
                case "shiftColor":
                    toAdd=new CommandShiftColor(width/2+20, positionY);
                    break;
                case "symmetry":
                    toAdd=new CommandSymmetry(width/2+20, positionY);
                    break;
                case "affectation":
                    toAdd=new CommandAffectation(width/2+20, positionY);
                    break;
                case "addition":
                    toAdd=new CommandAddition(width/2+20, positionY);
                    break;
                case "soustraction":
                    toAdd=new CommandSoustraction(width/2+20, positionY);
                    break;
                case "multiplication":
                    toAdd=new CommandMultiplication(width/2+20, positionY);
                    break;
                case "division":
                    toAdd=new CommandDivision(width/2+20, positionY);
                    break;
                default :
                    return 0;
            }
            this.add(toAdd);
            int res=toAdd.getHeight()+10;
            if(toAdd instanceof CommandWithCommands){
                CommandWithCommands tmp=(CommandWithCommands)toAdd;
                this.add(tmp.hookV);
                this.add(tmp.hookH);
                res+=tmp.hookV.getHeight()+tmp.hookH.getHeight();
            }
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
            return res;
        }
        
        Command addLaunch(String name){//pour generer du code
            switch(name){
                case "for":
                    return new CommandFor(0, 0);
                case "if":
                    return new CommandIf(0, 0);
                case "while":
                    return new CommandWhile(0, 0);
                case "drawLine":
                    return new CommandDrawLine(0, 0);
                case "drawArc":
                    return new CommandDrawArc(0, 0);
                case "raisePutBrush":
                    return new CommandRaisePutBrush(0, 0);
                case "moveTo":
                    return new CommandMoveTo(0, 0);
                case "setAngle":
                    return new CommandSetAngle(0, 0);
                case "addAngle":
                    return new CommandAddAngle(0, 0);
                case "setColor":
                    return new CommandSetColor(0, 0);
                case "shiftColor":
                    return new CommandShiftColor(0, 0);
                case "symmetry":
                    return new CommandSymmetry(0, 0);
                case "affectation":
                    return new CommandAffectation(0, 0);
                case "addition":
                    return new CommandAddition(0, 0);
                case "soustraction":
                    return new CommandSoustraction(0, 0);
                case "multiplication":
                    return new CommandMultiplication(0, 0);
                case "division":
                    return new CommandDivision(0, 0);
                default ://fonction
                    if(!nameFunAlreadySet(name)){//init
                        numberOfFunction++;
                        return new CommandFunctionInit(name, width/2-200, 60+3*ViewPlaying.this.buttonHeight);
                    }
                    for(Command c : commands){
                        if(c instanceof CommandFunctionInit && ((CommandFunctionInit)c).nameFunction.getText().equals("  "+name+"  "))
                            return new CommandFunctionCall((CommandFunctionInit)c, 0, 0);
                    }
            }
            return null;//a priori jamais atteint
        }
        
        void addFunction(String name, int x, int y){//nouvelle fonction, ajout dans WhiteBoard
            numberOfFunction++;
            CommandFunctionInit fC=new CommandFunctionInit(name, x, y);
            this.add(fC, 0);
            this.add(fC.hookV, 0);
            this.add(fC.hookH, 0);
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
            variables.put(name, 0);//met dans HashMap, par defaut variable=0
            JLabel[] tmp={new JLabel(name), new JLabel("0")};
            varDisplay.add(tmp);
            boolean firstSet=variables.size()==1;//ajout des blocs de commande
            if(firstSet){
                for(Component c : getComponents()){
                    if(c instanceof CommandOperationV){//deja initialise une fois
                        firstSet=false;
                        break;
                    }
                }
            }
            if(firstSet){
                lastPositionY+=addCommand("affectation", lastPositionY);
                lastPositionY+=addCommand("addition", lastPositionY);
                lastPositionY+=addCommand("soustraction", lastPositionY);
                lastPositionY+=addCommand("multiplication", lastPositionY);
                lastPositionY+=addCommand("division", lastPositionY);
                Variable variable=new Variable(width/2+20, lastPositionY);
                this.add(variable);
                lastPositionY+=variable.getHeight()+10;
                SwingUtilities.updateComponentTreeUI(this);//refresh affichage
            }
            for(NumberField f : fields) f.setBorder(borderV);
            for(Component c : getComponents()){//ajout dans tous les comboBox
                if((!firstSet && (c instanceof CommandOperationV || c instanceof Variable))
                || c instanceof CommandIf || c instanceof CommandWhile) resizeBox(c, name, true);
            }
            resizeCommand();
        }
        
        void addSettedVariables(int positionY){//regeneration des variables
            Variable var=new Variable(width/2+20, positionY);
            this.add(var);
            SwingUtilities.updateComponentTreeUI(this);//refresh affichage
        }
        
        void removeVariable(String name){
            variables.remove(name);
            for(JLabel[] label : varDisplay){
                if(label[0].getText().equals(name)){
                    varDisplay.remove(label);
                    features.remove(label[0]);
                    features.remove(label[1]);
                    SwingUtilities.updateComponentTreeUI(features);
                    break;
                }
            }
            if(variables.isEmpty()){//efface toutes les variables
                for(NumberField f : fields) f.setBorder(null);
                for(Component c : getComponents()){
                    if(c instanceof CommandOperationV || c instanceof Variable){
                        if(commands.remove(c)==true){//OperationV sur whiteBoard
                            ((Command)c).unStick();
                            if(((Command)c).next!=null) ((Command)c).next.unStick();
                            fields.remove(((CommandOperationV)c).input);
                        }
                        else if(c instanceof Variable && ((Variable)c).linkedTo!=null) ((Variable)c).unStick();
                        if(inWhiteBoard(c)) this.remove(c);
                        SwingUtilities.updateComponentTreeUI(this);
                    }
                }
            }
            for(Component c : getComponents()){//retrait de tous les comboBox
                if(c instanceof CommandOperationV || c instanceof CommandIf
                    || c instanceof CommandWhile || c instanceof Variable) resizeBox(c, name, false);
            }
            resizeCommand();
        }
        
        void resizeBox(Component c, String name, boolean add){
            JComboBox box=(c instanceof CommandIf)?((CommandIf)c).variableG:(c instanceof CommandWhile)?
              ((CommandWhile)c).variableG:(c instanceof Variable)?((Variable)c).choice:((CommandOperationV)c).choice;
            if(add) box.addItem(name);
            else box.removeItem(name);
            box.setPreferredSize(new Dimension(largerVariable(box), box.getHeight()));
            if(c instanceof Variable) ((Variable)c).resize();
        }
        
        int largerVariable(JComboBox box){//larger du comboBox apres retrait d un element
            if(box.getItemCount()==0) return 25;//liste vide
            JLabel larger=new JLabel(box.getItemAt(0).toString());
            for(int i=1; i<box.getItemCount(); i++){
                JLabel comp=new JLabel(box.getItemAt(i).toString());
                if(larger.getPreferredSize().width<comp.getPreferredSize().width) larger=comp;
            }
            larger.setText("  "+larger.getText()+"  ");
            return larger.getPreferredSize().width+15;
        }
        
        void resizeCommand(){//resize commandes sans variable
            for(Component c : getComponents()){
                if(c instanceof Command) ((Command)c).resize();
            }
        }
        
        
        /***** Create Levels *****/
        
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
        
        
        /************************
        *          Bin          *
        ************************/
        
        private class Bin extends JPanel{
            BufferedImage state;

            Bin() throws IOException{
                this.setBounds(width/2-45, height-50, 40, 40);
                this.loadBin("images/closedBin.png");
            }

            void loadBin(String path) throws IOException{//ouverte ou fermee
                try{
                    this.state=ImageIO.read(new File(path));
                    this.repaint();
                }
                catch(FileNotFoundException e){}
            }

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(this.state, 0, 0, 40, 40, Color.WHITE, null);
            }
        }


        /************************
        *     Command class     *
        ************************/

        class Command extends JPanel implements MouseInputListener{
            final String name;//if, else, for, while, ...
            final Color color;
            final int commandH=40, hookW=70, positionY;//hauteur d une commande, largeur d un hookH
            protected Command next, previous;//next a executer, previous pour ajuster l affichage
            private int mouseX, mouseY;//position initiale de la souris au moment du drag
            protected NumberField input;
            protected int commandW=0;//utile pour readapter largeur des NumberField
            
            Command(String name, Color color, int positionY){
                this.name=name;
                this.color=color;
                this.positionY=positionY;
                this.setBackground(color);
                this.setLayout(new GridBagLayout());//pour centrer verticalement les textes
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
            }
            
            boolean canExecute(){//pour les commandes!=CWC qui ont exactement un NumberField
                boolean isEmpty=input.isEmpty() || (this instanceof CommandDrawLine)?input.getNumber()<2:false;
                if(isEmpty) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else input.setBorder(variables.isEmpty()?null:borderV);
                return !isEmpty;
            }
            
            Command execute(){//chaque fonction les implemente
                return null;
            }
            
            void reset(){}//quand on interrompt le programme en plein milieu
            
            
            /***** Delete Command *****/
            
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
            
            
            /***** Regeneration of Command *****/
            
            void newDrag(){//nouvelle commande qu on drag pour la premiere fois
                if(inWhiteBoard(this) && !commands.contains(this)){//premier drag sur whiteBoard
                    commands.add(this);
                    if(this instanceof CommandWithCommands) commands.add(this.next);//ajout de HookH aussi
                    if(level.numberOfVariables!=0 && input!=null){
                        fields.add(input);
                        if(!variables.isEmpty()) input.setBorder(borderV);
                        if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                            NumberField tmp=(this instanceof CommandDrawArc)?
                                ((CommandDrawArc)this).angleScan:((CommandMoveTo)this).positionY;
                            fields.add(tmp);
                            if(!variables.isEmpty()) tmp.setBorder(borderV);
                        }
                    }
                    //pour regenerer commande utilisee :
                    if(this instanceof CommandFunctionCall)
                        addCommandCall(((CommandFunctionCall)this).function, positionY);
                    else addCommand(this.name, this.positionY);
                }
            }

            
            /***** Stick together *****/
            
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

            Command closeCommand(){//this et command sont assez proches pour se coller
                if(!(this instanceof CommandFunctionInit)){//initialisateur de fonction sans previous
                    for(Command c : commands){
                        if(c instanceof CommandWithCommands){
                            if(closeHeight(c) && closeWidthIntern((CommandWithCommands)c)) return c;
                        }
                        else if(closeHeight(c) && closeWidth(c)) return c;
                    }
                }
                return null;
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


            /***** Unstick *****/

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
                if(input!=null){
                    input.setBorder(variables.isEmpty()?null:borderV);
                    if(this instanceof CommandMoveTo || this instanceof CommandDrawArc){
                        NumberField field=(this instanceof CommandMoveTo)?
                            ((CommandMoveTo)this).positionY:((CommandDrawArc)this).angleScan;
                        field.setBorder(variables.isEmpty()?null:borderV);
                    }
                }
                if(limite!=null) limite.setValue(getNumbersFromHead()[0]);
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


            /***** Update of Command *****/
            
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
            
            void resize(){//largeur
                if(input==null) return;//pas besoin de resize, taille constante
                //resize des composants du bloc
                input.resize();
                if(this instanceof CommandMoveTo || this instanceof CommandDrawArc){
                    ((this instanceof CommandMoveTo)?((CommandMoveTo)this).positionY:
                        ((CommandDrawArc)this).angleScan).resize();
                }
                //resize du bloc en lui-meme
                int width=this.commandW+input.getPreferredSize().width;
                if(this instanceof CommandMoveTo || this instanceof CommandDrawArc){
                    NumberField tmp=(this instanceof CommandMoveTo)?((CommandMoveTo)this).positionY:
                        ((CommandDrawArc)this).angleScan;
                    width+=tmp.getPreferredSize().width;
                }
                else if(this instanceof CommandIf || this instanceof CommandWhile || this instanceof CommandOperationV){
                    JComboBox tmp=(this instanceof CommandIf)?((CommandIf)this).variableG:(this instanceof CommandWhile)?
                        ((CommandWhile)this).variableG:((CommandOperationV)this).choice;
                    width+=tmp.getPreferredSize().width;
                }
                this.setBounds(getX(), getY(), width, getHeight());
                SwingUtilities.updateComponentTreeUI(this);
                
                //replacement d un deuxieme champ avec une variable deja initialisee
                if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                    NumberField field2=(this instanceof CommandMoveTo)?
                        ((CommandMoveTo)this).positionY:((CommandDrawArc)this).angleScan;
                    if(field2.variable!=null) field2.variable.stick(false);
                }
                //replacement de variables initialisees a cote de comboBox
                else if(this instanceof CommandIf || this instanceof CommandWhile || this instanceof CommandOperationV){
                    Variable var=((Command)this).input.variable;
                    if(var!=null) var.stick(false);
                }
            }
            

            /***** Look *****/
            
            void setToForeground(){//this a l avant-plan quand on le drag==tete de liste
                if(this instanceof CommandWithCommands){
                    CommandWithCommands tmp=(CommandWithCommands)this;
                    ViewPlaying.this.dragDrop.remove(tmp.hookV);
                    ViewPlaying.this.dragDrop.add(tmp.hookV, 0);
                }
                dragDrop.setToForeground(this);
                stickVarToForeground();
                if(this.next!=null) this.next.setToForeground();//appel recursif
            }
            
            void stickVarToForeground(){
                if(input!=null && input.variable!=null){
                    input.variable.stick(false);
                    dragDrop.setToForeground(input.variable);
                }
                if(this instanceof CommandDrawArc || this instanceof CommandMoveTo){
                    NumberField f=(this instanceof CommandDrawArc)?
                        ((CommandDrawArc)this).angleScan:((CommandMoveTo)this).positionY;
                    if(f.variable!=null){
                        f.variable.stick(false);
                        dragDrop.setToForeground(input.variable);
                    }
                }
            }
            
            void switchOff(){//eteint le seul bloc a eteindre
                if(brightC!=null){
                    brightC.setBackground(brightC.color);//pour eviter decalage brighter/darker
                    brightC=null;
                }
            }
            

            /***** Mouse Override *****/
            
            public void mousePressed(MouseEvent e){
                unStick();//detache si a un precedent
                mouseX=e.getX();//position initiale de souris
                mouseY=e.getY();
                this.setToForeground();//mettre ce qu on drag a l avant-plan
            }
            
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

            public void mouseDragged(MouseEvent e){
                if(this instanceof CommandOperationV && variables.isEmpty()) return;//immobile
                //drag this et ses next + variables
                int x=e.getXOnScreen()-x00-mouseX-ViewPlaying.this.getX();
                int y=e.getYOnScreen()-y00-mouseY-ViewPlaying.this.getY();
                this.setLocation(x, y);
                stickVarToForeground();
                if(this.next!=null) this.next.stick();
                
                //allume et eteint les blocs selon les cas
                switchOff();//eteint tout d abord
                Command nearby=closeCommand();
                if(nearby!=null){//proche d un bloc
                    if(limite==null || getNumberFromThis()+getNumbersFromHead()[0]<=level.numberOfCommands){//attachable
                        brightC=nearby;//allume le seul bloc a allumer
                        brightC.setBackground(brightC.getBackground().brighter());
                    }
                }
                
                //ouvre eventuellement la poubelle
                try{
                    updateBinState();
                }
                catch(IOException e1){}
            }

            public void mouseReleased(MouseEvent e){
                if(this.toDelete()){
                    try{
                        this.deleteSteps();
                    }
                    catch(IOException e1){}
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

            }
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne interne Command
        

        /************************
        *  Subclass of Command  *
        ************************/
        
        class CommandStart extends Command implements Serializable{//bloc initial present que sur WhiteBoard
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
            
            Command closeCommand(){//this et c sont assez proches pour se coller
                for(Command c : commands){
                    if(!inFunction(c)){
                        if(c instanceof CommandWithCommands){
                            if(closeHeight(c) && closeWidthIntern((CommandWithCommands)c)) return c;
                        }
                        else if(closeHeight(c) && closeWidth(c)) return c;
                    }
                }
                return null;
            }
            
            boolean canExecute(){
                return function.canExecute();
            }
            
            Command execute(){
                function.caller.remove(this);
                function.caller.addFirst(this);//met en avant pour se distinguer des autres
                return function.execute();
            }
        }//fin classe interne FunctionCall


        /*************************
        *   Bloc with commands   *
        *************************/
        
        class CommandWithCommands extends Command{
            final HookVertical hookV;//juste une accroche verticale
            final HookHorizontal hookH;//next si pas de commandes internes
            
            CommandWithCommands(String name, Color color, int x, int y){
                super(name, color, y);
                hookV=new HookVertical(color, commandH, x, y+commandH+deltaY);//apres la commande
                hookH=new HookHorizontal(this, x, y+2*commandH+deltaY);//apres hookV
                this.next=hookH;
                hookH.previous=this;
            }
            
            boolean canExecute(){//ne peut pas etre vide
                boolean ok=true;
                if(!(this instanceof CommandFunctionInit)){//input vide
                    ok=!input.isEmpty();
                    if(!ok) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    else input.setBorder(variables.isEmpty()?null:borderV);
                }
                else{//vide dans la fonction
                    Command tmp=this.next;
                    while(tmp!=hookH){
                        if(!(tmp.canExecute())) ok=false;//on ne s arrete pas
                        tmp=tmp.next;
                    }
                }
                if(ok && next==hookH){//champs remplis mais cwc vide
                    hookH.setBackground(Color.RED.darker());
                    return false;
                }
                hookH.setBackground(color);//cwc rempli
                return ok;
            }
            
            Command execute(){//sera complete par ses enfants
                Command tmp=this.next;
                while(tmp!=this.hookH) tmp=tmp.execute();
                return null;//suivant gere par enfants
            }

            boolean evaluate(String op, int varG){
            	switch(op){
                    case "  <" : return varG<input.getNumber();
                    case " <=" : return varG<=input.getNumber();
                    case "  >" : return varG>input.getNumber();
                    case " >=" : return varG>=input.getNumber();
                    case " ==" : return varG==input.getNumber();
            	}
            	return false;
            }
            
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
            final CommandWithCommands head;
            
            HookHorizontal(CommandWithCommands head, int x, int y){
                super("hookHorizontal", head.color, y);
                this.head=head;
                this.setBounds(x, y, hookW, commandH/2);
            }
            
            public void mouseDragged(MouseEvent e){}//ne peut pas etre dragged seul
            
            void removeHH(){
            	PanelDragDropBoard.this.remove(this);
                commands.remove(this);
            }
            
            boolean canExecute(){
                return true;
            }

            Command execute(){
                return head;//revenir en arriere pour for et while
            }
        }//fin classe HookHorizontal


        class CommandFor extends CommandWithCommands{//classe interne
            private int nbRepeats=-1;//pas initialisee
            
            CommandFor(int x, int y){
                super("for", new Color(230, 138, 0), x, y);
                super.input=new NumberField(this);//nombre de repetition a saisir
                
                this.add(new JLabel("  Repeat  "));
                this.add(input);
                this.add(new JLabel("  time  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width;
            }

            Command execute(){
                if(nbRepeats==-1) nbRepeats=(input.getNumber()<0)?0:input.getNumber();
                nbRepeats--;
                if(nbRepeats>=0) return next;
                return hookH.next;//nbRepeats reinitialisee a -1 a la fin
            }
            
            void reset(){
                this.nbRepeats=-1;
            }
        }//fin classe interne For


        class CommandIf extends CommandWithCommands{//classe interne
            private JComboBox variableG=new JComboBox(), operateur=new JComboBox();//a priori seulement deux listes deroulantes
            private String op="<";//par defaut
            private boolean alreadyExecuted;
            //e.g x<100 <=> variableG(varG)="x", operateur="<", variableD(varD)="100"
            
            CommandIf(int x, int y){
                super("if", new Color(179, 0, 89), x, y);
                super.input=new NumberField(this);//choix libre du joueur donc pas une liste
                
                String[] tmp={"x", "y", "angle"};
                for(String s : tmp) variableG.addItem(s);
                for(String s : variables.keySet()) variableG.addItem(s);

                this.operateur.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        op=(String)operateur.getSelectedItem();
                    }
                });
                String[] tmpOp={"  <", "  >", " <=", " >=", " =="};
                for(String s : tmpOp) this.operateur.addItem(s);

                this.add(new JLabel("  If  "));
                this.add(variableG);
                this.add(operateur);
                this.add(new JLabel("  "));//pour la presentation
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width-variableG.getPreferredSize().width;
            }

            Command execute(){
                int varG=variableG.getSelectedItem().equals("x")?blackBoard.x:
                     variableG.getSelectedItem().equals("y")?blackBoard.y:
                     variableG.getSelectedItem().equals("angle")?blackBoard.angle:
                     variables.get(variableG.getSelectedItem().toString());
                alreadyExecuted=!alreadyExecuted;//quand hookH revient dessus, reinitialisera
            	if(evaluate(this.op, varG) && alreadyExecuted) return next;
                return hookH.next;
            }
            
            void reset(){
                alreadyExecuted=false;
            }
        }//fin classe interne If
        
        
        class CommandWhile extends CommandWithCommands implements ActionListener{//classe interne
            private JComboBox variableG=new JComboBox(), operateur=new JComboBox();
            private String op="<";
            private int whatIsVarG=0, limit=3000;//x<=>0, y<=>1, angle<=>2, variables<=>3 ; pour simuler la terminaison
            
            CommandWhile(int x, int y){
                super("while", new Color(204, 102, 102), x, y);
                super.input=new NumberField(this);

                this.variableG.addActionListener(this);
                String[] tmp={"x", "y", "angle"};
                for(String s : tmp) variableG.addItem(s);
                for(String s : variables.keySet()) variableG.addItem(s);

                this.operateur.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        op=(String)operateur.getSelectedItem();
                    }
                });
                String[] tmpOp={"  <", "  >", " <=", " >=", " =="};
                for(String s : tmpOp) this.operateur.addItem(s);

                this.add(new JLabel("  While  "));
                this.add(variableG);
                this.add(operateur);
                this.add(new JLabel("  "));//pour la presentation
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width-variableG.getPreferredSize().width;
            }
            
            public void actionPerformed(ActionEvent e){
                whatIsVarG=variableG.getSelectedItem().equals("x")?0:
                           variableG.getSelectedItem().equals("y")?1:
                           variableG.getSelectedItem().equals("angle")?2:3;
            }

            Command execute(){
                if(limit==3000) error(false);//enleve fond rouge qu une fois, au debut
                int varG=whatIsVarG==0?blackBoard.x:whatIsVarG==1?blackBoard.y:whatIsVarG==1?
                    blackBoard.angle:variables.get(variableG.getSelectedItem().toString());
                if(evaluate(this.op, varG) && limit-->0) return next;
            	if(limit==0){//si la terminaison a du etre simulee
                    error(true);
                    return null;
                }
                return hookH.next;
            }
            
            private void error(boolean b){
            	if(b){
                    limit=3000;//reinitialise
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
            
            void reset(){
                limit=3000;
            }
        }//fin classe interne While
        
        
        class CommandFunctionInit extends CommandWithCommands implements MouseListener{
            protected JLabel nameFunction;
            private CustomJButton changeName=new CustomJButton("", null, true);//pop up pour changer
            private LinkedList<CommandFunctionCall> caller=new LinkedList<CommandFunctionCall>();
            private boolean alreadyCall;
            
            CommandFunctionInit(String name, int x, int y){
                super("function", new Color(212, 115, 212), x, y-deltaY);
                
                try{
                    Image img=ImageIO.read(new File("images/engrenage.png"));
                    changeName.addImage(img);
                    changeName.setBackground(new Color(212, 115, 212));
                }
                catch(IOException e){}
                changeName.setPreferredSize(new Dimension(commandH-10, commandH-10));
                changeName.addActionListener((event)->{
                    String newName=JOptionPane.showInputDialog("New name ?");
                    if(newName!=null) rename(newName);
                });
                
                this.add(new JLabel("  "));//pour la presentation
                this.add(changeName);
                nameFunction=new JLabel("  "+name+"  ");
                this.add(nameFunction);
                this.setBounds(x, y, getPreferredSize().width, commandH);
            }
            
            void rename(String newName){
                this.nameFunction.setText("  "+newName+"  ");
                this.setSize(this.getPreferredSize().width, commandH);
                for(CommandFunctionCall c : caller) c.initializeDisplay();
            }
            
            void newDrag(){}//deja initialisee sur WhiteBoard
            void stick(){}//ne peut pas etre stick aux autres
            
            Command execute(){
                alreadyCall=!alreadyCall;
                if(alreadyCall) return next;
                return caller.getFirst().next;//first=celui qui a appele la fonction
            }
            
            void reset(){
                alreadyCall=false;
            }
        }//fin classe interne FunctionInit


        /*************************
        *       Draw class       *
        *************************/

        class CommandDrawLine extends Command{//classe interne
            CommandDrawLine(int x, int y){
                super("drawLine", Color.CYAN.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Draw a line of  "));
                this.add(input);
                this.add(new JLabel("  "));//pour la presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width;
            }

            Command execute(){
                int hypotenuse=input.getNumber();
                Vector v=new Vector();//pour creer objet interne
                Point p=v.destinationLine(blackBoard.x, blackBoard.y, blackBoard.angle, hypotenuse);
                
                //ajout du vecteur dans le dessin du joueur
                if(blackBoard.drawing){
                    Vector.VectorLine trait=v.new VectorLine(blackBoard.x,
                        blackBoard.y, p.x, p.y, blackBoard.angle, blackBoard.brushColor);
                    level.addToDraw(trait);
                    if(blackBoard.brush2){//symetrie
                        Vector.VectorLine trait2=v.new VectorLine(400-blackBoard.x,
                            blackBoard.y, 400-p.x, p.y, blackBoard.angle, blackBoard.brushColor);
                        level.addToDraw(trait2);
                    }
                }
                
                //nouvel emplacement du pinceau
                blackBoard.x=p.x;
                blackBoard.y=p.y;
                ViewPlaying.this.blackBoard.repaint();
                return next;
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
                super.commandW=getPreferredSize().width-input.getPreferredSize().width-angleScan.getPreferredSize().width;
            }
            
            boolean canExecute(){
                input.setBorder(variables.isEmpty()?null:borderV);
                angleScan.setBorder(variables.isEmpty()?null:borderV);
                boolean ok=true;
                if(input.isEmpty() || input.getNumber()<2){
                    input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    ok=false;
                }
                if(angleScan.isEmpty() || angleScan.getNumber()<2){
                    angleScan.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    ok=false;
                }
                return ok;
            }

            Command execute(){
                int rad=(input.getNumber()>=0)?input.getNumber():0;
                int angleS=angleScan.getNumber();
                Vector v=new Vector();
                Point center=v.destinationLine(blackBoard.x, blackBoard.y, 180+blackBoard.angle, rad);//milieu du cercle
                Point origin=v.destinationLine(center.x, center.y, blackBoard.angle-sens*90, rad);//-90 pour gauche, +90 pour droite
                Point translation=new Point(blackBoard.x-origin.x, blackBoard.y-origin.y);
                
                if(blackBoard.drawing){//ajout du vecteur dans le dessin du joueur
                    Point square=v.destinationLine(center.x, center.y, 90, rad);//haut du carre
                    square=v.destinationLine(square.x, square.y, 180, rad);//coin gauche du carre
                    square=new Point(square.x+translation.x, square.y+translation.y);//carre translate
                    Vector.VectorArc arc=v.new VectorArc(square.x, square.y, rad*2, 
                        blackBoard.angle-90*sens, sens*angleS, blackBoard.brushColor);//-90*sens car translation
                    level.addToDraw(arc);
                    if(blackBoard.brush2){
                        Vector.VectorArc arc2=v.new VectorArc(400-square.x-rad*2, square.y, rad*2, 
                            180-(blackBoard.angle-90*sens), -sens*angleS, blackBoard.brushColor);
                        level.addToDraw(arc2);
                    }
                }
                
                //nouvel emplacement du pinceau
                Point dest=v.destinationLine(center.x, center.y, blackBoard.angle+(angleS-90)*sens, rad);
                blackBoard.x=dest.x+translation.x;
                blackBoard.y=dest.y+translation.y;
                blackBoard.angle=(angleS*sens+blackBoard.angle)%360;
                ViewPlaying.this.blackBoard.repaint();
                return next;
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

            Command execute(){
                blackBoard.drawing=choiceRes;
                return(next!=null)?next.execute():null;
            }
        }//fin classe interne RaisePutBrush


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
                super.commandW=getPreferredSize().width-input.getPreferredSize().width-positionY.getPreferredSize().width;
            }
            
            boolean canExecute(){
                input.setBorder(variables.isEmpty()?null:borderV);
                positionY.setBorder(variables.isEmpty()?null:borderV);
                boolean isEmpty=input.isEmpty() || positionY.isEmpty();
                if(isEmpty){
                    if(input.isEmpty()) input.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    if(positionY.isEmpty()) positionY.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                }
                return !isEmpty;
            }

            Command execute(){
                blackBoard.x=input.getNumber();
                blackBoard.y=positionY.getNumber();
                ViewPlaying.this.blackBoard.repaint();
                return next;
            }
        }//fin de classe interne MoveTo


        class CommandSetAngle extends Command{//classe interne
            CommandSetAngle(int x, int y){
                super("setAngle", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Set angle to  "));
                this.add(this.input);
                this.add(new JLabel("  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width;
            }

            Command execute(){
                blackBoard.angle=input.getNumber();
                ViewPlaying.this.blackBoard.repaint();
                return next;
            }
        }//fin de classe interne ShiftAngle


        class CommandAddAngle extends Command{//classe interne
            CommandAddAngle(int x, int y){
                super("addAngle", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Add  "));
                this.add(input);
                this.add(new JLabel("  to angle  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width;
            }

            Command execute(){
                blackBoard.angle=(blackBoard.angle+input.getNumber())%360;
                ViewPlaying.this.blackBoard.repaint();
                return next;
            }
        }//fin de classe interne AddAngle


        class CommandSetColor extends Command{//classe interne
            private JComboBox colorChoice=new JComboBox();
            final Color[] palette={Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.RED,Color.WHITE,Color.YELLOW};
            private Color colorRes=level.brushFirstColor;

            CommandSetColor(int x, int y){
                super("setColor", Color.LIGHT_GRAY.darker(), y);
                
                colorChoice.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        for(int i=0; i<7; i++){
                            if(palette[i].equals(colorChoice.getSelectedItem())){
                                colorRes=palette[i];
                                return;
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

            Command execute(){
                blackBoard.brushColor=colorRes;
                ViewPlaying.this.blackBoard.repaint();
                return next;
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
        

        class CommandShiftColor extends Command{
            CommandShiftColor(int x, int y){
                super("shiftColor", Color.LIGHT_GRAY.darker(), y);
                super.input=new NumberField(this);
                
                this.add(new JLabel("  Add  "));
                this.add(this.input);
                this.add(new JLabel("  % to color  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width;
            }
            
            int regularize(int n){
                if(n>255) return 255;
                if(n<0) return 0;
                return n;
            }

            Command execute(){
                int percent=(255*input.getNumber())/100;
                int nRed=regularize(blackBoard.brushColor.getRed()+percent);
                int nGreen=regularize(blackBoard.brushColor.getGreen()+percent);
                int nBlue=regularize(blackBoard.brushColor.getBlue()+percent);
                
                blackBoard.brushColor=new Color(nRed,nGreen,nBlue);
                ViewPlaying.this.blackBoard.repaint();
                return next;
            }
        }//fin de classe interne shiftColor


        class CommandSymmetry extends Command{//classe interne
            private JComboBox choiceBox=new JComboBox();
            private boolean choiceRes;//symetrie off=false, on=true
            
            CommandSymmetry(int x, int y){
                super("symmetry", Color.LIGHT_GRAY.darker(), y);
                
                choiceBox.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED) choiceRes=!choiceRes;
                    }
                });
                choiceBox.addItem(" on ");
                choiceBox.addItem(" off ");
                
                this.add(new JLabel("  Turn  "));
                this.add(choiceBox);
                this.add(new JLabel("  vertical symmetry  "));
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
            }
            
            boolean canExecute(){
                return true;
            }

            Command execute(){
                blackBoard.brush2=choiceRes;
                ViewPlaying.this.blackBoard.repaint();
                return (next!=null)?next.execute():null;
            }
        }//fin classe interne Symmetry
        
        
        class NumberField extends JTextField{
            protected Variable variable;//varibale qu on lui stick
            final Command container;
            final int fieldHeight=30, fieldWidth=50;

            NumberField(Command c){
                super();
                setPreferredSize(new Dimension(fieldWidth, fieldHeight));
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
                return 0;//jamais atteint
            }
            
            boolean isEmpty(){
                if(variable==null) return getText().isEmpty();
                return false;
            }
            
            void resize(){
                setPreferredSize(new Dimension((variable!=null)?
                    variable.getPreferredSize().width:fieldWidth, fieldHeight));
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


        /*************************
        *        Variable        *
        *************************/
        
        class Variable extends JPanel implements MouseInputListener, ActionListener{
            final int positionY, variableW, variableH;
            private int mouseX, mouseY;
            private boolean lastCreated=true;
            private NumberField linkedTo;
            private JComboBox choice=new JComboBox();
            private String name;
            
            Variable(int x, int y){
                this.positionY=y;                
                this.setBackground(new Color(255, 204, 255));
                this.setLayout(new GridBagLayout());
                this.addMouseMotionListener(this);
                this.addMouseListener(this);
                
                choice.setBackground(new Color(255, 204, 255));
                for(String varName : variables.keySet()) choice.addItem(varName);
                choice.setPreferredSize(new Dimension(largerVariable(choice), choice.getPreferredSize().height-5));
                choice.addActionListener(this);
                name=choice.getItemAt(0).toString();
                
                this.add(new JLabel("    "));
                this.add(choice);
                this.add(new JLabel("    "));
                NumberField tmp=new NumberField(null);//juste pour la hauteur
                variableH=tmp.fieldHeight;
                this.setBounds(x, y+deltaY, getPreferredSize().width, variableH);
                variableW=getPreferredSize().width-choice.getPreferredSize().width;
            }

            public void actionPerformed(ActionEvent e){
                this.name=(variables.isEmpty())?"":choice.getSelectedItem().toString();
            }
            
            
            /***** Delete Variable *****/
            
            boolean toDelete(){//quand pres de la poubelle
                if(bin.getLocation().y>getLocation().y+getHeight()) return false;
                if(bin.getLocation().y+bin.getHeight()<getLocation().y) return false;
                if(bin.getLocation().x>getLocation().x+getWidth()) return false;
                return !(bin.getLocation().x+bin.getWidth()<getLocation().x);
            }
            
            void updateBinState() throws IOException{
            	if(this.toDelete()) bin.loadBin("images/openBin.png");
            	else bin.loadBin("images/closedBin.png");
            }
            
            void deleteSteps() throws IOException{//enleve variable du panel
                PanelDragDropBoard.this.remove(this);
                if(lastCreated) addSettedVariables(this.positionY);//regeneration
                bin.loadBin("images/closedBin.png");
                SwingUtilities.updateComponentTreeUI(ViewPlaying.this.dragDrop);//refresh affichage
            }

            
            /***** Stick & Unstick *****/

            NumberField closeCommand(){//this et NumberField sont assez proches pour se coller
                for(NumberField f : fields){
                    if(f.variable==null && closeHeight(f) && closeWidth(f)) return f;
                }
                return null;
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
                    linkedTo.container.resize();
                    fields.remove(linkedTo);//plus disponible pour liaison
                    switchOff();
                }
                int x=linkedTo.container.getX()+linkedTo.getX();
                int y=linkedTo.container.getY()+linkedTo.getY();
                this.setLocation(x, y);
            }
            
            void unStick(){//decolle this (dragged) de son linkedTo
                if(linkedTo==null) return;
                fields.add(linkedTo);//disponible pour liaison
                linkedTo.variable=null;//suppression des liens
                linkedTo.container.resize();
                linkedTo=null;
            }
            
            void resize(){
                setBounds(getX(), getY(), variableW+choice.getPreferredSize().width, variableH);
            }


            /***** Mouse Motion *****/
            
            void switchOff(){//eteint le seul field a eteindre
                brightF.setBorder(borderV);//pour eviter decalage brighter/darker
                brightF=null;
            }
            
            public void mousePressed(MouseEvent e){
                unStick();//detache si etait attache
                mouseX=e.getX();//position initiale de souris
                mouseY=e.getY();
                dragDrop.setToForeground(this);//mettre ce qu on drag a l avant-plan
            }

            public void mouseDragged(MouseEvent e){
                if(variables.isEmpty()) return;//immobile
                int x=e.getXOnScreen()-x00-mouseX-ViewPlaying.this.getX();
                int y=e.getYOnScreen()-y00-mouseY-ViewPlaying.this.getY();
                this.setLocation(x, y);
                
                //allume et eteint les NumberField selon les cas
                if(brightF!=null) switchOff();//eteint tout d abord
                NumberField nearby=closeCommand();
                if(nearby!=null){//proche d un bloc
                    brightF=nearby;//allume le seul necessaire
                    brightF.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
                }
                
                //ouvre eventuellement la poubelle
                try{
                    updateBinState();
                }
                catch(IOException e1){}
            }

            public void mouseReleased(MouseEvent e){
                if(this.toDelete()){
                    try{
                        this.deleteSteps();
                    }
                    catch(IOException e1){}
                }
                else{
                    if(lastCreated && inWhiteBoard(this)){
                        lastCreated=false;
                        addSettedVariables(this.positionY);
                    }
                    if(brightF!=null) stick(true);//accroche
                }
            }
            
            public void mouseMoved(MouseEvent e){}
            public void mouseClicked(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }//fin classe interne Variable
        
        
        class CommandOperationV extends Command implements ActionListener{//classe interne
            protected JComboBox choice=new JComboBox();
            protected String varName;
            
            CommandOperationV(String name, int x, int y, String sign){
                super(name, new Color(255, 153, 194), y);
                super.input=new NumberField(this);
                
                for(String varName : variables.keySet()) choice.addItem(varName);
                choice.addActionListener(this);
                varName=choice.getItemAt(0).toString();
                
                this.add(new JLabel("  "));//presentation
                this.add(choice);
                this.add(new JLabel("  "+sign+"  "));
                this.add(input);
                this.add(new JLabel("  "));//presentation
                this.setBounds(x, y+deltaY, getPreferredSize().width, commandH);
                super.commandW=getPreferredSize().width-input.getPreferredSize().width-choice.getPreferredSize().width;
            }

            public void actionPerformed(ActionEvent e){
                varName=(variables.isEmpty())?"":choice.getSelectedItem().toString();
            }
        }//fin de classe interne OperationVariable


        class CommandAffectation extends CommandOperationV{//classe interne
            CommandAffectation(int x, int y){
                super("affectation", x, y, "=");
            }

            Command execute(){
                variables.replace(varName, input.getNumber());
                ViewPlaying.this.updateVariableDisplay();
                return next;
            }
        }//fin de classe interne Affectation


        class CommandAddition extends CommandOperationV{//classe interne
            CommandAddition(int x, int y){
                super("addition", x, y, "+");
            }

            Command execute(){
                variables.replace(varName, variables.get(varName)+input.getNumber());
                ViewPlaying.this.updateVariableDisplay();
                return next;
            }
        }//fin de classe interne Addition


        class CommandSoustraction extends CommandOperationV{//classe interne
            CommandSoustraction(int x, int y){
                super("soustraction", x, y, "-");
            }

            Command execute(){
                variables.replace(varName, variables.get(varName)-input.getNumber());
                ViewPlaying.this.updateVariableDisplay();
                return next;
            }
        }//fin de classe interne Soustraction


        class CommandMultiplication extends CommandOperationV{//classe interne
            CommandMultiplication(int x, int y){
                super("multiplication", x, y, "*");
            }

            Command execute(){
                variables.replace(varName, variables.get(varName)*input.getNumber());
                ViewPlaying.this.updateVariableDisplay();
                return next;
            }
        }//fin de classe interne Multiplication


        class CommandDivision extends CommandOperationV{//classe interne
            CommandDivision(int x, int y){
                super("division", x, y, "/");
            }

            Command execute(){
                variables.replace(varName, variables.get(varName)/input.getNumber());
                ViewPlaying.this.updateVariableDisplay();
                return next;
            }
        }//fin de classe interne Division
    }//fin classe interne PanelDragDropBoard
}