package fr.asi.designer.anttasks.domino.impl;

import static fr.asi.designer.anttasks.util.DominoUtils.openDatabase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import lotus.domino.Database;
import lotus.domino.DxlExporter;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.asi.designer.anttasks.domino.NotesTask;
import fr.asi.designer.anttasks.util.XmlUtils;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Export a set of documents into a DXL file
 * @author Lionel HERVIER
 */
public class DxlExport extends NotesTask {

	/**
	 * Un log succ�s
	 */
	public static final String EXPORT_SUCCESS = "<?xml version='1.0'?>\n" +
			"<DXLExporterLog>\n" +
			"</DXLExporterLog>";
	
	/**
	 * Server
	 */
	private String server;
	
	/**
	 * Database
	 */
	private String database;
	
	/**
	 * Formula
	 */
	private String formula;
	
	/**
	 * Destination file
	 */
	private String toFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.NotesTask#execute(lotus.domino.Session)
	 */
	@Override
	protected void execute(Session session) throws NotesException {
		this.log("Exporting " + this.formula + " from " + this.server + "!!" + this.database + " to " + this.toFile);
		Database db = null;
		NoteCollection nc = null;
		DxlExporter exporter = null;
		
		File f = new File(this.toFile);
		if( !f.isAbsolute() )
			f = new File(this.getProject().getProperty("basedir") + "/" + this.toFile);
		OutputStream out = null;
		Writer writer = null;
		try {
			// Build the document collection
			db = openDatabase(this.getSession(), this.server, this.database);
			nc = db.createNoteCollection(false);
			nc.setSelectDocuments(true);
			nc.setSelectionFormula(this.formula);
			nc.buildCollection();
			
			// Export as a native DXL string
			exporter = session.createDxlExporter();
			exporter.setRichTextOption(DxlExporter.DXLRICHTEXTOPTION_RAW);
			exporter.setOutputDOCTYPE(false);
			String dxl = exporter.exportDxl(nc);
			if( !EXPORT_SUCCESS.equals(exporter.getLog()) )
				throw new BuildException("Unable to export DXL: " + exporter.getLog());
			
			// Extract to a DOM object so we can clean it
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(dxl.getBytes("UTF-8"));
			org.w3c.dom.Document document = builder.parse(in, "UTF-8");
			in.close();
			
			// Clean the XML
			this.clean(document.getDocumentElement());
			
			// Extract as pretty XML string
			OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            
            // Prepare the stream
            Utils.createFolder(f.getParentFile());		// Ensure folder exists
    		if( f.exists() )
    			if( !f.delete() )
    				throw new BuildException("Unable to replace file " + f.getAbsolutePath());
    		f.createNewFile();
    		out = new FileOutputStream(f);
			writer = new OutputStreamWriter(out, "UTF-8");
            XMLSerializer serializer = new XMLSerializer(writer, format);
            serializer.serialize(document);
		} catch (ParserConfigurationException e) {
			throw new BuildException(e);
		} catch (UnsupportedEncodingException e) {
			throw new BuildException(e);
		} catch (SAXException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new BuildException(e);
		} finally {
			Utils.closeQuietly(writer);
			Utils.closeQuietly(out);
			
			Utils.recycleQuietly(exporter);
			Utils.recycleQuietly(nc);
			Utils.recycleQuietly(db);
		}
	}

	/**
	 * Clean a DXL document so we remove elements that are not mandatory
	 * to re-generate the documents, like replicaid, version, noteinfo, updatedby, wassignedby, etc...
	 * @param rootElt root element
	 */
	private void clean(Element rootElt) {
		// Clean database tag
		XmlUtils.removeAttributes(rootElt, "replicaid", "version", "path", "maintenanceversion");
		XmlUtils.removeChild(rootElt, "databaseinfo", "fulltextsettings", "launchsettings");
		
		// Clean documents
		NodeList docLst = rootElt.getElementsByTagName("document");
		for( int i=0; i<docLst.getLength(); i++ ) {
			Element doc = (Element) docLst.item(i);
			XmlUtils.removeChild(doc, "noteinfo", "updatedby", "revisions");
		}
	}
	
	// ===================================================================================
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @param toFile the toFile to set
	 */
	public void setToFile(String toFile) {
		this.toFile = toFile;
	}

	
}
