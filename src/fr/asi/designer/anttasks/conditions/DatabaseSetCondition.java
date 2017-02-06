package fr.asi.designer.anttasks.conditions;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

import fr.asi.designer.anttasks.DatabaseSetElement;

/**
 * Base Notes condition that rely on a server and a database
 * @author Lionel HERVIER
 */
public abstract class DatabaseSetCondition extends DatabaseSetElement<Boolean> implements Condition {

	/**
	 * Evaluate the condition on the database
	 * @param databases the databases
	 * @return the condition result
	 * @throws NotesException en cas de pb
	 */
	protected abstract boolean eval(List<Database> databases) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.DatabaseSetElement#run(java.util.List)
	 */
	@Override
	protected Boolean run(List<Database> databases) throws NotesException {
		return new Boolean(this.eval(databases));
	}

	/**
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	public boolean eval() throws BuildException {
		return this.run().booleanValue();
	}
}
