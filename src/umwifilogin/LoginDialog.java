package umwifilogin;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldUsername;
	private boolean pressedYes;
	private JPasswordField passwordFieldPassword;
	public JLabel lblTitle;
	
	public LoginDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		setIconImage(umwifilogin.LogoImage);
		setTitle("UM WiFi Auto Login");
		setBounds(100, 100, 362, 152);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pressedYes=true;
				setVisible(false);
				dispose();
			}
		});
		btnOK.setBounds(140, 89, 89, 23);
		contentPanel.add(btnOK);
		
		lblTitle = new JLabel("Previous record not found, please enter your username & password.");
		lblTitle.setBounds(10, 11, 336, 14);
		contentPanel.add(lblTitle);
		
		JLabel lblUsername = new JLabel("Username :");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsername.setBounds(10, 36, 69, 14);
		contentPanel.add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password :");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(10, 61, 69, 14);
		contentPanel.add(lblPassword);
		
		textFieldUsername = new JTextField(umwifilogin.username);
		textFieldUsername.setBounds(89, 33, 257, 20);
		contentPanel.add(textFieldUsername);
		textFieldUsername.setColumns(10);
		
		passwordFieldPassword = new JPasswordField(umwifilogin.password);
		passwordFieldPassword.setBounds(89, 58, 257, 20);
		contentPanel.add(passwordFieldPassword);
	}
	
	public String getUsername() {
		return this.textFieldUsername.getText();
	}
	
	public String getPassword() {
		return new String(this.passwordFieldPassword.getPassword());
	}
	
	public boolean isPressedOK() {
		return this.pressedYes;
	}
}
