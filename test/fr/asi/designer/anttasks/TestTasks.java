package fr.asi.designer.anttasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import junit.framework.Assert;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.impl.DatabaseCreate;
import fr.asi.designer.anttasks.util.DominoUtils;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Test for the ant tasks
 * @author Lionel HERVIER
 */
public class TestTasks extends BaseAntTest {

	/**
	 * Content of the sample rich text
	 */
	public String richTextContent;
	
	/**
	 * @see fr.asi.designer.anttasks.BaseAntTest#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		// Read content of our test rich text content.
		// This content is a XML content.
		this.richTextContent = Utils.read(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/asi/designer/anttasks/tasks/dummyRichTextContent.txt"),
				"UTF-8"
		);
	}

	/**
	 * Test the DXL Export and import task
	 */
	public void testDxlImportExport() throws Exception {
		File exportFile = File.createTempFile("dxlExport", ".dxl");
		exportFile.deleteOnExit();
		this.setProperty("srcDb", "tests/testDxlImportExportSrc.nsf");
		this.setProperty("destDb", "tests/testDxlImportExportDest.nsf");
		this.setProperty("file", exportFile.getAbsolutePath());
		
		// Create the two databases
		DatabaseCreate create = new DatabaseCreate();
		create.setPassword(this.getProperty("password"));
		create.setServer(this.getProperty("server"));
		create.setDatabase(this.getProperty("srcDb"));
		create.execute();
		create = new DatabaseCreate();
		create.setPassword(this.getProperty("password"));
		create.setServer(this.getProperty("server"));
		create.setDatabase(this.getProperty("destDb"));
		create.execute();
		
		// Create a set of documents in the source db
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(session, TestTasks.this.getProperty("server"), TestTasks.this.getProperty("srcDb"));
				
				// These documents will be exported
				for( int i=0; i<10; i++ ) {
					Document doc = db.createDocument();
					doc.replaceItemValue("Form", "DxlExport");
					doc.replaceItemValue("IntField", i);
					doc.replaceItemValue("DateField", session.createDateTime(new Date()));
					Vector<String> v = new Vector<String>();
					v.addAll(Arrays.asList("Valeur1", "Valeur2"));
					doc.replaceItemValue("Multi", v);
					
					// Insert the text content as a single paragraph
					RichTextItem rtIt = doc.createRichTextItem("RtItem");
					rtIt.appendText(TestTasks.this.richTextContent);
					
					doc.save(true, false);
				}
				
				// This one will not
				Document doc = db.createDocument();
				doc.replaceItemValue("Form", "Other");
				doc.save(true, false);
				
				return null;
			}
		});
		
		// Run the task
		this.runAntTask("tasks/TestDxlImportExport.xml");
		
		// Check that the documents are present in the dest database
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(session, TestTasks.this.getProperty("server"), TestTasks.this.getProperty("destDb"));
				DocumentCollection coll = db.getAllDocuments();
				Assert.assertEquals(10, coll.getCount());
				
				Document doc = coll.getFirstDocument();
				while( doc != null ) {
					Assert.assertEquals("DxlExport", doc.getItemValueString("Form"));
					
					Item dateIt = doc.getFirstItem("DateField");
					Assert.assertNotNull(dateIt);
					
					Item multi = doc.getFirstItem("Multi");
					Assert.assertNotNull(multi);
					Assert.assertEquals(2, multi.getValues().size());
					
					Item i = doc.getFirstItem("IntField");
					Assert.assertEquals(Item.NUMBERS, i.getType());
					
					RichTextItem rtIt = (RichTextItem) doc.getFirstItem("RtItem");
					String s = rtIt.getUnformattedText();
					Assert.assertEquals(TestTasks.this.richTextContent, s);
					
					doc = coll.getNextDocument();
				}
				return null;
			}
		});
	}
	
	/**
	 * Test the field export and import tasks
	 */
	public void testFieldImportExport() throws Exception {
		File exportFile = File.createTempFile("fieldExport", ".dxl");
		exportFile.deleteOnExit();
		this.setProperty("db", "tests/testFieldImportExport.nsf");
		this.setProperty("file", exportFile.getAbsolutePath());
		
		// Create the main database
		DatabaseCreate create = new DatabaseCreate();
		create.setPassword(this.getProperty("password"));
		create.setServer(this.getProperty("server"));
		create.setDatabase(this.getProperty("db"));
		create.execute();
		
		// Create the documents
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(session, TestTasks.this.getProperty("server"), TestTasks.this.getProperty("db"));
				
				// The source document
				Document source = db.createDocument();
				source.replaceItemValue("Form", "Export");
				source.replaceItemValue("IntField", 12);
				source.replaceItemValue("DateField", session.createDateTime(new Date()));
				Vector<String> v = new Vector<String>();
				v.addAll(Arrays.asList("Valeur1", "Valeur2"));
				source.replaceItemValue("Multi", v);
				
				// Insert the rich text as multiple paragraphs (like when editing with the notes client)
				RichTextItem rtIt = source.createRichTextItem("RtItem");
				StringReader sreader = new StringReader(TestTasks.this.richTextContent);
				BufferedReader breader = new BufferedReader(sreader);
				try {
					String line = breader.readLine();
					while( line != null ) {
						rtIt.appendText(line);
						rtIt.addNewLine();
						line = breader.readLine();
					}
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
				
				source.save(true, false);
				
				// The destination doc
				Document dest = db.createDocument();
				dest.replaceItemValue("Form", "Import");
				dest.save(true, false);
				
				return null;
			}
		});
		
		// Run the task
		this.runAntTask("tasks/TestFieldImportExport.xml");
		
		// Check that the dest document have been updated
		DominoUtils.runInSession(this.getProperty("password"), new DominoUtils.NotesRunnable<Void>() {
			public Void run(Session session) throws NotesException {
				Database db = DominoUtils.openDatabase(session, TestTasks.this.getProperty("server"), TestTasks.this.getProperty("db"));
				DocumentCollection coll = db.search("Form = 'Import'");
				Assert.assertEquals(1, coll.getCount());
				
				Document doc = coll.getFirstDocument();
				Assert.assertEquals(12, doc.getItemValueInteger("IntField"));
				
				Item dateIt = doc.getFirstItem("DateField");
				Assert.assertNotNull(dateIt);
					
				Item multi = doc.getFirstItem("Multi");
				Assert.assertNotNull(multi);
				Assert.assertEquals(2, multi.getValues().size());
				
				RichTextItem rtIt = (RichTextItem) doc.getFirstItem("RtItem");
				String s = rtIt.getUnformattedText();
				Assert.assertEquals(TestTasks.this.richTextContent, s);
				
				return null;
			}
		});
	}
}
