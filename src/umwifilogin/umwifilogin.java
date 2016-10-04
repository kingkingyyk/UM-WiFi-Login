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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class umwifilogin {
	
	public static ImageIcon LogoIcon=new ImageIcon(Toolkit.getDefaultToolkit().getClass().getResource("/img-logo.png"));
	public static Image LogoImage=LogoIcon.getImage();
	public static File ConfigFile=new File(System.getProperty("java.io.tmpdir")+"\\UMWiFiLogin.txt");
	public static String Version="v1.2";
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
							String s=br.readLine();
							br.close();
							is.close();
							conn.disconnect();
							if (s!=null && s.contains("unified.um.edu.my")) {
								SysTrayIcn.displayMessage("UM WiFi Auto Login", "UM WiFi detected, logging in...", TrayIcon.MessageType.INFO);
								url=new URL("http://unified.um.edu.my/eportal/InterFace.do?method=login&time=Tue%20Oct%2004%202016%2008:50:45%20GMT+0800%20(Malay%20Peninsula%20Standard%20Time)");
								HttpURLConnection.setFollowRedirects(true);
								conn=(HttpURLConnection) url.openConnection();
								s=s.replace("<script>top.self.location.href='http://unified.um.edu.my/eportal/index.jsp?","").replaceAll("'</script>","");
								s=s.replaceAll("=","%253D").replaceAll("&","%2526");
								
								StringBuilder sb=new StringBuilder();
								sb.append("userId="); sb.append(username); sb.append("&");
								sb.append("password="); sb.append(password); sb.append("&");
								sb.append("service="); sb.append("perdana.um.edu.my"); sb.append("&");
								sb.append("queryString="); sb.append(s); sb.append("&");
								sb.append("operatorPwd="); sb.append("&"); sb.append("validcode=");
								s=sb.toString();
								
								conn.setRequestMethod("POST");
								conn.setRequestProperty("User-Agent","Mozilla/5.0");
								conn.setRequestProperty("Accept-Language","en-US, en;q=0.5");
								conn.setDoOutput(true);
								DataOutputStream wr=new DataOutputStream(conn.getOutputStream());
								wr.writeBytes(s);
								wr.flush();
								wr.close();
								
								is=conn.getInputStream();
								br=new BufferedReader(new InputStreamReader(is));
								while ((s=br.readLine())!=null) {
									System.out.println(s);
								}
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
