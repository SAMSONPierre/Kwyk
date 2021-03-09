import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JButton;

class CustomJButton extends JButton{
    private String text;
    private Image image;
    private boolean done;

    CustomJButton(String text, Image image,boolean done){
        this.text=text;
        this.image=image;
        this.done = done;
    }
    
    void addImage(Image image){
        this.image=image;
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image==null) return;
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        if(text!=null){
        	if(done) {
        		g.setColor(Color.GREEN);
        	}
        	else {
        		g.setColor(Color.gray);
        	}
            
            FontMetrics metric=g.getFontMetrics();
            int width=metric.stringWidth(text);
            g.drawString(text, (getWidth()-width)/2, (getHeight()+70)/2);
        }
    }
}