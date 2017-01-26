package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Document;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.BaseDesignElementTask;

/**
 * Task to clear the template that design elements depends on
 * @author Lionel HERVIER
 */
public class ClearInheritTemplateFrom extends BaseDesignElementTask {

	/**
	 * Dry run ?
	 */
	private boolean dryRun = false;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDesignElementTask#execute(fr.asi.designer.anttasks.domino.BaseDesignElementTask.Type, lotus.domino.Document)
	 */
	@Override
	protected void execute(Type type, Document designElement) throws NotesException {
		if( !designElement.hasItem("$Class") )
			return;
		
		String server = designElement.getParentDatabase().getServer();
		String db = designElement.getParentDatabase().getFilePath();
		log(server + "!!" + db + ": " + (this.dryRun ? "Warning" : "Removing :") + " template inheritance for '" + designElement.getItemValueString("$TITLE") + "' (" + designElement.getUniversalID() + " / " + type + ")");
		
		if( this.dryRun )
			return;
		
		designElement.removeItem("$Class");
		designElement.save(true, false);
	}

	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

}
