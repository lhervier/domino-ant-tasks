package fr.asi.designer.anttasks.domino.impl;

import org.apache.tools.ant.BuildException;

import lotus.domino.Document;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.DesignElementTask;

/**
 * This task will scan XPages into a set of databases
 * and will verify that have been compiled.
 * FIXME: Implement a Condition instead of a task.
 * @author Lionel HERVIER
 */
public class CheckXPagesCompiled extends DesignElementTask {

	/**
	 * Constructor
	 * Force selection of XPages
	 */
	public CheckXPagesCompiled() {
		super.setSelect(Type.XPAGES.name() + "," + Type.CUSTOM_CONTROLS.name());
	}
	
	/**
	 * @see fr.asi.designer.anttasks.domino.DesignElementTask#execute(fr.asi.designer.anttasks.domino.DesignElementTask.Type, lotus.domino.Document)
	 */
	@Override
	protected void execute(Type type, Document designElement) throws NotesException {
		if( !designElement.hasItem("$ClassData0") || !designElement.hasItem("$ClassData1") )
			throw new BuildException("XPage '" + designElement.getItemValueString("$TITLE") + "' is not compiled !");
	}

	// ============================================================
	
	/**
	 * @see fr.asi.designer.anttasks.domino.DesignElementTask#setSelect(java.lang.String)
	 */
	@Override
	public void setSelect(String select) {
		throw new BuildException("You are not allowed to set the 'select' parameter on this task");
	}
}
