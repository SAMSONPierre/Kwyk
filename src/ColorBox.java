import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class ColorBox extends JComboBox{
    final Color[] palette={Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.RED,Color.WHITE,Color.YELLOW};
    protected Color colorRes=palette[0];
    
    ColorBox(){
        super();
        addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                colorRes=palette[getSelectedIndex()];
            }
        });
        for(int i=0; i<7; i++) addItem(palette[i]);
        setRenderer(new ColorComboRenderer());
    }
    
    
    class ColorComboRenderer extends JPanel implements ListCellRenderer{
        Color main=Color.BLUE;//couleur en tete d affichage

        ColorComboRenderer(){
            super();
            this.setPreferredSize(new Dimension(30, 15));
            setBorder(new CompoundBorder(new LineBorder(Color.WHITE), new LineBorder(Color.BLACK)));
        }

        public Component getListCellRendererComponent(JList l,Object o,int row,boolean b,boolean focus){
            if(o instanceof Color) main=(Color)o;
            return this;
        }

        public void paint(Graphics g){
            setBackground(main);
            super.paint(g);
        }
    }
}