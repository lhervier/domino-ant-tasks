package fr.asi.designer.anttasks.conditions;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

import fr.asi.designer.anttasks.NotesElement;

/**
 * Base condition using Notes APIs
 * @author Lionel HERVIER
 */
public abstract class NotesCondition extends NotesElement<Boolean> implements Condition {

	/**
	 * Execution
	 * @throws NotesException in case of trouble...
	 */
	protected abstract boolean eval(Session session) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.NotesElement#run(lotus.domino.Session)
	 */
	@Override
	protected Boolean run(Session session) throws NotesException {
		return this.eval(session);
	}

	/**
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	public boolean eval() throws BuildException {
		return this.run().booleanValue();
	}
}
