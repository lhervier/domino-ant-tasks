package fr.asi.designer.anttasks.conditions;

import static fr.asi.designer.anttasks.util.DominoUtils.openDatabase;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.DatabaseSet;
import fr.asi.designer.anttasks.DatabaseSetElement;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Base Notes condition that rely on a server and a database
 * @author Lionel HERVIER
 */
public abstract class BaseDatabaseSetCondition extends BaseNotesCondition implements DatabaseSetElement {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;

	/**
	 * The database set
	 */
	private List<DatabaseSet> databases = new ArrayList<DatabaseSet>();
	
	/**
	 * Create an empty databaseSet
	 */
	public DatabaseSet createDatabaseSet() {
		DatabaseSet ret = new DatabaseSet();
		ret.setParentDatabaseSetElement(this);
		this.databases.add(ret);
		return ret;
	}
	
	/**
	 * Clear the database set
	 */
	public void clearDatabaseSet() {
		this.databases.clear();
	}
	
	/**
	 * Evaluate the condition on the database
	 * @param databases the databases
	 * @return the condition result
	 * @throws NotesException en cas de pb
	 */
	protected abstract boolean eval(List<Database> databases) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.conditions.BaseNotesCondition#eval(lotus.domino.Session)
	 */
	@Override
	protected boolean eval(Session session) throws NotesException {
		// Extract databases file path
		List<Database> dbs = new ArrayList<Database>();
		try {
			if( !Utils.isEmpty(this.database) && !Utils.isEmpty(this.server) )
				dbs.add(openDatabase(this.getSession(), this.server, this.database));
			for( DatabaseSet s : this.databases )
				dbs.addAll(s.getDatabases());
			
			// Run execution on each database
			return this.eval(dbs);
		} finally {
			for( Database db : dbs )
				Utils.recycleQuietly(db);
		}
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the database
	 */
	protected String getDatabase() {
		return database;
	}

	// =======================================================================
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
}
