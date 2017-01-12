package fr.asi.designer.anttasks;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.impl.DatabaseCreate;
import fr.asi.designer.anttasks.domino.impl.DatabaseDelete;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Test the sets
 * @author Lionel HERVIER
 */
public class TestSets extends BaseAntTest {

	/**
	 * Test the "databaseSet" set
	 * @throws Exception
	 */
	public void testDatabaseSet() throws Exception {
		this.setProperty("template", "tests/databaseSet.ntf");
		this.setProperty("db1", "tests/databaseSet1.nsf");
		this.setProperty("db2", "tests/databaseSet2.nsf");
		this.setProperty("db3", "tests/databaseSet3.nsf");
		this.setProperty("db4", "tests/databaseSet4.nsf");
		this.setProperty("masterTemplateName", "testDatabaseSet");
		
		// Create the template
		DatabaseCreate createTask = new DatabaseCreate();
		createTask.setPassword(this.getProperty("password"));
		createTask.setServer(this.getProperty("server"));
		createTask.setDatabase(this.getProperty("template"));
		createTask.execute();
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(
						session, 
						TestSets.this.getProperty("server"), 
						TestSets.this.getProperty("template")
				);
				db.setTitle(db.getTitle() + "\n#1" + TestSets.this.getProperty("masterTemplateName"));
				return null;
			}
		});
		
		// Create two more databases that rely on the template
		for( int i=0; i<2; i++ ) {
			DatabaseDelete deleteTask = new DatabaseDelete();
			deleteTask.setPassword(this.getProperty("password"));
			deleteTask.setServer(this.getProperty("server"));
			deleteTask.setDatabase(this.getProperty("db" + (i + 1)));
			deleteTask.execute();
		}
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database template = session.getDatabase(
						TestSets.this.getProperty("server"), 
						TestSets.this.getProperty("template"), 
						true
				);
				for( int i=0; i<2; i++ ) {
					template.createFromTemplate(
							TestSets.this.getProperty("server"), 
							TestSets.this.getProperty("db" + (i + 1)), 
							true
					);
				}
				return null;
			}
		});
		
		// Create two more database
		createTask = new DatabaseCreate();
		createTask.setPassword(this.getProperty("password"));
		createTask.setServer(this.getProperty("server"));
		createTask.setDatabase(this.getProperty("db3"));
		createTask.execute();
		
		createTask = new DatabaseCreate();
		createTask.setPassword(this.getProperty("password"));
		createTask.setServer(this.getProperty("server"));
		createTask.setDatabase(this.getProperty("db4"));
		createTask.execute();
		
		// Create a param doc in the first database only
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db1 = session.getDatabase(
						TestSets.this.getProperty("server"), 
						TestSets.this.getProperty("db1"), 
						true
				);
				Document doc = db1.createDocument();
				doc.replaceItemValue("Form", "Param");
				doc.save(true, false);
				return null;
			}
		});
		
		// DatabaseSet inside a task tag
		List<String> result = runAntTask("sets/TestDatabaseSet.xml", "testTask");
		assertEquals(4, result.size());
		assertEquals("Doc created in db1", result.get(0));
		assertEquals("Doc NOT created in db2", result.get(1));
		assertEquals("Doc created in db3", result.get(2));
		assertEquals("Doc created in db4", result.get(3));
		
		// DatabaseSet inside a condition tag
		result = runAntTask("sets/TestDatabaseSet.xml", "testCondition");
		assertEquals(3, result.size());
		assertEquals("Doc created in db1", result.get(0));
		assertEquals("Param doc DOES NOT exists in all databases that rely on " + this.getProperty("masterTemplateName"), result.get(1));
		assertEquals("Param doc exists in database " + this.getProperty("db1"), result.get(2));
	}
}
