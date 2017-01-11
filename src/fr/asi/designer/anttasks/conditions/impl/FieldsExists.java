package fr.asi.designer.anttasks.conditions.impl;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.conditions.BaseDatabaseSetCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A condition to check if a given document contains a given field
 * @author Lionel HERVIER
 */
public class FieldsExists extends BaseDatabaseSetCondition {

	/**
	 * The formula. Must select only one document.
	 */
	private String formula;
	
	/**
	 * The field names (separated with coma)
	 */
	private String fields;

	/**
	 * @see fr.asi.designer.anttasks.conditions.BaseDatabaseSetCondition#eval(Database)
	 */
	@Override
	protected boolean eval(List<Database> databases) throws NotesException {
		for( Database db : databases ) {
			DocumentCollection coll = null;
			Document doc = null;
			try {
				coll = db.search(this.formula);
				if( coll.getCount() != 1 )
					throw new BuildException("The formula must select only one document");
				
				String[] tblFields = this.fields.split(",");
				for( int i=0; i<tblFields.length; i++ )
					tblFields[i] = tblFields[i].trim();
				
				doc = coll.getFirstDocument();
				for( String f : tblFields ) {
					if( !doc.hasItem(f) )
						return false;
					Item it = doc.getFirstItem(f);
					if( it.getType() == Item.TEXT && Utils.isEmpty(it.getValueString()) )
						return false;
				}
			} finally {
				Utils.recycleQuietly(doc);
				Utils.recycleQuietly(coll);
			}
		}
		return true;
	}
	
	// =============================================================================

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(String fields) {
		this.fields = fields;
	}
}
