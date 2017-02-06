package fr.asi.designer.anttasks.domino;

import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.NotesElement;

public abstract class NotesTask extends NotesElement<Void> {

	/**
	 * Execution
	 * @throws NotesException in cas of trouble...
	 */
	protected abstract void execute(Session session) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.NotesElement#run(lotus.domino.Session)
	 */
	@Override
	protected Void run(Session session) throws NotesException {
		this.execute(session);
		return null;
	}

	/**
	 * Run the ant task
	 */
	public void execute() {
		this.run();
	}

}
