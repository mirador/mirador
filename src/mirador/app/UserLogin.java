package mirador.app;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class UserLogin extends JFrame {
  protected MiraApp app;
  
  public UserLogin(String tileString, MiraApp app) {
    super();
    this.app = app;
    
    setSize(300, 150);
    setTitle(tileString);
//    setVisible(true);
    
    initPanel();
  }
  
  protected void initPanel() {
    JPanel panel = new JPanel();
        
    panel.setLayout(null);

    JLabel userLabel = new JLabel("User");
    userLabel.setBounds(10, 10, 80, 25);
    panel.add(userLabel);

    final JTextField userText = new JTextField(20);
    userText.setBounds(100, 10, 160, 25);
    panel.add(userText);

    JLabel passwordLabel = new JLabel("Password");
    passwordLabel.setBounds(10, 40, 80, 25);
    panel.add(passwordLabel);

    final JPasswordField passwordText = new JPasswordField(20);
    passwordText.setBounds(100, 40, 160, 25);
    panel.add(passwordText);

    JButton loginButton = new JButton("Login");
    loginButton.setBounds(5, 80, 80, 25);
    panel.add(loginButton);
    
    JButton registerButton = new JButton("What's this?");
    registerButton.setBounds(160, 80, 100, 25);
    panel.add(registerButton);    
    
    loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        app.uploader.setConnected(true);
        app.uploader.setUsername(userText.getText());
        app.uploader.setPassword(new String(passwordText.getPassword()));
        app.uploader.authenticate();
        app.uploader.upload();
        }
    });
    
    registerButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {        
        try {
          String url ="http://localhost/classes/access_user/register.php";
          Desktop dt = Desktop.getDesktop();
          URI uri = new URI(url);
          dt.browse(uri.resolve(uri));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {
        }      
      }      
    });
    
    add(panel);    
  }
}
