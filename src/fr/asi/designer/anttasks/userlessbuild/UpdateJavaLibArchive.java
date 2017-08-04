package fr.asi.designer.anttasks.userlessbuild;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.jdom2.JDOMException;

import sun.misc.BASE64Encoder;
import fr.asi.designer.anttasks.util.Utils;
import fr.asi.designer.anttasks.util.XmlUtils;

/**
 * Task to generate a .javalib file from a java source folder
 * @author Lionel HERVIER
 */
public class UpdateJavaLibArchive extends Task {

	/**
	 * The javalib file to update
	 */
	private String javaLibPath;
	
	/**
	 * Path to the jar file 
	 */
	private String jarFilePath;
	
	/**
	 * Name of the jar file to replace
	 */
	private String jarName;
	
	/**
	 * @param javaLibPath the javaLibPath to set
	 */
	public void setJavaLibPath(String javaLibPath) {
		this.javaLibPath = javaLibPath;
	}

	/**
	 * @param jarFilePath the jarFilePath to set
	 */
	public void setJarFilePath(String jarFilePath) {
		this.jarFilePath = jarFilePath;
	}

	/**
	 * @param jarName the jarName to set
	 */
	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	/**
	 * Execution
	 */
	public void execute() {
		try {
			// Extract file parameters
			File fJavaLib = new File(this.getProject().getProperty("basedir") + "/" + this.javaLibPath);
			if( !fJavaLib.exists() )
				throw new RuntimeException("La javalib n'existe pas...");
			
			File fJarFile = new File(this.getProject().getProperty("basedir") + "/" + this.jarFilePath);
			if( !fJarFile.exists() )
				throw new RuntimeException("Le jar n'existe pas");
			
			// Extract jar file as base64
			BASE64Encoder encoder = new BASE64Encoder();
			String base64 = encoder.encode(Utils.readFile(fJarFile));
			base64 = base64.replaceAll("\\r\\n", "\n");
			
			// Replace the content of the javalib file
			String xpath = "//*[name()='item' and @name='$FILE']/*[name()='object']/*[name()='file' and @name='" + this.jarName + "']/*[name()='filedata']";
			XmlUtils.replaceElementContent(fJavaLib, xpath, "\n" + base64 + "\n");
			
			// Update the "size" attribute
			xpath = "//*[name()='item' and @name='$FILE']/*[name()='object']/*[name()='file' and @name='" + this.jarName + "']/@size";
			XmlUtils.replaceAttributeContent(fJavaLib, xpath, Long.toString(fJarFile.length()));
			
			// Update the modified date
			xpath = "//*[name()='item' and @name='$FILE']/*[name()='object']/*[name()='file' and @name='" + this.jarName + "']/*[name()='modified']/*[name()='datetime']";
			// 20170803T151736,92+02
			DateFormat df = new SimpleDateFormat("yyyyMMdd'T'hhmmss,'00'Z");
			String dt = df.format(new Date());
			dt = dt.substring(0, dt.length() - 2);
			XmlUtils.replaceElementContent(fJavaLib, xpath, dt);
			
		} catch(RuntimeException e) {
			this.log(e, Project.MSG_ERR);
			throw e;
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (JDOMException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}
