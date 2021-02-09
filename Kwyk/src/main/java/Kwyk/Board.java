package Kwyk;

import java.awt.Color;

public class Board {
    Pixel[][]p;
    
    Board(int[][]patron) { // on genere directement le patron du dessin a faire : sous-tableaux de la forme {ligne/ordonnee, col/abscisse, code RVB de la couleur}
    	this.p=new Pixel[400][400];
    	int i=0; // curseur pour parcourir chaque sous tableau du patron, donc les coordonnees doivent etre mises dans l ordre (e.g (0,0) < (0,1) < (1,0))
    	for(int row=0; row<p.length; row++) {
    		for(int col=0; col<p[0].length; col++) {
    			if (i<patron.length && row==patron[i][0] && col==patron[i][1]) { // si l on est au niveau d une case a peindre
    				this.p[row][col]=new Pixel(new Color(patron[i][2]), true);
    				i++;
    			}
    			else this.p[row][col]=new Pixel();
    		}
    	}
    }
    
    class Pixel{//class interne
        Color color;//couleur du pixel
        boolean toFill;//true si on doit repasser dessus
                       //à la fin : tableau de false pour valider
        
        Pixel(Color color, boolean toFill){
            this.color=color;
            this.toFill=toFill;
        }
        
        Pixel(){//case noire quelconque
            this(Color.black, false);
        }
        
        void changePixel(Color color){//quand on repasse sur le patron
            if(!toFill && color==this.color) this.toFill=false;
            this.color=color;
        }
    }//fin classe interne Pixel
	
}