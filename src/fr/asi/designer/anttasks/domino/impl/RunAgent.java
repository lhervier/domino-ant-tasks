package fr.asi.designer.anttasks.domino.impl;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

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
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(Session session, String server, String dbPath)
	 */
	@Override
	public void execute(Session session, String server, String database) throws NotesException {
		this.log("Running agent '" + this.agent + "' in database '" + server + "!!" + database + "'");
		Database src = null;
		Document doc = null;
		try {
			src = this.openDatabase(server, database);
			Agent ag = src.getAgent(this.agent);
			if( ag == null )
				throw new BuildException("Unable to find agent '" + RunAgent.this.agent + " in database");
			
			doc = src.createDocument();
			doc.replaceItemValue("Form", "RunAgent");
			for( ContextDocField field : this.contextDocFields )
				doc.replaceItemValue(field.getName(), field.getValue());
			doc.save(true, false);
			
			ag.run(doc.getNoteID());
		} finally {
			doc.remove(true);
			Utils.recycleQuietly(doc);
			Utils.recycleQuietly(src);
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
