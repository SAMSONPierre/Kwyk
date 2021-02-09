package Kwyk;

import java.awt.Color;

public class Board {
    Pixel[][]p;
    
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