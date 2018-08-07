package guiDemo;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class FilterForm extends JFrame {

	private MainForm parent;

	public FilterForm(MainForm mf) {
		parent = mf;
	}

	public void FilterFrame() {

		JButton ok = new JButton("OK");
		JPanel panel = new JPanel(new BorderLayout());
		JPanel filter_pan = new JPanel(new GridLayout(3, 1));

		JPanel attr_pan = new JPanel(new BorderLayout());
		final JTextField attr_fld = new JTextField(30);
		attr_pan.add(new Label("Аттрибуты:"), BorderLayout.WEST);
		attr_pan.add(attr_fld, BorderLayout.CENTER);

		JPanel filt_pan = new JPanel(new BorderLayout());
		final JTextField filt_fld = new JTextField(30);
		filt_pan.add(new Label("Фильтр:"), BorderLayout.WEST);
		filt_pan.add(filt_fld, BorderLayout.CENTER);

		JPanel preset_pan = new JPanel(new BorderLayout());
		final JComboBox<String> preset_fld = new JComboBox<String>();
		preset_pan.add(new Label("Интересные:"), BorderLayout.WEST);
		preset_pan.add(preset_fld, BorderLayout.CENTER);
	
		
		filter_pan.add(attr_pan);
		filter_pan.add(filt_pan);
		filter_pan.add(preset_pan);

		attr_fld.setText(parent.ATTRIB);
		filt_fld.setText(parent.FILTER);

		preset_fld.addItem(parent.prop.getProp("ldap.filter2"));
		preset_fld.addItem(parent.prop.getProp("ldap.filter3"));
		preset_fld.addItem(parent.prop.getProp("ldap.filter4"));
        //https://social.technet.microsoft.com/wiki/contents/articles/8077.active-directory-ldap-ru-ru.aspx

		panel.add(filter_pan, BorderLayout.CENTER);
		panel.add(ok, BorderLayout.SOUTH);

		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.ATTRIB = attr_fld.getText();
				parent.FILTER = filt_fld.getText();
				parent.refresh();
				setVisible(false);
			}
		});

		preset_fld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				filt_fld.setText(preset_fld.getSelectedItem().toString());
				parent.FILTER = filt_fld.getText();
			}
		});
		
		add(panel);
		pack();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(true);
	}
}
