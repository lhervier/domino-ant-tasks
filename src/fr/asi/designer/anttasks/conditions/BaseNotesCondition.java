package fr.asi.designer.anttasks.conditions;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

import fr.asi.designer.anttasks.util.ObjectHolder;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Base condition using Notes APIs
 * @author Lionel HERVIER
 */
public abstract class BaseNotesCondition implements Condition {

	/**
	 * Password of the local id file
	 */
	private String password;
	
	/**
	 * The notes session
	 */
	private Session session;
	
	/**
	 * Execution
	 * @throws NotesException in cas of trouble...
	 */
	protected abstract boolean eval(Session session) throws NotesException;
	
	/**
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	public boolean eval() throws BuildException {
		final ObjectHolder<Throwable> exHolder = new ObjectHolder<Throwable>();
		final ObjectHolder<Boolean> resultHolder = new ObjectHolder<Boolean>();
		Thread t = new NotesThread() {
			public void runNotes() {
				try {
					BaseNotesCondition.this.session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) BaseNotesCondition.this.password
					);
					resultHolder.value = BaseNotesCondition.this.eval(BaseNotesCondition.this.session);
				} catch(Throwable e) {
					exHolder.value = e;
				} finally {
					Utils.recycleQuietly(BaseNotesCondition.this.session);
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		if( exHolder.value != null ) {
			exHolder.value.printStackTrace(System.err);
			throw new BuildException(exHolder.value);
		}
		return resultHolder.value;
	}
	
	// ========================= GETTERS AND SETTERS ======================================
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}
}
