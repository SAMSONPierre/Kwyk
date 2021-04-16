import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VariablePanel extends JPanel{
    private JLabel varName;
    private JLabel varValue;
    
    VariablePanel(String name, int value){
        varName=new JLabel(name);
        varValue=new JLabel(Integer.toString(value));
        add(varName);
        add(new JLabel(" = "));
        add(varValue);
        setBackground(new Color(255, 204, 255));
        setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 1));
    }
    
    String getVarName(){
        return varName.getText();
    }
    
    void setValue(int n){
        varValue.setText(Integer.toString(n));
    }
}