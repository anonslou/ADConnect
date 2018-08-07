package adProvider;

import javax.naming.AuthenticationException;
import javax.naming.directory.DirContext;

public interface AuthInterface {

	public String getLOGIN();

	public void setPASS(String s);

	public void setLOGIN(String s);

	public DirContext connect() throws AuthenticationException;
}
