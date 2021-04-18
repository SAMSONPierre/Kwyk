import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ViewLogin extends View{
    private JTextField username;//champ pour remplir l username
    private JPasswordField password;//champ pour remplir le mot de passe
    //boutons pour se connecter, creer un compte, jouer sans compte :
    private JButton login, createAccount, tryWithoutAccount;
    private JLabel error;//message qui s affiche en cas d erreur d username/password
    
    ViewLogin(){//aucun Player pour l instant
        super(null);
        initialisationFieldsProperties();//initialisation des champs de saisie id et pwd
        initialisationButtons();//initialisation des boutons login et create account
        error=new JLabel("");//vide au debut
    	error.setForeground(Color.red);//les erreurs seront ecrites en rouge
        setPage();//ajout de tous les elements
    }
    
    void initialisationFieldsProperties(){
    	username=new JTextField();
    	username.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e){
                if(containsSpecialChar(username) && !isDeleteKey(e)) errorSpecialChar(username);
                else if(!usernameStartOk()) errorFirstCharUsername();
                else if(!containsSpecialChar(username) && usernameStartOk() && !error.getText().equals(""))
                    resetError(username);
            }
            
            public void keyPressed(KeyEvent e) {
            	if(e.getKeyCode()==KeyEvent.VK_ENTER) login.doClick();
            }
    	});
        
    	password=new JPasswordField();
    	password.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e){
                if(containsSpecialChar(password) && !isDeleteKey(e)) errorSpecialChar(password);
                else if(!containsSpecialChar(password) && isDeleteKey(e) && !error.getText().equals("")) 
                    resetError(password);
            }
            
            public void keyPressed(KeyEvent e) {
            	if(e.getKeyCode()==KeyEvent.VK_ENTER) login.doClick();
            }
    	});
    	
    	//taille des composants
    	Dimension textSize=new Dimension(150, username.getPreferredSize().height);//largeur, hauteur
    	username.setPreferredSize(textSize);
    	password.setPreferredSize(textSize);
    }
    
    void initialisationButtons(){
    	createAccount=new JButton("Create Account");
    	createAccount.addActionListener((event)->{
            String usernameS=username.getText();
            String passwordS=new String(password.getPassword());//char[] en String
            if(usernameS.equals(passwordS) && usernameS.length()!=0) errorSafety();
            else if(canCreateAccount()) super.control.createAccount(usernameS, passwordS);
            else errorLogin();
    	});
        
    	login=new JButton("Log In");
    	login.setPreferredSize(createAccount.getPreferredSize());
    	login.addActionListener((event)->{
            String usernameS=username.getText();
            String passwordS=new String(password.getPassword());//char[] en String
            if(!usernameS.equals("") && password.getPassword().length!=0) control.login(usernameS, passwordS);
            else errorLogin();
    	});
    	login.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent e) {
            	if(e.getKeyCode()==KeyEvent.VK_ENTER) login.doClick();
            }
        });
        
    	tryWithoutAccount=new JButton("Try without account");
    	tryWithoutAccount.setFont(new Font("Arial", Font.ITALIC, 16)); 	   	
    	tryWithoutAccount.addActionListener((event)->super.control.login("default", "default"));
    }
    
    void setPage(){
        JPanel center=new JPanel(new GridBagLayout());//contiendra tout, sauf tryWithoutAccount
        GridBagConstraints constraints=new GridBagConstraints();//contraintes d ajout dans center
        
        //en cas d erreur d username/password, s affiche tout en haut dans center
        center.add(error);
        
        //sinon username apparait tout en haut dans center
        JPanel line1=new JPanel();
        line1.add(new JLabel("Your username :"));
        line1.add(username);
        constraints.gridy=1;//ligne suivante
        center.add(line1, constraints);
        
        //juste en bas d username
        JPanel line2=new JPanel();
        line2.add(new JLabel("Your password :"));
        line2.add(password);
        constraints.gridy=2;//ligne suivante
        center.add(line2, constraints);
        
        //boutons login et createAccount sur la même ligne, en bas de password
        JPanel line3=new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));//alignement, hgap, vgap
        line3.add(login);
        line3.add(new JLabel("or"));
        line3.add(createAccount);
        constraints.gridy=3;//ligne suivante
        center.add(line3, constraints);
        
        //bouton tryWithoutAccount en fin de page, tout a droite
        JPanel end=new JPanel(new FlowLayout(FlowLayout.RIGHT));//ajout a droite
        end.add(tryWithoutAccount);        
        
        this.setLayout(new BorderLayout());
        this.add(center);
        this.add(end, BorderLayout.SOUTH);
        
        this.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent e) {
            	if(e.getKeyCode()==KeyEvent.VK_ENTER) login.doClick();
            }
        });
    }
    
    boolean canCreateAccount() {
    	return !username.getText().equals("") && password.getPassword().length!=0 
            && !containsSpecialChar(username) && !containsSpecialChar(password) && usernameStartOk();
    }
    
    boolean usernameStartOk(){
    	return username.getText().isEmpty() || Character.isLetter(username.getText().charAt(0));
    }
    
    boolean containsSpecialChar(JTextField source){
    	for(int i=0; i<source.getText().length(); i++){
            if(!(Character.isLetterOrDigit(source.getText().charAt(i)))) return true;
    	}
    	return false;
    }
    
    boolean isDeleteKey(KeyEvent e){
        return e.getKeyCode()==KeyEvent.VK_BACK_SPACE || e.getKeyCode()==KeyEvent.VK_DELETE;
    }
    
    
    /*********************************
    *  differents messages d'erreur  *
    *********************************/
    
    void resetError(JTextField source){//supprimer le message d erreur quand on enleve erreur
    	source.setBorder(null);
    	error.setText("");
    }
    
    void errorLogin(){//incorrect ou username=default(==nom du fichier pour jouer sans compte)
        error.setText("<html><i>Incorrect username/password.</i></html>");
    }
    
    void errorSpecialChar(JTextField errorSource){//saisie de caracteres speciaux (interdite)
    	error.setText("<html><i>Special characters are not allowed.</i></html>");
    	errorSource.setBorder(BorderFactory.createLineBorder(Color.RED.darker(), 3));
    }
    
    void errorFirstCharUsername() {
    	error.setText("<html><i>Username must start with a letter.</i></html>");
    	username.setBorder(BorderFactory.createLineBorder(Color.RED.darker(), 3));    	
    }
    
    void errorSafety(){
    	error.setText("<html><i>For safety reasons, please choose an username different from your password.</i></html>");
    	username.setBorder(BorderFactory.createLineBorder(Color.RED.darker(), 3));    	
    	password.setBorder(BorderFactory.createLineBorder(Color.RED.darker(), 3));    	
    }
    
    void usernameAlreadyExists(){
        this.error.setText("<html><i>Please choose another username.</i></html>");
    }
}