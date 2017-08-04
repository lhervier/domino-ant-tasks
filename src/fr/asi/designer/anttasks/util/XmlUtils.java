package fr.asi.designer.anttasks.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Usefull dom functions
 * @author Lionel HERVIER
 */
public class XmlUtils {

	/**
	 * Remove attributes from a parent node
	 * @param parentNode the parent Node
	 * @param attributes the attributes names to remove
	 */
	public static void removeAttributes(Element parentNode, String... attributes) {
		for( String attr : attributes )
			parentNode.removeAttribute(attr);
	}
	
	/**
	 * Remove child nodes from a parent node
	 * @param parentNode the parent Node
	 * @param childNodes the name of the child nodes to remove
	 */
	public static void removeChild(Element parentNode, String... childNodes) {
		List<String> nodesToRemove = Arrays.asList(childNodes);
		NodeList children = parentNode.getChildNodes();
		List<Node> toRemove = new ArrayList<Node>();
		for( int i=0; i<children.getLength(); i++ ) {
			Node currNode = children.item(i);
			if( nodesToRemove.contains(currNode.getNodeName()) )
				toRemove.add(currNode);
		}
		for( Iterator<Node> it = toRemove.iterator(); it.hasNext(); ) {
			Node nd = it.next();
			parentNode.removeChild(nd);
		}
	}
	
	/**
	 * Update an XML File
	 * @param xmlFile the xml file to update
	 * @param xpath the xpath expression that will select an element
	 * @param newValue the element new value
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static final void replaceElementContent(File xmlFile, String xpath, String newValue) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlFile);
		
		org.jdom2.Element toReplace = null;
		
		XPathFactory xpfac = XPathFactory.instance();
		XPathExpression<org.jdom2.Element> xp = xpfac.compile(xpath, Filters.element());
		for( org.jdom2.Element elt : xp.evaluate(doc)) {
			toReplace = elt;
			break;
		}
		
		if( toReplace == null )
			throw new RuntimeException("Unable to find node corresponding to xpath expression");
		
		toReplace.setText(newValue);
		
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getRawFormat());
		xmlOutput.output(doc, new FileWriter(xmlFile));
	}
	
	/**
	 * Update an XML File
	 * @param xmlFile the xml file to update
	 * @param xpath the xpath expression that will select an element
	 * @param newValue the element new value
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static final void replaceAttributeContent(File xmlFile, String xpath, String newValue) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlFile);
		
		Attribute toReplace = null;
		
		XPathFactory xpfac = XPathFactory.instance();
		XPathExpression<Attribute> xp = xpfac.compile(xpath, Filters.attribute());
		for( Attribute elt : xp.evaluate(doc)) {
			toReplace = elt;
			break;
		}
		
		if( toReplace == null )
			throw new RuntimeException("Unable to find attribute corresponding to xpath expression");
		
		toReplace.setValue(newValue);
		
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getRawFormat());
		xmlOutput.output(doc, new FileWriter(xmlFile));
	}
}
