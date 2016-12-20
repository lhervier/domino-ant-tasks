package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;

/**
 * Ant task to launch a refresh design of a set of databases.
 * 
 * Design refresh is done via a console command that load the "design" task
 * with parameters.
 * @author Lionel HERVIER
 */
public class RefreshDesign extends BaseDatabaseSetTask {

	/**
	 * Dry run ?
	 */
	private boolean dryRun = false;
	
	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		this.log("Refreshing design of '" + db.getServer() + "!!" + db.getFilePath() + "'", Project.MSG_INFO);
		String cmd = "load design -f " + db.getFilePath();
		
		if( this.dryRun )
			return;
		
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(db.getServer());
		task.setCommand(cmd);
		task.setTaskRunningMessage("Designer");
		task.execute(db.getParent());
	}
}
