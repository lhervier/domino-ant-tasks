package fr.asi.designer.anttasks;

import static fr.asi.designer.anttasks.util.DominoUtils.openDatabase;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Base task for tasks that work with databaseSets
 * @author Lionel HERVIER
 */
public abstract class BaseDatabaseSetElement<T> extends BaseNotesElement<T> {
	
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
	private List<DatabaseSet> databaseSet = new ArrayList<DatabaseSet>();
	
	/**
	 * Create an empty databaseSet
	 */
	public DatabaseSet createDatabaseSet() {
		DatabaseSet ret = new DatabaseSet();
		ret.setParentDatabaseSetElement(this);
		this.databaseSet.add(ret);
		return ret;
	}
	
	/**
	 * Clear the database set
	 */
	public void clearDatabaseSet() {
		this.databaseSet.clear();
	}
	
	/**
	 * Returns the databaseSet.
	 * This method is need so that our object is viewed by the Introspector class
	 * as a bean with a "databaseSet" property.
	 * @return the databaseSet
	 */
	public List<DatabaseSet> getDatabaseSet() {
		return this.databaseSet;
	}
	
	/**
	 * Execution on a given database
	 * @param databases the Notes databases to run the task on
	 * @throws NotesException
	 */
	protected abstract T execute(List<Database> databases) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.BaseNotesElement#execute(lotus.domino.Session)
	 */
	@Override
	public T run(Session session) throws NotesException {
		// Extract databases file path
		List<Database> dbs = new ArrayList<Database>();
		try {
			if( !Utils.isEmpty(this.database) && !Utils.isEmpty(this.server) )
				dbs.add(openDatabase(this.getSession(), this.server, this.database));
			for( DatabaseSet s : this.databaseSet )
				dbs.addAll(s.getDatabases());
			
			// Run execution on each database
			return this.execute(dbs);
		} finally {
			for( Database db : dbs )
				Utils.recycleQuietly(db);
		}
	}
	
	// ==============================================================
	
	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}
	
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
