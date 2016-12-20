package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to declare that an agent must run on behalf of someone
 * @author Lionel HERVIER
 */
public class SetOnBehalfOf extends BaseDatabaseSetTask {

	/**
	 * Agent
	 */
	private String agent;
	
	/**
	 * On behalf of
	 */
	private String onBehalfOf;
	
	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * @param onBehalfOf the onBehalfOf to set
	 */
	public void setOnBehalfOf(String onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(Database)
	 */
	@Override
	public void execute(Database db) throws NotesException {
		this.log(db.getServer() + "!!" + db.getFilePath() + "/" + this.agent + " will be set to run on behalf of '" + this.onBehalfOf + "'");
		
		NoteCollection coll = null;
		try {
			// Créé la collection
			coll = db.createNoteCollection(false);
			coll.setSelectAgents(true);
			coll.buildCollection();
			String id = coll.getFirstNoteID();
			while( id.length() > 0 ) {
				Document agentDoc = null;
				try {
					agentDoc = db.getDocumentByID(id);
					String title = agentDoc.getItemValueString("$TITLE");
					if( this.agent.equals(title) ) {
						agentDoc.replaceItemValue("$OnBehalfOf", this.onBehalfOf);
						agentDoc.sign();
						agentDoc.save(true, false);
						break;
					}
				} finally {
					Utils.recycleQuietly(agentDoc);
				}
				id = coll.getNextNoteID(id);
			}
		} finally {
			Utils.recycleQuietly(coll);
		}
	}
}
