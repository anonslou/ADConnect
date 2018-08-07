package guiDemo;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import adProvider.ADConnection;
import enter.Prop;


@SuppressWarnings("serial")
public class MainForm extends JFrame {
	
	public Prop prop = null;
	public MainForm(Prop prop) {
		this.prop = prop;
	}

	public String FILTER = null;
	public String ATTRIB = null;

	private TaibleModelMainForm model = null;
	private JTable table = null;

	public void refresh() {
		model.setADAttribure(ATTRIB);
		model.setADFilter(FILTER);
		model.getData();

		model.fireTableDataChanged();
		model.fireTableStructureChanged();
		table.createDefaultColumnsFromModel();

		table.validate();
		table.repaint();
		validate();
		repaint();
	}

	public void MainFrame(ADConnection aobj) {

		FILTER = prop.getProp("ldap.filter"); // фильтр по умолчанию, который сработает сразу после запуска
		ATTRIB = prop.getProp("ldap.attrib"); // Значение lastLogon сейчас берется только с двух серверов...
		
		JButton filter = new JButton("Фильтры");
		JButton export = new JButton("Экспорт в CSV");
		JPanel panel = new JPanel(new BorderLayout());

		if (model == null) {
			model = new TaibleModelMainForm();
		}
		model.setAuthObj(aobj);
		model.setADAttribure(ATTRIB);
		model.setADFilter(FILTER);
		model.getData();
		table = new JTable(model);

		JScrollPane spane = new JScrollPane(table);
		panel.add(spane, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.add(filter);
		buttons.add(export);
		panel.add(buttons, BorderLayout.NORTH);

		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterForm ff = new FilterForm(MainForm.this);
				ff.FilterFrame();
			}
		});

		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (fc.showSaveDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
					model.exportToCSVfile(fc.getSelectedFile(), ";", "cp1251");
				}
			}
		});

		add(panel);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

}
