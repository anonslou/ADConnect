package simpleDemos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.naming.AuthenticationException;

import adProvider.ADConnection;
import adProvider.ADQuery;
import enter.Prop;

public class CreateADUsersFromFile {

	/**
	 * Массово создает пользователей по ФИО, взятым из файла в OU, задает пароль
	 * для смены при первом входе, добавляет пользователей в группы
	 * 
	 * @throws IOException
	 */
	public void userCreater(String fileName, String pass, String OU, String grp1, String grp2) throws IOException {

		Prop prop = new Prop("src/config.property");
		ADConnection adc = new ADConnection(prop);

		adc.useSSL(true);

		adc.setSERVER(prop.getProp("ad.server"));
		adc.setROOT(prop.getProp("ad.domain"));

		adc.setLOGIN(prop.getProp("ad.login"));
		adc.setPASS(prop.getProp("ad.pass"));

		try {
			adc.connect();
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
		ADQuery adq = new ADQuery(adc);

		FileReader fr = new FileReader();
		File file = new File(fileName);

		ArrayList<String> list = fr.fileToList(file, "cp1251");

		for (int i = 0; i < list.size(); i++) {
			String[] fio = list.get(i).split(" ");

			String userDN = adq.createADUser(fio[0], fio[1], fio[2], OU, false);

			if (userDN != null) {
				adq.changeUserPassword(userDN, pass, true);

				adq.addUserToGroup(userDN, adq.findDNbyCN(grp2));
				adq.addUserToGroup(userDN, adq.findDNbyCN(grp2));
			}

			System.out.println(userDN);
		}

		adc.close();
	}

	public static void main(String[] args) throws IOException {
		CreateADUsersFromFile ms = new CreateADUsersFromFile();
		ms.userCreater("C:\\user.txt", "Pass1234", "OU=NEW,OU=Users", "Group USER 1", "Group USER 2");
	}

}
