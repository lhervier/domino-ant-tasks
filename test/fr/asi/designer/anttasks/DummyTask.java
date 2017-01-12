package fr.asi.designer.anttasks;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;

public class DummyTask extends BaseDatabaseSetTask {

	private String form;
	
	@Override
	protected void execute(Database database) throws NotesException {
		Document doc = database.createDocument();
		doc.replaceItemValue("Form", this.form);
		doc.save(true, false);
	}

	/**
	 * @param form the form to set
	 */
	public void setForm(String form) {
		this.form = form;
	}
}
