package fr.asi.designer.anttasks;

import static fr.asi.designer.anttasks.util.DominoUtils.openDatabase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

import fr.asi.designer.anttasks.conditions.BaseDatabaseSetCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A Set of domino databases
 * @author Lionel HERVIER
 */
public class DatabaseSet extends ConditionBase {

	/**
	 * the parent task
	 */
	private DatabaseSetElement parentDatabaseSetElement;
	
	/**
	 * A Server name
	 */
	private String server;
	
	/**
	 * A template name the databases must inherit from
	 */
	private String template;

	/**
	 * A database name
	 */
	private String database;
	
	/**
	 * Return the databases
	 * @return the databases
	 * @throws NotesException
	 */
	public List<Database> getDatabases() throws NotesException {
		List<Database> ret = new ArrayList<Database>();
		
		// Server name
		String server;
		if( this.server == null )
			server = this.parentDatabaseSetElement.getServer();
		else
			server = this.server;
		
		// Extract the list of all the corresponding databases
		if( !Utils.isEmpty(this.database) ) {
			if( this.isSelectedDatabase(this.database) ) {
				ret.add(openDatabase(this.parentDatabaseSetElement.getSession(), server, this.database));
			}
		
		} else if( !Utils.isEmpty(this.template) ) {
			DbDirectory dir = null;
			try {
				dir = this.parentDatabaseSetElement.getSession().getDbDirectory(server);
				Database db = dir.getFirstDatabase(DbDirectory.DATABASE);
				while( db != null ) {
					if( db.getDesignTemplateName().equals(this.template) ) {
						if( this.isSelectedDatabase(db) ) {
							if( !db.isOpen() )
								if( !db.open() )
									throw new BuildException("Unable to open database '" + server + "!!" + db.getFilePath() + "'");
							ret.add(db);
						}
					}
					db = dir.getNextDatabase();
				}
			} finally {
				Utils.recycleQuietly(dir);
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns true if the selected database must be part
	 * of the database set
	 * @param database the database
	 * @return true or false
	 * @throws NotesException
	 */
	private boolean isSelectedDatabase(String database) throws NotesException {
		Database db = null;
		try {
			db = openDatabase(this.parentDatabaseSetElement.getSession(), this.parentDatabaseSetElement.getServer(), database);
			return this.isSelectedDatabase(db);
		} finally {
			Utils.recycleQuietly(db);
		}
	}
	
	/**
	 * Returns true if the selected database must be part
	 * of the database set
	 * @param database the database
	 * @return true or false
	 * @throws NotesException
	 */
	private boolean isSelectedDatabase(Database database) throws NotesException {
		if( this.countConditions() == 0 )
			return true;
		Condition c = this.getCondition();
		this.associate(c, database);		// We need to associate the condition to the current notes context.
		return this.getCondition().eval();
	}
	
	/**
	 * @return the condition
	 */
	private Condition getCondition() {
		return (Condition) this.getConditions().nextElement();
	}
	
	/**
	 * Associate the conditions to the current database
	 * @param condition the condition
	 * @param db the database
	 * @throws NotesException
	 */
	private void associate(Condition condition, Database db) throws NotesException {
		if( condition instanceof BaseDatabaseSetCondition ) {
			// Force the condition to run on the current database only.
			// If a condition uses a databaseSet when used itself inside a databaseSet, it will be ignored.
			BaseDatabaseSetCondition c = (BaseDatabaseSetCondition) condition;
			c.clearDatabaseSet();
			c.setServer(db.getServer());
			c.setDatabase(db.getFilePath());
			c.setPassword(this.parentDatabaseSetElement.getPassword());
		}
		
		if( condition instanceof ConditionBase ) {
			ConditionBase cb = (ConditionBase) condition;
			Enumeration<Condition> en = this.getSubConditions(cb);
			while( en.hasMoreElements() )
				this.associate(en.nextElement(), db);
		}
	}
	
	/**
	 * Return the subconditions of a given condition.
	 * FIXME: Awful hack... getConditions is protected on ConditionBase. So i use reflection to call it...
	 * @param condition the condition
	 * @return the sub conditions
	 */
	@SuppressWarnings("unchecked")
	private Enumeration<Condition> getSubConditions(ConditionBase condition) {
		try {
			Method m = ConditionBase.class.getDeclaredMethod("getConditions", new Class<?>[] {});
			m.setAccessible(true);
			return (Enumeration<Condition>) m.invoke(condition, new Object[] {});
		} catch(Throwable e) {
			throw new BuildException(e);
		}
	}
	
	// ===============================================================================================
	
	/**
	 * @param parentElement the parentElement to set
	 */
	public void setParentDatabaseSetElement(DatabaseSetElement parentElement) {
		this.parentDatabaseSetElement = parentElement;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}
}
