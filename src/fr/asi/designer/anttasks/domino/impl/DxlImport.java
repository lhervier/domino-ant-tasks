package fr.asi.designer.anttasks.domino.impl;

import java.io.File;

import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Import a dxl file into a database
 * @author Lionel HERVIER
 */
public class DxlImport extends BaseDatabaseSetTask {

	/**
	 * Success log message
	 */
	private static final String IMPORT_SUCCESS = "<?xml version='1.0'?>\n" +
				"<DXLImporterLog>\n" +
				"</DXLImporterLog>";
	
	/**
	 * The file to get the DXL from
	 */
	private String fromFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(Database)
	 */
	@Override
	protected void execute(Database db) throws NotesException {
		this.log("Importing " + this.fromFile + " to " + db.getServer() + "!!" + db.getFilePath());
		
		Stream stream = null;
		DxlImporter importer = null;
		try {
			File f = new File(this.fromFile);
			if( !f.isAbsolute() )
				f = new File(this.getProject().getProperty("basedir") + "/" + this.fromFile);
			stream = db.getParent().createStream();
			if ( !stream.open(f.getAbsolutePath()) || (stream.getBytes() == 0) )
				throw new BuildException("Unable to open file " + f.getAbsolutePath());
			
			importer = db.getParent().createDxlImporter();
			importer.setReplaceDbProperties(false);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(stream, db);

			String logs = importer.getLog();
			if( !logs.equals(IMPORT_SUCCESS) )
				throw new BuildException(logs);
		} finally {
			Utils.recycleQuietly(importer);
			Utils.recycleQuietly(db);
			Utils.closeQuietly(stream);
		}
	}
	
	// =========================================================================

	/**
	 * @param fromFile the fromFile to set
	 */
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}

}
