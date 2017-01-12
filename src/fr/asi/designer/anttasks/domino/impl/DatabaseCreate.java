package fr.asi.designer.anttasks.domino.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to create a new database.
 * @author Lionel HERVIER
 */
public class DatabaseCreate extends BaseNotesTask {

	/**
	 * Server where to create the database
	 */
	private String server;
	
	/**
	 * Name of the database to create
	 */
	private String database;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Creating database " + this.server + "!!" + this.database);
		
		File nsf = null;
		try {
			
			// Copy the blank database from the resources to the temp folder
			InputStream in = null;
			OutputStream out = null;
			try {
				nsf = File.createTempFile("blank", ".nsf");
				in = Thread.currentThread().getContextClassLoader().getResourceAsStream("blank.nsf");
				out = new FileOutputStream(nsf);
				
				byte[] buffer = new byte[4 * 1024];
				int read = in.read(buffer);
				while( read != -1 ) {
					out.write(buffer, 0, read);
					read = in.read(buffer);
				}
				
			} catch(IOException e) {
				throw new BuildException(e);
			} finally {
				Utils.closeQuietly(out);
				Utils.closeQuietly(in);
			}
			
			// Use copy task to create the new database from blank
			DatabaseCopy copy = this.delegate(DatabaseCopy.class);
			copy.setSrcServer("");
			copy.setSrcDatabase(nsf.getAbsolutePath());
			copy.setDestServer(this.server);
			copy.setDestDatabase(this.database);
			copy.execute();
		} finally {
			if( nsf != null ) 
				if( !nsf.delete() )
					throw new BuildException("Unable to remove temp file " + nsf.getAbsolutePath());
		}
	}
	
	// =========================================================================
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
}
