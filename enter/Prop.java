package enter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Prop {

	private Properties property = null;

	public Prop(String file) throws IOException {

		FileInputStream fis;
		property = new Properties();

		try {
			fis = new FileInputStream(file);
			property.load(fis);

		} catch (IOException e) {
			throw new IOException("ОШИБКА: Файл свойств отсуствует!");
		}
	}

	public String getProp(String name) {
		return name = property.getProperty(name);
	}
}
