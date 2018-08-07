package adProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

public class ADQuery {
	private LdapContext ctx = null;
	private ADConnection adc = null;

	public ADQuery(ADConnection adc) {
		this.adc = adc;
		this.ctx = adc.getCtx();
	}

	private NamingEnumeration<?> ADsearch(String filter, String attribute) {

		SearchControls sc = new SearchControls();
		attribute = attribute.replaceAll(" ", "");
		String[] attributeFilter = attribute.split(",");
		sc.setReturningAttributes(attributeFilter);
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<?> results = null;

		try {
			results = ctx.search("", filter, sc);
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
		return results;

	}

	public ArrayList<String> getListDN(String filter) {
		ArrayList<String> list = new ArrayList<String>();
		NamingEnumeration<?> results = ADsearch(filter, "distinguishedName");
		try {
			while (results.hasMoreElements()) {
				SearchResult sr = (SearchResult) results.next();
				Attributes attrs = sr.getAttributes();
				Attribute distinguishedName = attrs.get("distinguishedName");
				String retval = (String) distinguishedName.get();
				retval = retval.toUpperCase().replaceAll("," + adc.getDC().toUpperCase(), "");
				list.add(retval);
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 
	 * @param cn
	 *            - Каноникал нейм искомого объекта
	 * @return - строка с distinguishedName, без DC (DC можно получить методом getDC() объекта ADConnection) Внимание,
	 *         если объектов больше одного, т.е. cn не уникален в скопе objectCategory=*, например, когда есть
	 *         пользователь и группа с одинаковыми cn, метод возвратит первый найденый объект! Для таких объектов,
	 *         следует использовать метод getListDN, где возвращается список DN для произвольного фильтра
	 */
	public String findDNbyCN(String cn) {
		SearchControls sc = new SearchControls();
		String[] attributeFilter = { "cn", "distinguishedName" };
		sc.setReturningAttributes(attributeFilter);
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<?> results = null;
		String retval = null;
		try {
			String filter = "(&(objectCategory=*)(cn=" + cn + "))";
			results = ctx.search("", filter, sc);
			SearchResult sr = (SearchResult) results.next();
			Attributes attrs = sr.getAttributes();
			Attribute distinguishedName = attrs.get("distinguishedName");
			retval = (String) distinguishedName.get();
			retval = retval.toUpperCase().replaceAll("," + adc.getDC().toUpperCase(), "");

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
		return retval;

	}

	/**
	 * 
	 * @param dn
	 *            - путь до объекта, без dc=,dc=
	 * @param attribute
	 *            - null, чтобы вернуть все доступные атрибуты
	 * @return - все найденные атрибуты, если атрибут не найден, записи о нем не будет
	 */
	public Hashtable<String, Object> getAttributeByNames(String dn, String attribute) {
		String[] attributeFilter = null;
		if (attribute != null) {
			attribute = attribute.replaceAll(" ", "");
			attributeFilter = (String[]) attribute.split(",");
		}
		Attributes answer = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();

		try {
			if (attribute != null) {
				answer = ctx.getAttributes(dn, attributeFilter);
			} else {
				answer = ctx.getAttributes(dn);
			}
			// TODO переписать с использованием метода ADsearch
			for (NamingEnumeration<?> ae = answer.getAll(); ae.hasMore();) {
				Attribute attr = (Attribute) ae.next();
				result.put(attr.getID(), attr.get());
			}

		} catch (NamingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * не тестировалось!
	 */
	public void setAttributes(String dn, Hashtable<String, Object> hash) {

		ModificationItem[] mods = new ModificationItem[hash.size()];

		Enumeration<String> keys = hash.keys();
		int i = 0;
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			Attribute mod = new BasicAttribute(key, hash.get(key));
			mods[i++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod);
		}

		try {
			ctx.modifyAttributes(dn, mods);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private static int AD_ACCOUNTDISABLE = 0x0002;
	private static int AD_PASSWD_NOTREQD = 0x0020;
	@SuppressWarnings("unused")
	private static int AD_PASSWD_CANT_CHANGE = 0x0040;
	private static int AD_NORMAL_ACCOUNT = 0x0200;
	@SuppressWarnings("unused")
	private static int AD_DONT_EXPIRE_PASSWORD = 0x1000;
	private static int AD_PASSWORD_EXPIRED = 0x800000;

	/**
	 * 
	 * @param f
	 *            - Фамилия
	 * @param i
	 *            - Имя
	 * @param o
	 *            - Отчество
	 * @param path
	 *            - OU, куда положить пользователя (без DC)
	 * @param disable
	 *            - true, чтобы заблокировать пользователя после создания.
	 * @return - DN пользователя.
	 * 
	 *         ВНИМАНИЕ: пользователь создается без пароля, не смотря на политику безопасности!
	 */
	public String createADUser(String f, String i, String o, String path, boolean disable) {

		String name = ADUtils.getTranslit(ADUtils.generateName(f, i, o));

		// TODO Сделать нормальные методы для работы с именами!
		name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
		String lastletters = name.substring(name.length() - 2, name.length()).toUpperCase();
		name = name.substring(0, name.length() - 2) + lastletters;

		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", "user");
		attrs.put("samAccountName", name);
		attrs.put("cn", name);

		attrs.put("givenName", i);
		attrs.put("sn", f);
		attrs.put("displayName", f + " " + i + " " + o);
		attrs.put("userPrincipalName", name + "@" + adc.getROOT());
		attrs.put("mail", name + "@" + adc.getROOT());

		int flags = AD_NORMAL_ACCOUNT + AD_PASSWD_NOTREQD + AD_PASSWORD_EXPIRED;
		if (disable) {
			flags += AD_ACCOUNTDISABLE;
		}
		attrs.put("userAccountControl", Integer.toString(flags));

		String user_DN = "CN=" + name + "," + path;
		try {
			ctx.createSubcontext(user_DN, attrs);
		} catch (NameAlreadyBoundException e1) {
			// TODO пользователь уже существует
			System.out.println("пользователь существует");
			return null;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
		return user_DN;

	}

	/**
	 * 
	 * @param userDN
	 *            - DN пользователя, можно найти методом findDNbyCN или получить из метода createADUser
	 * @param pass
	 *            - новый пароль пользователя
	 * @param mustChange
	 *            - true, чтобы установить галку: "Сменить пароль при первом входе в систему"
	 * 
	 *            ВНИМАНИЕ: Пароль должен соответствовать требованиям безопасности.
	 */
	public void changeUserPassword(String userDN, String pass, boolean mustChange) {
		try {
			ModificationItem[] mods = null;
			String newQuotedPass = "\"" + pass + "\"";
			byte[] newUnicodePass = newQuotedPass.getBytes("UTF-16LE");

			if (mustChange) {
				mods = new ModificationItem[2];
			} else {
				mods = new ModificationItem[1];
			}
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd",
					newUnicodePass));
			if (mustChange) {
				mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("pwdLastSet",
						Integer.toString(0)));
			}
			ctx.modifyAttributes(userDN, mods);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addUserToGroup(String userDN, String groupDN) {
		userDN += "," + adc.getDC();

		try {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", userDN));
			ctx.modifyAttributes(groupDN, mods);
		} catch (NameAlreadyBoundException e1) {
			// TODO пользователь уже в группе
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
