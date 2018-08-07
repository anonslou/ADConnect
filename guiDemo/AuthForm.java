package guiDemo;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.naming.AuthenticationException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import adProvider.ADConnection;
import enter.Prop;

@SuppressWarnings("serial")
public class AuthForm extends JFrame {

	private JTextField login_fld;
	private JPasswordField pass_fld;
	
	public Prop prop = null;

	public void AuthFrame() {
		
		try {
			 prop = new Prop("src/config.properties");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(AuthForm.this, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			System.exit(1);
		}
		
		JButton enterBt = new JButton("Вход");
		JPanel panel = new JPanel(new BorderLayout());

		JPanel auth_pan = new JPanel(new GridLayout(2, 1));

		JPanel login_pan = new JPanel(new BorderLayout());
		login_fld = new JTextField(30);
		login_pan.add(new Label("Логин:"), BorderLayout.WEST);
		login_pan.add(login_fld, BorderLayout.EAST);

		JPanel pass_pan = new JPanel(new BorderLayout());
		pass_fld = new JPasswordField(30);
		pass_pan.add(new Label("Пароль:"), BorderLayout.WEST);
		pass_pan.add(pass_fld, BorderLayout.EAST);

		login_fld.setText(prop.getProp("ad.login"));
		pass_fld.setText("");

		auth_pan.add(login_pan);
		auth_pan.add(pass_pan);

		panel.add(auth_pan, BorderLayout.CENTER);
		panel.add(enterBt, BorderLayout.SOUTH);

		enterBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// TODO Добавить вход по нажатию Enter на клавиатуре...

				ADConnection adao = new ADConnection(prop);
				adao.setSERVER(prop.getProp("ad.server"));
				adao.setROOT(prop.getProp("ad.domain"));
				if ( prop.getProp("ad.ssl").equals("false")) {
					adao.useSSL(false);
				} else {
					adao.useSSL(true);
				}
				adao.setPORT(prop.getProp("ad.port")); // если не сделать, то useSSL установит порт 636 для ssl или 389 в противном случае
				adao.setLOGIN(login_fld.getText());
				adao.setPASS(new String(pass_fld.getPassword()));

				
				try {
					adao.connect();
				} catch (AuthenticationException e1) {
					JOptionPane.showMessageDialog(AuthForm.this, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}

				MainForm mf = new MainForm(prop);
				mf.MainFrame(adao);
				AuthForm.this.setVisible(false);
			}
		});

		add(panel);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

}
