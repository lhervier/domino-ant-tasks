package fr.asi.designer.anttasks.domino.impl;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.domino.ContextDocField;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to launch an agent using the Notes API NotesAgent.run. This task can contain
 * {@link ContextDocField} if you need to add values to the
 * document context.
 * @author Lionel HERVIER
 */
public class RunAgent extends BaseDatabaseSetTask {

	/**
	 * Agent
	 */
	private String agent;
	
	/**
	 * The fields to add
	 */
	private List<ContextDocField> contextDocFields = new ArrayList<ContextDocField>();
	
	/**
	 * @return an empty context doc field object
	 */
	public ContextDocField createContextDocField() {
		ContextDocField ret = new ContextDocField();
		this.contextDocFields.add(ret);
		return ret;
	}
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		this.log("Running agent '" + this.agent + "' in database '" + db.getServer() + "!!" + db.getFilePath() + "'");
		Document doc = null;
		Agent ag = null;
		try {
			ag = db.getAgent(this.agent);
			if( ag == null )
				throw new BuildException("Unable to find agent '" + this.agent + " in database");
			
			doc = db.createDocument();
			doc.replaceItemValue("Form", "RunAgent");
			for( ContextDocField field : this.contextDocFields )
				doc.replaceItemValue(field.getName(), field.getValue());
			doc.save(true, false);
			
			ag.run(doc.getNoteID());
		} finally {
			Utils.recycleQuietly(ag);
			doc.remove(true);
			Utils.recycleQuietly(doc);
		}
	}
	
	// ===================================================================
	
	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
}
