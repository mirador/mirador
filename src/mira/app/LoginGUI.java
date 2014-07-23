package mira.app;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginGUI {

	static String username;
	static String password;
	static boolean user_authenticated = false;
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("SEErador - Login");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);
		placeComponents(panel);

		frame.setVisible(true);
	}

	private static void placeComponents(JPanel panel) {

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
		
		loginButton.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent event){
	    		
	    		username = userText.getText();
	    		password = passwordText.getText();
	    		
	    		HttpClientExample client = new HttpClientExample();
	    		try {
					user_authenticated = client.authenticate(username,password);
					System.out.println(user_authenticated);
				} catch (Exception e) {
					e.printStackTrace();
				}
	    		
	    		//(new Thread(mirac)).start();
	    	
	    	}
	    	
	    });
		registerButton.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent event){
	    		
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
		
		
	}

}