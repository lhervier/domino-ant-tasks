package fr.asi.designer.anttasks.domino;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.util.Utils;

/**
 * Base class for tasks that runs on design elements
 * @author Lionel HERVIER
 */
public abstract class DesignElementTask extends DatabaseSetTask {

	/**
	 * Enumeration of all supported design elements
	 */
	public static enum Type {
		ALL,
		ACTIONS,
		AGENTS,
		APPLETS,
		DATABASE_SCRIPTS,
		COLUMNS,
		DATA_CONNECTIONS,
		FILE_RESOURCE,
		HIDDEN_FILE,
		CUSTOM_CONTROLS,
		THEMES,
		XPAGES,
		FOLDERS,
		FORMS,
		FRAMESETS,
		NAVIGATORS,
		OUTLINES,
		PAGES,
		PROFILES,
		SCRIPT_LIBRARIES,
		WEB_SERVICE_CONSUMERS,
		WEB_SERVICE_PROVIDERS,
		SHARED_FIELDS,
		SUBFORMS,
		VIEWS,
		WIRING_PROPERTIES,
		COMPOSITE_APPLICATIONS,
		IMAGES,
		STYLESHEETS,
		DB2_ACCESS_VIEWS,
		ICON
	}
	
	/**
	 * The design elements to select
	 */
	private String select = Type.ALL.name();
	
	/**
	 * Execute the task on the design element
	 * @param type the type of the design element
	 * @param designElement the backend document for the design element
	 * @throws NotesException
	 */
	protected abstract void execute(Type type, Document designElement) throws NotesException;
	
	/**
	 * @see fr.asi.designer.anttasks.DatabaseSetElement#execute(Database)
	 */
	@Override
	protected void execute(Database db) throws NotesException {
		NoteCollection nc = null;
		try {
			nc = db.createNoteCollection(false);
			
			String[] selects = this.select.split(",");
			for( String select : selects ) {
				// Selecting all design elements => Delegates to sub tasks
				
				// Build collection
				// See https://www-10.lotus.com/ldd/ddwiki.nsf/dx/ls-design-programming.htm (version 1 !!!!!)
				Type t = Type.valueOf(select);
				if( Type.ALL.equals(t) ) {
					nc.selectAllDesignElements(true);
				} else if( Type.ACTIONS.equals(t) ) {
					nc.setSelectActions(true);
				} else if( Type.AGENTS.equals(t) ) {
					nc.setSelectAgents(true);
				} else if( Type.APPLETS.equals(t) ) {
					nc.selectAllDesignElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"@\")");
				} else if( Type.DATABASE_SCRIPTS.equals(t) ) {
					nc.setSelectDatabaseScript(true);
				} else if( Type.COLUMNS.equals(t) ) {
					nc.setSelectMiscIndexElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"^\")");
				} else if( Type.DATA_CONNECTIONS.equals(t) ) {
					nc.setSelectDataConnections(true);
				} else if( Type.FILE_RESOURCE.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"g\") & !@Matches($Flags; \"*{~K[];`}*\")");
				} else if( Type.HIDDEN_FILE.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"~\") & !@Matches($Flags; \"*{~K[];`}*\")");
				} else if( Type.CUSTOM_CONTROLS.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \";\")");
				} else if( Type.THEMES.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"`\")");
				} else if( Type.XPAGES.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"K\")");
				} else if( Type.FOLDERS.equals(t) ) {
					nc.setSelectFolders(true);
				} else if( Type.FORMS.equals(t) ) {
					nc.setSelectForms(true);
				} else if( Type.FRAMESETS.equals(t) ) {
					nc.setSelectFramesets(true);
				} else if( Type.NAVIGATORS.equals(t) ) {
					nc.setSelectNavigators(true);
				} else if( Type.OUTLINES.equals(t) ) {
					nc.setSelectOutlines(true);
				} else if( Type.PAGES.equals(t) ) {
					nc.setSelectPages(true);
				} else if( Type.PROFILES.equals(t) ) {
					nc.setSelectProfiles(true);
				} else if( Type.SCRIPT_LIBRARIES.equals(t) ) {
					nc.setSelectScriptLibraries(true);
					nc.setSelectionFormula("!@Contains($FlagsExt; \"W\")");
				} else if( Type.WEB_SERVICE_CONSUMERS.equals(t) ) {
					nc.setSelectScriptLibraries(true);
					nc.setSelectionFormula("@Contains($FlagsExt; \"W\")");
				} else if( Type.WEB_SERVICE_PROVIDERS.equals(t) ) {
					nc.setSelectMiscCodeElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"{\")");
				} else if( Type.SHARED_FIELDS.equals(t) ) {
					nc.setSelectSharedFields(true);
				} else if( Type.SUBFORMS.equals(t) ) {
					nc.setSelectSubforms(true);
				} else if( Type.VIEWS.equals(t) ) {
					nc.setSelectViews(true);
				} else if( Type.WIRING_PROPERTIES.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \":\")");
				} else if( Type.COMPOSITE_APPLICATIONS.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"|\")");
				} else if( Type.IMAGES.equals(t) ) {
					nc.setSelectImageResources(true);
				} else if( Type.STYLESHEETS.equals(t) ) {
					nc.setSelectStylesheetResources(true);
				} else if( Type.DB2_ACCESS_VIEWS.equals(t) ) {
					nc.setSelectMiscFormatElements(true);
					nc.setSelectionFormula("@Contains($Flags; \"z\")");
				} else if( Type.ICON.equals(t) ) {
					nc.setSelectIcon(true);
				} else
					throw new BuildException("Unknown design element type '" + select + "'");
				
				nc.buildCollection();
				
				String noteId = nc.getFirstNoteID();
				while( !Utils.isEmpty(noteId) ) {
					Document doc = null;
					try {
						doc = db.getDocumentByID(noteId);
						this.execute(t, doc);
					} finally {
						Utils.recycleQuietly(doc);
					}
					noteId = nc.getNextNoteID(noteId);
				}
			}
		} finally {
			Utils.recycleQuietly(nc);
		}
	}
	
	// ======================================================================

	/**
	 * @param select the select to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}
}
