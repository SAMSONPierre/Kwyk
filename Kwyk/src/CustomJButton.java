import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JButton;

class CustomJButton extends JButton{
    private String text;
    private Image image;

    CustomJButton(String text, Image image){
        this.text=text;
        this.image=image;
    }
    
    void addImage(Image image){
        this.image=image;
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image==null) return;
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        if(text!=null){
            g.setColor(Color.WHITE);
            FontMetrics metric=g.getFontMetrics();
            int width=metric.stringWidth(text);
            g.drawString(text, (getWidth()-width)/2, (getHeight()+70)/2);
        }
    }
}