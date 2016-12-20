package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to clear the content of an update site database
 * @author Lionel HERVIER
 */
public class ClearDb extends BaseDatabaseSetTask {

	/**
	 * The formula
	 */
	private String formula;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		if( this.formula == null )
			this.log("Clearing all content in database '" + db.getServer() + "!!" + db.getFilePath() + "'");
		else
			this.log("Clearing '" + this.formula + "' in database '" + db.getServer() + "!!" + db.getFilePath() + "'");
		DocumentCollection coll = null;
		try {
			if( Utils.isEmpty(this.formula) )
				coll = db.getAllDocuments();
			else
				coll = db.search(this.formula);
			coll.removeAll(false);
		} finally {
			Utils.recycleQuietly(coll);
		}
	}
	
	// ===============================================================
	
	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
}
