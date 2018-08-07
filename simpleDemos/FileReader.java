package simpleDemos;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Чтение файлов со списками
 * @author KonishchevDV
 */
public class FileReader {

	/**
	 * @param file - файл со списком объектов
	 * @param codepage - кодировка файла, например: CP1251
	 * @return - ArrayList с объектами String
	 */
	ArrayList<String> fileToList(File file, String codepage) {

		BufferedReader br = null;
		String s;
		ArrayList<String> list = new ArrayList<String>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), codepage));
			while ((s = br.readLine()) != null) {
				list.add(s);
			}
			br.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}
}
