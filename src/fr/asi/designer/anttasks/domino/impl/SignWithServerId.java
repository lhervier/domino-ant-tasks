package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.DatabaseSetTask;
import fr.asi.designer.anttasks.domino.subtasks.CheckAdminRequestStatus;
import fr.asi.designer.anttasks.domino.subtasks.SignDatabaseWithServerId;

/**
 * Task to sign a given database with the server id
 * @author Lionel HERVIER
 */
public class SignWithServerId extends DatabaseSetTask {

	/**
	 * @see fr.asi.designer.anttasks.domino.DatabaseSetTask#execute(lotus.domino.Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		this.log("Signing database '" + db.getServer() + "!!" + db.getFilePath() + "' with the server ID");
		
		// Create the adminp request
		SignDatabaseWithServerId signTask = this.delegate(SignDatabaseWithServerId.class);
		signTask.setServer(db.getServer());
		signTask.setDatabase(db.getFilePath());
		signTask.execute();
		String noteId = signTask.getNoteId();
		
		// Force adminp to run
		SendConsole sendConsole = this.delegate(SendConsole.class);
		sendConsole.setServer(db.getServer());
		sendConsole.setCommand("tell adminp process all");
		sendConsole.execute();
		
		// Wait for the request to finish
		int maxTimeout = 200;
		int tick = 0;
		String status = null;
		while( !"Processed".equals(status) && tick < maxTimeout ) {
			if( tick % 5 == 0 )
				this.log("Waiting for adminp requests to finish");
			tick++;
			
			CheckAdminRequestStatus task = this.delegate(CheckAdminRequestStatus.class);
			task.setServer(db.getServer());
			task.setNoteId(noteId);
			task.execute();
			
			status = task.getStatus();
			
			if( "Error".equals(status) )
				throw new BuildException("The adminp request do not execute correctly. Check content of admin4.nsf database !");
			
			if( !"Processed".equals(status) )
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new BuildException(e);
				}
		}
	}
}
