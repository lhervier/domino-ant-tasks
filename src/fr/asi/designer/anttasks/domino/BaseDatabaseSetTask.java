package fr.asi.designer.anttasks.domino;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.BaseDatabaseSetElement;

/**
 * Abstract class for databaseSet based tasks
 * @author Lionel HERVIER
 */
public abstract class BaseDatabaseSetTask extends BaseDatabaseSetElement<Void> {

	/**
	 * Execution on each database
	 * @param db the database to run the task on
	 * @throws NotesException
	 */
	protected abstract void execute(Database db) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.BaseDatabaseSetElement#execute(java.util.List)
	 */
	@Override
	protected Void execute(List<Database> databases) throws NotesException {
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
