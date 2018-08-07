package guiDemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.AuthenticationException;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import adProvider.ADConnection;
import adProvider.ADQuery;
import adProvider.ADUtils;

@SuppressWarnings("serial")
public class TaibleModelMainForm extends AbstractTableModel {

	ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

	private String filter = null;
	private String attrib = null;

	private static final String LASTLOGON = "lastLogon";
	private static final String PING = "ping";
	private static final String IP = "ip";
	// TODO добавить разбор значения атрибута userAccountControl

	public void setLogin(String login) {

	}

	private ADConnection server;

	public void setAuthObj(ADConnection adao) {
		this.server = adao;
	}

	public void getData() {

		ADConnection secondServer = server.copy();
		// Второй сервер AD 
		secondServer.setSERVER(server.getProp().getProp("ad.server2"));
		// TODO - находить все сервера и собирать данные со всех

		try {
			server.connect();
			secondServer.connect();
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}

		data.clear();

		ADQuery query = new ADQuery(server);
		ArrayList<String> list = query.getListDN(filter);
		Iterator<String> i = list.iterator();
		ArrayList<String> line = null;
		while (i.hasNext()) {
			String curObj = i.next();
			//for debug all attribute System.out.println(query.getAttributeByNames(curObj, null));
			Hashtable<String, Object> attr = query.getAttributeByNames(curObj, attrib);
			line = new ArrayList<String>();

			String[] attrFilt = (String[]) attrib.replaceAll(" ", "").split(",");
			for (int j = 0; j < attrFilt.length; j++) {

				String curAttr = attrFilt[j];
				if (LASTLOGON.equalsIgnoreCase(curAttr)) {
					// Собираем значение lastLogon с двух серверов и берем самый свежий. 
					Calendar calSecond = null;
					Calendar cal = null;

					ADQuery secondQuery = new ADQuery(secondServer);
					Hashtable<String, Object> attrSecond = secondQuery.getAttributeByNames(curObj, LASTLOGON);
					String lastLogon = (String) attrSecond.get(LASTLOGON);

					// from second server
					// TODO - находить все сервера и собирать данные со всех
					if (lastLogon != null) {
						Long timeSecond = new Long(lastLogon);
						cal = ADUtils.getTime(timeSecond);
					}

					// from server
					if (attr.get(curAttr) != null) {
						Long time = new Long((String) attr.get(curAttr));
						calSecond = ADUtils.getTime(time);

						if (cal == null || cal.before(calSecond)) {
							cal = calSecond;
						}
					}

					if (cal != null) {
						SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd");
						line.add(sdt.format(cal.getTime()).toString());
					} else {
						line.add(null);
					}

				} else if (IP.equalsIgnoreCase(curAttr)) {
					// псевдоатрибут, который добавляет в таблицу ip адрес объекта (должен резолвиться в DNS!)
					// вырезаем CN из DN
					String cn = curObj.substring(curObj.indexOf("CN=") + 3, curObj.indexOf(","));
					try {
						line.add(ADUtils.getIPbyName(cn));
					} catch (UnknownHostException e) {
						line.add("Unknown Host");
					}

				} else if (PING.equalsIgnoreCase(curAttr)) {
					// TODO - псевдоатрибут, проверки доступности объекта онлайн (объект должен резолвиться)
					line.add("offline");

				} else {
					line.add((String) attr.get(curAttr));
				}
			}
			data.add(line);
		}
		secondServer.close();
		server.close();
		System.out.println(data.size()); // TODO по умолчанию запрос возвращет 1000 строк
	}

	public void exportToCSVfile(File file, String delim, String codepage) {

		if (codepage == null) {
			codepage = "UTF8";
		}
		if (delim == null) {
			delim = ";";
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos, codepage);
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write(filter + "\n");
			bw.write(attrib + "\n");

			for (int k = 0; k < data.size(); k++) {
				for (int l = 0; l < data.get(k).size(); l++) {
					if (l != data.get(k).size() - 1) {
						if (data.get(k).get(l) != null) {
							bw.write(data.get(k).get(l) + delim);
						} else {
							bw.write(delim);
						}
					} else {
						if (data.get(k).get(l) != null) {
							bw.write(data.get(k).get(l));
						} else {
							bw.write("");
						}
					}
				}
				bw.write("\n");
			}

			bw.close();
			osw.close();
			fos.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setADFilter(String filter) {
		this.filter = filter;
	}

	public void setADAttribure(String attrib) {
		this.attrib = attrib;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		String[] attributeFilter = (String[]) attrib.replaceAll(" ", "").split(",");
		return attributeFilter.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String[] attributeFilter = (String[]) attrib.replaceAll(" ", "").split(",");
		return attributeFilter[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).get(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

}
