package umwifilogin;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class umwifilogin {
	
	public static ImageIcon LogoIcon=new ImageIcon(Toolkit.getDefaultToolkit().getClass().getResource("/img-logo.png"));
	public static Image LogoImage=LogoIcon.getImage();
	public static File ConfigFile=new File(System.getProperty("java.io.tmpdir")+"\\UMWiFiLogin.txt");
	public static String Version="v1.1";
	public static String username;
	public static String password;
	public static boolean lastLoginFailed=false;
	private static TrayIcon SysTrayIcn;
	
	
	public static void main (String [] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		if (!ConfigFile.exists()) {
			CredentialDialog(true);
		} else {
			BufferedReader br=new BufferedReader(new FileReader(ConfigFile));
			username=br.readLine();
			password=br.readLine();
			br.close();
		}
		
		CreateTrayIcon();
		StartLogin();
	}
	
	public static void CredentialDialog(boolean startup){
		LoginDialog diag=new LoginDialog();
		diag.setLocationRelativeTo(null);
		if (!startup) {
			diag.lblTitle.setText("Enter username & password : ");
		}
		diag.setVisible(true);

		if (diag.isPressedOK()) {
			try {
				PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(ConfigFile)));
				username=diag.getUsername();
				password=diag.getPassword();
				pw.println(username);
				pw.println(password);
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastLoginFailed=false;
		} else if (startup) {
			System.exit(0);
		}
	}
	public static void CreateTrayIcon() {
		SystemTray tray=SystemTray.getSystemTray();
		
		PopupMenu popup=new PopupMenu();
		TrayIcon icn=new TrayIcon(LogoImage.getScaledInstance((int)tray.getTrayIconSize().getWidth(), (int)tray.getTrayIconSize().getHeight(), Image.SCALE_SMOOTH),"UM WiFi Auto Login");

		MenuItem watermark=new MenuItem("By kingkingyyk");
		watermark.setEnabled(false);
		popup.add(watermark);
		
		MenuItem versionMark=new MenuItem(Version);
		versionMark.setEnabled(false);
		popup.add(versionMark);
		
		MenuItem changeCredential=new MenuItem("Change account");
		changeCredential.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				CredentialDialog(false);
			}
			
		});
		popup.add(changeCredential);
		
		MenuItem exit=new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
			
		});
		popup.add(exit);
		
		icn.setPopupMenu(popup);
		try {
			tray.add(icn);
		} catch (Exception e) {}
		
		SysTrayIcn=icn;
	}
	
	public static void StartLogin() {
		Thread t=new Thread() {
			public void run() {
				while (true) {
					if (!lastLoginFailed) {
						try {
							URL url=new URL("http://www.google.com");
							HttpURLConnection.setFollowRedirects(true);
							HttpURLConnection conn=(HttpURLConnection) url.openConnection();
							conn.connect();
							InputStream is=conn.getInputStream();
							BufferedReader br=new BufferedReader(new InputStreamReader(is));
							ArrayList<String> htmlCode=new ArrayList<>();
							String s;
							while ((s=br.readLine())!=null && htmlCode.size()<3) {
								htmlCode.add(s);
							}
							br.close();
							is.close();
							conn.disconnect();
							SysTrayIcn.displayMessage("UM WiFi Auto Login", "UM WiFi detected, logging in...", TrayIcon.MessageType.INFO);
							if (htmlCode.size()>=3 && htmlCode.get(2).contains("arubalp=")) {
								WebClient client=new WebClient();
								HtmlPage page=client.getPage("http://www.google.com.my");
								HtmlForm f=page.getForms().get(0);
								HtmlTextInput usernameField=f.getInputByName("user");
								HtmlPasswordInput passwordField=f.getInputByName("password");
								usernameField.setValueAttribute(username);
								passwordField.setValueAttribute(password);
								HtmlCheckBoxInput box=f.getInputByName("visitor_accept_terms");
								box.setChecked(true);
								HtmlPage page2=f.getInputByValue("Log In").click();
								page.cleanUp();
								if (page2.getBaseURL().toString().contains("umwifilogin")) {
									SysTrayIcn.displayMessage("UM WiFi Auto Login", "Log in failed. Please check your credential. :)", TrayIcon.MessageType.ERROR);
									lastLoginFailed=true;
								} else {
									SysTrayIcn.displayMessage("UM WiFi Auto Login", "Login success! :)", TrayIcon.MessageType.INFO);
									lastLoginFailed=false;
								}
								page2.cleanUp();
								client.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
				}
			}
		};
		t.start();
	}
	
}
