package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.DatabaseSetTask;

/**
 * Task to send a "tell amgr run" on the server console
 * @author Lionel HERVIER
 */
public class TellAmgrRun extends DatabaseSetTask {

	/**
	 * The name of the agent
	 */
	private String agent;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.DatabaseSetTask#execute(lotus.domino.Database)
	 */
	@Override
	protected void execute(Database db) throws NotesException {
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(db.getServer());
		task.setCommand("tell amgr run \"" + db.getFilePath() + "\" '" + this.agent + "'");
		task.execute();
	}
	
	// ==================================================================================

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}
}
