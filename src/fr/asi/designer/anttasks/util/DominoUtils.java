package fr.asi.designer.anttasks.util;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

/**
 * Static methods for Domino
 * @author Lionel HERVIER
 */
public class DominoUtils {

	/**
	 * Returns an opened database object using the current session
	 * @param server the server name
	 * @param database the database name
	 * @return the database
	 * @throws BuildException if the database cannot be opened
	 * @throws NotesException in case of trouble...
	 */
	public static Database openDatabase(Session session, String server, String database) throws BuildException, NotesException {
		Database ret = session.getDatabase(
				server, 
				database, 
				false
		);
		if( ret == null )
			throw new BuildException("Database '" + server + "!!" + database + "' doest not exists");
		if( !ret.isOpen() )
			if( !ret.open() )
				throw new BuildException("Unable to open database '" + server + "!!" + database + "'");
		return ret;
	}
}
