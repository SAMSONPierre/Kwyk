import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ViewSummaryTraining extends ViewGame{//sommaire des exercices
    final int buttonHeight=super.getButtonHeight();//hauteur d un bouton
    
    ViewSummaryTraining(Player player){
        super(player);
        sommaire();
    }
    
    ViewSummaryTraining(Player player, String path){
        super(player);
        listeNiveau(path, player.getCurrentLevel());
    }
    
    void sommaire(){
        JPanel summary=new JPanel(new FlowLayout());
        summary.setBounds(0,50+buttonHeight,widthFS,heightFS);
        File[] arrayLevels=nombreNiveau("levels/training/");
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length());
                File[] arrayLevels2=nombreNiveau("levels/training/"+name+"/");
                JButton jb;
                if(arrayLevels2.length != 0) {
                	String name2=arrayLevels2[0].getName().substring(0, arrayLevels2[0].getName().length()-3);
                    Image img=ImageIO.read(new File("preview/training/"+name+"/"+name2+"png"));
                    boolean done = isDone(i,arrayLevels2.length);
                    jb=new CustomJButton(name.substring(name.indexOf("_")+1),img,done);
                }
                else {
                	jb = new JButton(name.substring(name.indexOf("_")+1));
                }
                jb.setPreferredSize(new Dimension(200, 200));
                jb.addActionListener((event)->super.control.switchTraining(name+"/"));
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(summary);
    }
    
    void listeNiveau(String path, boolean[][] currentL){
        JPanel summary=new JPanel(new WrapLayout(WrapLayout.CENTER, 50, 50));
        JScrollPane scrollP=new JScrollPane(summary, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollP.setBounds(0, 50+buttonHeight, widthFS, heightFS);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);//vitesse de scroll
        int directory=Integer.parseInt(path.charAt(0)+"")-1;
        File[] arrayLevels=sortedFiles(path);
        for(int i=0; i<arrayLevels.length; i++){
            try{
                String name=arrayLevels[i].getName().substring(0, arrayLevels[i].getName().length()-4);
                Image img=ImageIO.read(new File("preview/training/"+path+name+".png"));
                CustomJButton jb=new CustomJButton(name.substring(name.indexOf('-')+1), img, currentL[directory][i+1]);
                jb.setEnabled(currentL[directory][i]);
                jb.addActionListener((event)->super.control.load("training/"+path+name));
                jb.setPreferredSize(new Dimension(200, 200));
                summary.add(jb);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        this.add(scrollP);
    }
    
    File[] sortedFiles(String path){//sinon niveau 11 avant niveau 2 par exemple
        File[] toChange=nombreNiveau("levels/training/"+path);
        File[] res=new File[toChange.length];
        for(int i=0; i<res.length; i++){
            String name=toChange[i].getName();
            res[Integer.parseInt(name.substring(0, name.indexOf('-')-1))]=toChange[i];
        }
        return res;
    }
    
    boolean isDone(int i,int length) {
    	boolean[] array = this.getModel().getPlayer().getCurrentLevel()[i];
    	for(int j=0;j<length+1;j++) {
    		if(!array[j]) return false;
    	}
    	return true;
    }
}