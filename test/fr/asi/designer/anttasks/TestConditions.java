package fr.asi.designer.anttasks;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.impl.DatabaseCreate;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Test the conditions
 * @author Lionel HERVIER
 */
public class TestConditions extends BaseAntTest {

	/**
	 * Test the "documentExists" condition
	 * @throws Exception
	 */
	public void testDocumentExists() throws Exception {
		this.setProperty("db", "tests/documentExists.nsf");
		
		// Create a new one (overwriting if needed)
		DatabaseCreate createTask = new DatabaseCreate();
		createTask.setPassword(this.getProperty("password"));
		createTask.setServer(this.getProperty("server"));
		createTask.setDatabase(this.getProperty("db"));
		createTask.execute();
		
		// Create a document in the database
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(
						session, 
						TestConditions.this.getProperty("server"), 
						TestConditions.this.getProperty("db")
				);
				Document doc = db.createDocument();
				doc.replaceItemValue("Form", "test");
				doc.save(true, false);
				return null;
			}
		});

		// Run the sample build.xml
		List<String> out = runAntTask("conditions/TestDocumentExists.xml");
		assertEquals(3, out.size());
		assertEquals("Exists / No databaseSet", out.get(0));
		assertEquals("Does not exists / No databaseSet", out.get(1));
		assertEquals("Exists / DatabaseSet", out.get(2));
	}
	
	/**
	 * Test the "fieldsExists" condition
	 * @throws Exception
	 */
	public void testFieldsExists() throws Exception {
		this.setProperty("db", "tests/fieldsExists.nsf");
		
		// Create a new one (overwriting if needed)
		DatabaseCreate createTask = new DatabaseCreate();
		createTask.setPassword(this.getProperty("password"));
		createTask.setServer(this.getProperty("server"));
		createTask.setDatabase(this.getProperty("db"));
		createTask.execute();
		
		// Create documents in the database
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(
						session, 
						TestConditions.this.getProperty("server"), 
						TestConditions.this.getProperty("db")
				);
				
				Document doc = db.createDocument();
				doc.replaceItemValue("Form", "Both");
				doc.replaceItemValue("Field1", "value");
				doc.replaceItemValue("Field2", "value");
				doc.save(true, false);
				
				doc = db.createDocument();
				doc.replaceItemValue("Form", "SecondEmpty");
				doc.replaceItemValue("Field1", "value");
				doc.replaceItemValue("Field2", "");
				doc.save(true, false);
				
				doc = db.createDocument();
				doc.replaceItemValue("Form", "SecondMissing");
				doc.replaceItemValue("Field1", "value");
				doc.save(true, false);
				
				return null;
			}
		});

		// Run the sample build.xml
		List<String> out = runAntTask("conditions/TestFieldsExists.xml");
		assertEquals(4, out.size());
		assertEquals("Exists both / No databaseSet", out.get(0));
		assertEquals("Second empty / No databaseSet", out.get(1));
		assertEquals("Second missing / No databaseSet", out.get(2));
		assertEquals("Exists both / DatabaseSet", out.get(3));
	}
	
}
