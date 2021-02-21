import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViewPlaying extends View{
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelWhiteBoard whiteBoard;//ecriture du code avec blocs de commandes
    private PanelCommandBoard commandBoard;//blocs de commande disponibles
    private JButton training, challenge, create, logout;//boutons pour acceder aux autres pages
    
    ViewPlaying(Player player){
        super(player);        
        setButton();//initialise les boutons
        setTop();//construit la barre du haut avec les boutons
        addBoard();//ajout des 3 tableaux, avec des marges de 20 (haut, bas et entre tableaux)
    }
    
    void setButton(){
        training=new JButton("Training");
        training.addActionListener((event)->super.control.switchTraining());
        challenge=new JButton("Challenge");
        challenge.addActionListener((event)->super.control.switchChallenge());
        create=new JButton("Create");
        create.addActionListener((event)->super.control.switchCreate());
        logout=new JButton("Log out");
        logout.addActionListener((event)->super.control.logout());
    }
    
    void setTop(){
        JPanel topBar=new JPanel();
        topBar.setLayout(new GridLayout());
        int width=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
        topBar.setBounds(0, 0, width, create.getPreferredSize().height);//hauteur d un bouton
        topBar.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
        this.add(topBar);
        
        //pannel de gauche
        JPanel left=new JPanel();//training, challenge, create
        left.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.add(training);
        left.add(challenge);
        left.add(create);
        topBar.add(left);
        
        //panel de droite
        JPanel right=new JPanel();//username, progression, bouton logout
        right.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(new JLabel(super.getModel().getPlayer().username));
        right.add(new JLabel("     "));//separation entre username et barre de progression
        /* ... barre de progression a ajouter plus tard */
        right.add(new JLabel("     "));//separation entre barre de progression et bouton
        right.add(logout);
        topBar.add(right);
    }
    
    void addBoard(){
        int heightButt=create.getPreferredSize().height;//hauteur d un bouton
        
        //taille fixee
        blackBoard=new PanelBlackBoard(heightButt);
        this.add(blackBoard);
        
        //taille relative a l ecran
        Rectangle r=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();//plein écran
        int width=(r.width-480)/2;//480=4 marges de colonne + taille blackBoard
        int height=r.height-65-this.getInsets().top;//65=marges haut+bas+hauteur topBar, getInsets().top=barre supérieur de la fenetre
        
        whiteBoard=new PanelWhiteBoard(width, height, heightButt);
        this.add(whiteBoard);
        commandBoard=new PanelCommandBoard(width, height, heightButt);
        this.add(commandBoard);
    }
}
