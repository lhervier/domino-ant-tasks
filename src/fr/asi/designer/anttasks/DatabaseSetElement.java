package fr.asi.designer.anttasks;

import lotus.domino.Session;

/**
 * Interface used by all Notes elements (tasks and conditions)
 * that rely on a database set
 * @author Lionel HERVIER
 */
public interface DatabaseSetElement {

	/**
	 * Return the current notes id password
	 */
	public String getPassword();
	
	/**
	 * Return the name of the current server
	 */
	public String getServer();
	
	/**
	 * Return the current session
	 */
	public Session getSession();
}
