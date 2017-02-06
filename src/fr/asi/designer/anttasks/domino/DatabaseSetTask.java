package fr.asi.designer.anttasks.domino;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.DatabaseSetElement;

/**
 * Abstract class for databaseSet based tasks
 * @author Lionel HERVIER
 */
public abstract class DatabaseSetTask extends DatabaseSetElement<Void> {

	/**
	 * Execution on each database
	 * @param db the database to run the task on
	 * @throws NotesException
	 */
	protected abstract void execute(Database db) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.DatabaseSetElement#execute(java.util.List)
	 */
	@Override
	protected Void run(List<Database> databases) throws NotesException {
		for( Database db : databases )
			this.execute(db);
		return null;
	}
	
	/**
	 * Execution of the task
	 */
	public void execute() {
		this.run();
	}
}
