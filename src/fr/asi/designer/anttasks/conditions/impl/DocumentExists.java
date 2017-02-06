package fr.asi.designer.anttasks.conditions.impl;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.conditions.DatabaseSetCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Condition to check if a given set of documents exists in 
 * a set of databases
 * @author Lionel HERVIER
 */
public class DocumentExists extends DatabaseSetCondition {

	/**
	 * The formula
	 */
	private String formula;
	
	/**
	 * @see fr.asi.designer.anttasks.conditions.DatabaseSetCondition#eval(java.util.List)
	 */
	@Override
	protected boolean eval(List<Database> databases) throws NotesException {
		for( Database db : databases ) {
			DocumentCollection coll = null;
			try {
				coll = db.search(this.formula);
				if( coll.getCount() == 0 )
					return false;
			} finally {
				Utils.recycleQuietly(coll);
			}
		}
		return true;
	}
	
	// ===================================================================================

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
}
