import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class WrapLayout extends FlowLayout{
    public WrapLayout(int align, int hgap, int vgap){
        super(align, hgap, vgap);
    }

    public Dimension preferredLayoutSize(Container target){
        return layoutSize(target, true);
    }

    public Dimension minimumLayoutSize(Container target){
        Dimension minimum=layoutSize(target, false);
        minimum.width-=(getHgap()+1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred){
	synchronized(target.getTreeLock()){
            Container container=target;//pour remonter dans la hierarchie
            while(container.getSize().width==0 && container.getParent()!=null)
                container=container.getParent();
            int targetWidth=container.getSize().width;
            if(targetWidth==0) targetWidth=Integer.MAX_VALUE;
            Insets insets=target.getInsets();
            int horizontalInsetsAndGap=insets.left+insets.right+(getHgap()*2);

            //fixe lignes selon largeur disponible
            Dimension dim=new Dimension(0, 0);
            int rowWidth=0;
            int rowHeight=0;
            for(Component m : target.getComponents()){
                if(m.isVisible()){
                    Dimension d=(preferred)?m.getPreferredSize():m.getMinimumSize();
                    if(rowWidth+d.width>targetWidth-horizontalInsetsAndGap){//nouvelle ligne
                        addRow(dim, rowWidth, rowHeight);//donc ajout a la hauteur
                        rowWidth=0;
                        rowHeight=0;
                    }
                    rowWidth+=d.width+((rowWidth>0)?getHgap():0);
                    rowHeight=Math.max(rowHeight, d.height);
                }
            }
            addRow(dim, rowWidth, rowHeight);
            dim.width+=horizontalInsetsAndGap;
            dim.height+=insets.top+insets.bottom+getVgap()*2;
            
            return dim;
	}
    }
    
    private void addRow(Dimension dim, int rowWidth, int rowHeight){
        dim.width=Math.max(dim.width, rowWidth);
        dim.height+=rowHeight+((dim.height>0)?getVgap():0);
    }
}