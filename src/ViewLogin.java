import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ViewLogin extends View{
    private JTextField username;//champ pour remplir l username
    private JPasswordField password;//champ pour remplir le mot de passe
    //boutons pour se connecter, creer un compte, jouer sans compte :
    private JButton login, createAccount, tryWithoutAccount;
    private JLabel error;//message qui s affiche en cas d erreur d username/password
    
    ViewLogin(){//aucun Player pour l instant
        super(null);
        initialisation();//initialisation des elements
        setPage();//ajout de tous les elements
    }
    
    void initialisation(){
        username=new JTextField();
        password=new JPasswordField();
        login=new JButton("Log In");
        createAccount=new JButton("Create Account");
        tryWithoutAccount=new JButton("Try without account");
        tryWithoutAccount.setFont(new Font("Arial", Font.ITALIC, 16));
        error=new JLabel("");//vide au debut
        error.setForeground(Color.red);//les erreurs seront ecrites en rouge
        
        //taille des composants
        Dimension textSize=new Dimension(150, username.getPreferredSize().height);//largeur, hauteur
        username.setPreferredSize(textSize);
        password.setPreferredSize(textSize);
        login.setPreferredSize(createAccount.getPreferredSize());
        
        //ajout des listeners
        login.addActionListener((event)->{
            String usernameS=username.getText();
            String passwordS=new String(password.getPassword());//char[] en String
            if(!usernameS.equals("") && !password.equals(""))
                super.control.login(usernameS, passwordS);
            else errorLogin();
        });
        createAccount.addActionListener((event)->{
            String usernameS=username.getText();
            String passwordS=new String(password.getPassword());//char[] en String
            if(!usernameS.equals("") && !password.equals(""))
                super.control.createAccount(usernameS, passwordS);
            else errorLogin();
        });
        tryWithoutAccount.addActionListener((event)->super.control.tryWithoutAccount());
    }
    
    void setPage(){
        JPanel center=new JPanel();//panneau du centre (contiendra tout, sauf tryWithoutAccount)
        center.setLayout(new GridBagLayout());
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
        JPanel line3=new JPanel();
        line3.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));//alignement, hgap, vgap
        line3.add(login);
        line3.add(new JLabel("or"));
        line3.add(createAccount);
        constraints.gridy=3;//ligne suivante
        center.add(line3, constraints);
        
        //bouton tryWithoutAccount en fin de page, tout a droite
        JPanel end=new JPanel();
        end.setLayout(new FlowLayout(FlowLayout.RIGHT));//ajout a droite
        end.add(tryWithoutAccount);        
        
        this.setLayout(new BorderLayout());
        this.add(center);
        this.add(end, BorderLayout.SOUTH);
    }
    
    void errorLogin(){//incorrect ou username=default(==nom du fichier pour jouer sans compte)
        error.setText("<html><i>Incorrect username/password.</i></html>");
    }
    
    void usernameAlreadyExists(){
        this.error.setText("<html><i>Please choose another username.</i></html>");
    }
}