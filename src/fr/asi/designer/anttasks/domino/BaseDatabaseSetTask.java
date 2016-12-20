package fr.asi.designer.anttasks.domino;

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
public abstract class BaseDatabaseSetTask extends BaseNotesTask {
	
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
		ret.setParentTask(this);
		this.databases.add(ret);
		return ret;
	}
	
	/**
	 * Execution on a given database
	 * @param database the Notes database to run the task on
	 * @throws NotesException
	 */
	protected abstract void execute(Database database) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		// Extract databases file path
		List<Database> dbs = new ArrayList<Database>();
		try {
			if( !Utils.isEmpty(this.database) && !Utils.isEmpty(this.server) )
				dbs.add(this.openDatabase(this.server, this.database));
			for( DatabaseSet s : this.databases )
				dbs.addAll(s.getDatabases());
			
			// Run execution on each database
			for( Database db : dbs )
				this.execute(db);
		} finally {
			for( Database db : dbs )
				Utils.recycleQuietly(db);
		}
	}
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#delegate(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseNotesTask> T delegate(Class<T> cl) {
		T ret = super.delegate(cl);
		if( !(ret instanceof BaseDatabaseSetTask) )
			return ret;
		
		BaseDatabaseSetTask ret2 = (BaseDatabaseSetTask) ret;
		ret2.database = this.database;
		ret2.server = this.server;
		ret2.databases = this.databases;
		
		return (T) ret2;
	}

	/**
	 * @return the server
	 */
	String getServer() {
		return server;
	}
	
	// ==============================================================
	
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
