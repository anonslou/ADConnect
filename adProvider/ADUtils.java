package adProvider;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

public class ADUtils {

	/**
	 * Преобразует время из формата AD в обычный календарь. 
	 * @param adTime - время в формате AD
	 * @return Calendar
	 */
	public static Calendar getTime(Long adTime) {
		long javaTime = adTime - 0x19db1ded53e8000L;
		javaTime /= 10000;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(javaTime);
		return cal;
	}

	public static String getIPbyName(String name) throws UnknownHostException {
		return InetAddress.getByName(name).getHostAddress();
	}

	public static String getTranslit(String s) {
		final Hashtable<String, String> trans = new Hashtable<String, String>(33 + 2);
		trans.put("а", "a");
		trans.put("б", "b");
		trans.put("в", "v");
		trans.put("г", "g");
		trans.put("д", "d");
		trans.put("е", "e");
		trans.put("ё", "e");
		trans.put("ж", "j");
		trans.put("з", "z");
		trans.put("и", "i");
		trans.put("й", "y");
		trans.put("к", "k");
		trans.put("л", "l");
		trans.put("м", "m");
		trans.put("н", "n");
		trans.put("о", "o");
		trans.put("п", "p");
		trans.put("р", "r");
		trans.put("с", "s");
		trans.put("т", "t");
		trans.put("у", "u");
		trans.put("ф", "f");
		trans.put("х", "h");
		trans.put("ц", "ts");
		trans.put("ч", "ch");
		trans.put("ш", "sh");
		trans.put("щ", "shch");
		trans.put("ь", "");
		trans.put("ы", "i");
		trans.put("ъ", "");
		trans.put("э", "e");
		trans.put("ю", "u");
		trans.put("я", "ya");

		trans.put("-", "_");
		trans.put(" ", "_");

		Enumeration<String> i = trans.keys();
		while (i.hasMoreElements()) {
			String letter = i.nextElement();
			s = s.toLowerCase();
			s = s.replaceAll(letter, trans.get(letter));
		}

		return s;
	}

	public static String generateName(String f, String i, String o) {
		if (!"".equals(i) && !"".equals(o) && !"".equals(f)) {
			return f.substring(0, 1).toUpperCase() + f.substring(1, f.length()).toLowerCase()
					+ i.substring(0, 1).toUpperCase() + o.substring(0, 1).toUpperCase();
		} else if (!"".equals(i) && !"".equals(f)) {
			return f.substring(0, 1).toUpperCase() + f.substring(1, f.length()).toLowerCase()
					+ i.substring(0, 1).toUpperCase();
		} else if (!"".equals(f)) {
			return f.substring(0, 1).toUpperCase() + f.substring(1, f.length()).toLowerCase();
		}
		return null;

	}
	
}
