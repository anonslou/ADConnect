package simpleDemos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.AuthenticationException;

import adProvider.ADConnection;
import adProvider.ADQuery;
import enter.Prop;

public class SetMail {

	/**
	 * Устанавливает mail в дефолтный всем у кого данное поле пусто
	 * @throws IOException
	 */
	public void setMail() throws IOException {

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

		String filter = "(&(objectCategory=user)(!(mail=*)))";
		ArrayList<String> list = adq.getListDN(filter);

		Iterator<String> i = list.iterator();
		while (i.hasNext()) {
			String dn = i.next();
			System.out.println(dn);

			Hashtable<String, Object> attrs;
			attrs = adq.getAttributeByNames(dn, "userPrincipalName");

			String mail = (String) attrs.get("userPrincipalName");

			if (mail != null) {
				attrs.clear();

				attrs.put("mail", mail);
				adq.setAttributes(dn, attrs);

			}
		}
		adc.close();
	}

	public static void main(String[] args) throws IOException {
		SetMail ms = new SetMail();
		ms.setMail();
	}

}
