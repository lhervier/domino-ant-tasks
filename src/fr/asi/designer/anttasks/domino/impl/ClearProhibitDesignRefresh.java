package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Document;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.DesignElementTask;

/**
 * Task to remove the "prohibit design refresh" flag
 * on design elements.
 * @author Lionel HERVIER
 */
public class ClearProhibitDesignRefresh extends DesignElementTask {

	/**
	 * Dry run ?
	 */
	private boolean dryRun = false;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.DesignElementTask#execute(fr.asi.designer.anttasks.domino.DesignElementTask.Type, lotus.domino.Document)
	 */
	@Override
	protected void execute(Type type, Document designElement) throws NotesException {
		String flags = designElement.getItemValueString("$Flags");
		int pos = flags.indexOf("P");
		if( pos == -1 )
			return;
		
		String server = designElement.getParentDatabase().getServer();
		String db = designElement.getParentDatabase().getFilePath();
		log(server + "!!" + db + ": " + (this.dryRun ? "Warning" : "Removing :") + " 'prohibit design refresh' flag for '" + designElement.getItemValueString("$TITLE") + "' (" + designElement.getUniversalID() + " / " + type + ")");
		
		if( this.dryRun )
			return;
		
		flags = flags.substring(0, pos) + flags.substring(pos + 1);
		designElement.replaceItemValue("$Flags", flags);
		designElement.save(true, false);
	}
	

	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}
}
