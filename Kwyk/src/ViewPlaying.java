import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JPanel;

//autres fonctionnalités (boutons) à mettre dans l espace en bas de BlackBoard (x€[20 ; 420] et y€[440+buttonHeight+getInsets().top ; height])
public class ViewPlaying extends ViewGame{
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    final int heightFS;//hauteur de l ecran, sans getInsets().top=barre superieur de la fenetre
    final int widthFS;//largeur de l ecran
    private PanelBlackBoard blackBoard;//patron + visualisation du resultat du code
    private PanelDragDropBoard dragDrop;
    JPanel features=new JPanel();//panel avec tous les boutons sous BlackBoard
    
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
        blackBoard=new PanelBlackBoard(buttonHeight);
        this.add(blackBoard);
        
        //taille relative a l ecran
        int height=heightFS-40-buttonHeight;//40=marges haut+bas
        int width=widthFS-460;//460=3 marges de colonne + taille blackBoard
        dragDrop=new PanelDragDropBoard(width, height, buttonHeight, getInsets().top);
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
}
