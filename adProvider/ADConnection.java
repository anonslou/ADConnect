package adProvider;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import enter.Prop;

public class ADConnection implements AuthInterface {
	private String PASS = null;
	private String LOGIN = null;
	private String SERVER = null;
	private String PORT = "";
	private String ROOT = null;
	private String DC = "";

	private boolean ssl = false;

	public void useSSL(boolean ssl) {
		this.ssl = ssl;
		if ("".equals(PORT) || PORT == null) {
			setPORT(null); // PORT = "636"
		}
	}

	public String getSERVER() {
		return SERVER;
	}

	public void setSERVER(String serverName) {
		if (serverName == null || serverName.equals("")) {
			SERVER = "127.0.0.1";
		}
		SERVER = serverName;
	}

	public String getPORT() {
		return PORT;
	}

	public String getDC() {
		return DC;
	}

	public void setPORT(String port) {
		if (port == null || port.equals("")) {
			if (ssl) {
				port = "636";
			} else {
				port = "389";
			}
		}
		PORT = port;
	}

	public String getROOT() {
		return ROOT;
	}

	public void setROOT(String root) {
		String[] dcs = prop.getProp("ad.domain").split("\\.");
		DC = "";
		for (int i = 0; i < dcs.length - 1; i++) {
			DC += "DC=" + dcs[i] + ",";
		}
		DC += "DC=" + dcs[dcs.length - 1];

		ROOT = root;

		if (!"".equalsIgnoreCase(LOGIN)) {
			LOGIN = LOGIN + "@" + ROOT;
		}

	}

	private Prop prop = null;
	public ADConnection(Prop prop) {
		this.prop = prop;
	}

	public Prop getProp() {
		return prop;
	}
	
	public void close() {
		try {
			ctx.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	LdapContext ctx = null;
	private static String INITCTX = "com.sun.jndi.ldap.LdapCtxFactory";

	@Override
	public DirContext connect() throws AuthenticationException {

		String serv = "";
		Hashtable<String, String> env = new Hashtable<String, String>();
		if (ssl) {
			serv = "ldaps://";
			env.put(Context.SECURITY_PROTOCOL, "ssl");
/*
		Пример установки публичного доменного сертификата в хранилище сертификатов java.
		Без этого ssl соединение не заработает!
		C:\Program Files\Java\jdk1.6.0_24\bin>keytool.exe -import -keystore "C:\Program Files\Java\jre6\lib\security\cacerts" -file c:\client.crt
		Пароль на хранилище по умолчанию: changeit		
*/
//			Путь до хранилища и пароль на него нужно задать в системных проперти, чтобы программа могла работать с хранилищем
			System.setProperty("java.net.ssl.trustStore", prop.getProp("java.net.ssl.trustStore"));
			System.setProperty("java.net.ssl.trustStorePassword", prop.getProp("java.net.ssl.trustStorePassword"));
		} else {
			serv = "ldap://";
		}
		serv += SERVER + ":" + PORT + "/" + DC;
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);
		env.put(Context.PROVIDER_URL, serv);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, LOGIN);
		env.put(Context.SECURITY_CREDENTIALS, PASS);
		env.put(Context.REFERRAL, "ignore");

		try {
			InitialDirContext initialContext = new InitialLdapContext(env, null);
			ctx = (LdapContext) initialContext;
		} catch (AuthenticationException e1) {
			throw new AuthenticationException("Неверный логин или пароль");
		} catch (NamingException e2) {
			e2.printStackTrace();
			return null;
		}
		return ctx;
	}

	public LdapContext getCtx() {
		return ctx;
	}

	@Override
	public String getLOGIN() {
		return LOGIN;
	}

	@Override
	public void setPASS(String s) {
		PASS = s;
	}

	@Override
	public void setLOGIN(String s) {
		if (s.indexOf("@") < 0) {
			LOGIN = s + "@" + getROOT();
		} else {
			LOGIN = s;
		}
	}

	/**
	 * Создает копию соединения, чтобы легко можно было подключиться
	 * к другому серверу с тем же логином/паролем внутри программы.
	 * @return - объект ADConnection
	 */
	public ADConnection copy() {
		ADConnection adc = new ADConnection(prop);
		// порядок вызова методов важен, т.к.
		// LOGIN и ROOT завязаны друг на друга
		adc.setROOT(ROOT);
		adc.setPORT(PORT);
		adc.setSERVER(SERVER);
		adc.setPASS(PASS);
		adc.setLOGIN(LOGIN);
		return adc;
	}
}
