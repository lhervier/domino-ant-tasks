package fr.asi.designer.anttasks.util;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
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
	
	/**
	 * Interface to run Notes task
	 * @author Lionel HERVIER
	 * @param <T> type of the return value
	 */
	public static interface NotesRunnable<T> {

		/**
		 * Run the code in the Notes session
		 * @param session the notes session
		 * @return the value
		 * @throws NotesException
		 */
		public T run(Session session) throws NotesException;
	}

	/**
	 * Run atask in a Notes session
	 * @param <T> the return type
	 * @param password the password of the local id file
	 * @param r the task to run
	 * @return the value
	 * @throws BuildException
	 */
	public static <T> T runInSession(final String password, final NotesRunnable<T> r) throws BuildException {
		final ObjectHolder<Throwable> exHolder = new ObjectHolder<Throwable>();
		final ObjectHolder<T> resultHolder = new ObjectHolder<T>();
		Thread t = new NotesThread() {
			public void runNotes() {
				Session session = null;
				try {
					session = NotesFactory.createSession((String) null, (String) null, password);
					resultHolder.value = r.run(session);
				} catch (Throwable e) {
					exHolder.value = e;
				} finally {
					Utils.recycleQuietly(session);
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		if (exHolder.value != null) {
			exHolder.value.printStackTrace(System.err);
			throw new BuildException(exHolder.value);
		}
		return resultHolder.value;
	}
}
