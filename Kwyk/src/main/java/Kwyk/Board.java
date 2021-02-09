package Kwyk;

import java.awt.Color;

public class Board {//modele a reproduire
    Pixel[][]board;
    
    Board(int[][] patron){//on genere le patron du dessin a faire : sous-tableaux de la forme {ligne/ordonnee, col/abscisse, code RVB de la couleur}
        this.board=new Pixel[400][400];
        int i=0;//curseur pour parcourir chaque sous tableau du patron, donc coordonnees dans l ordre (e.g (0,0) < (0,1) < (1,0))
        for(int row=0; row<board.length; row++){
            for(int col=0; col<board[0].length; col++){
                if(i<patron.length && row==patron[i][0] && col==patron[i][1]){//au niveau d une case a peindre
                    this.board[row][col]=new Pixel(new Color(patron[i][2]));
                    i++;
                }
                else this.board[row][col]=new Pixel(Color.black);
            }
        }
    }
    
    class Pixel{//class interne
        Color color;//couleur du pixel
        boolean toFill;//true=point à tracer, a la fin : tableau de false pour valider
        
        Pixel(Color color){
            this.color=color;
            this.toFill=(color!=Color.black);//case noire pas a repeindre par defaut
        }
        
        void changePixel(Color color){//quand on repasse sur le patron
            if(!toFill && color.darker()==this.color) //la couleur du patron est plus foncee
                this.toFill=false;
            this.color=color;
        }
    }//fin classe interne Pixel
	
}
