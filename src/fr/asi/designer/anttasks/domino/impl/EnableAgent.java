package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.DatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to enable an agent on a set of databases
 * @author Lionel HERVIER
 */
public class EnableAgent extends DatabaseSetTask {
	
	/**
	 * Agent to enable
	 */
	private String agent;
	
	/**
	 * The server to enable the agent on
	 */
	private String serverToRun;
	
	/**
	 * @param serverToRun the serverToRun to set
	 */
	public void setServerToRun(String serverToRun) {
		this.serverToRun = serverToRun;
	}

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.DatabaseSetTask#execute(Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		this.log("Enabling agent '" + this.agent + "' in database '" + db.getServer() + "!!" + db.getFilePath() + "'");
		Agent ag = null;
		try {
			ag = db.getAgent(this.agent);
			if( ag == null )
				throw new BuildException("Agent '" + EnableAgent.this.agent + "' not found in database '" + db.getServer() + "!!" + db.getFilePath() + "'");

			ag.setEnabled(true);
			ag.setServerName(this.serverToRun);
			ag.save();
		} finally {
			Utils.recycleQuietly(ag);
		}
	}
}
