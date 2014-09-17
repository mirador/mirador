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
  
  public UserLogin(MiraApp app) {
    super();
    this.app = app;
        
    setSize(300, 150);
    setTitle("Sign in");
    setResizable(false);
    setAlwaysOnTop(true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);    
    setLocationRelativeTo(app.frame);
    
//    setUndecorated(true);    
//    setVisible(true);
    
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        UserLogin.this.app.intf.enable();
      }
    });  
    
    
    initPanel();
  }
  
  public void setVisible(boolean b) {
    super.setVisible(b);
    if (b) {
      app.intf.disable();
    } else {
      app.intf.enable();
    }
  }
  
  protected void initPanel() {
    JPanel panel = new JPanel();
        
    panel.setLayout(null);

    JLabel userLabel = new JLabel("Username");
    userLabel.setBounds(10, 10, 80, 25);
    panel.add(userLabel);

    final JTextField userText = new JTextField(20);
    userText.setBounds(100, 10, 190, 25);
    panel.add(userText);

    JLabel passwordLabel = new JLabel("Password");
    passwordLabel.setBounds(10, 40, 80, 25);
    panel.add(passwordLabel);

    final JPasswordField passwordText = new JPasswordField(20);
    passwordText.setBounds(100, 40, 190, 25);
    panel.add(passwordText);

    JButton registerButton = new JButton("What's this?");
    registerButton.setBounds(5, 90, 100, 25);
    panel.add(registerButton);
    
    JButton loginButton = new JButton("Submit");
    loginButton.setBounds(210, 90, 80, 25);
    panel.add(loginButton);
    
    loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        UserLogin.this.setVisible(false);
        app.uploader.setConnected(true);
        app.uploader.setUsername(userText.getText());
        app.uploader.setPassword(new String(passwordText.getPassword()));
        app.uploader.authenticate();
        app.uploader.upload();
        if (app.uploader.isAuthenticated()) {
          UserLogin.this.dispose();    
        } else {
          UserLogin.this.setVisible(true);
        }
      }
    });
    
    registerButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {        
        try {
          String url ="http://mirador.fathom.info/";
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
