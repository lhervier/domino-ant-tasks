package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.NotesTask;


/**
 * Starts an http task on a domino server
 * @author Lionel HERVIER
 */
public class HttpStart extends NotesTask {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * En cas d'erreur, on continue
	 */
	private boolean failSafe;
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param failSafe the failSafe to set
	 */
	public void setFailSafe(boolean failSafe) {
		this.failSafe = failSafe;
	}

	public void execute(Session session) {
		this.log("Starting HTTP Task on server '" + this.server + "'");
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(this.server);
		task.setCommand("load http");
		task.setTaskStartedMessage("HTTP Server[ ]*Listen for connect requests on TCP Port:");
		try {
			task.execute();
		} catch(BuildException e) {
			if( this.failSafe )
				return;
			throw e;
		}
	}
}
