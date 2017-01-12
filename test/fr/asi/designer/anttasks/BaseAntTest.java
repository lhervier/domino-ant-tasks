package fr.asi.designer.anttasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.ibm.jvm.util.ByteArrayOutputStream;

import fr.asi.designer.anttasks.util.Utils;

public abstract class BaseAntTest extends TestCase {

	/**
	 * Password of the current id
	 */
	private static final String PASSWORD = "";
	
	/**
	 * Name of the current server
	 */
	private static final String SERVER = "HLINK/HERMES";
	
	/**
	 * Properties sent to tests
	 */
	private Map<String, String> props = new HashMap<String, String>();
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.setProperty("password", PASSWORD);
		this.setProperty("server", SERVER);
	}
	
	/**
	 * Define a property
	 */
	public void setProperty(String name, String value) {
		this.props.put(name, value);
	}
	
	/**
	 * Return the value of a property
	 */
	public String getProperty(String name) {
		return this.props.get(name);
	}
	
	/**
	 * Launch an ant task on a given build file
	 * @param buildXml path (in the classpath) to the build.xml
	 * @return a string for every line of <echo> task
	 * @throws IOException
	 */
	public List<String> runAntTask(String buildXml) throws IOException {
		return this.runAntTask(buildXml, null);
	}
	
	/**
	 * Launch an ant task on a given build file
	 * @param buildXml path (in the classpath) to the build.xml
	 * @param target a specific target to execute
	 * @return a string for every line of <echo> task
	 * @throws IOException
	 */
	public List<String> runAntTask(
			String buildXml, 
			String target) throws IOException {
		File f = File.createTempFile("build", ".xml");
		try {
			// Copy the build.xml from resource to the file system.
			InputStream in = null;
			OutputStream out = null;
			try {
				in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/asi/designer/anttasks/build/" + buildXml);
				out = new FileOutputStream(f);
				byte[] buffer = new byte[4 * 1024];
				int read = in.read(buffer);
				while( read != -1 ) {
					out.write(buffer, 0, read);
					read = in.read(buffer);
				}
			} finally {
				Utils.closeQuietly(in);
				Utils.closeQuietly(out);
			}
			
			// Prepare logging
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream outPw = new PrintStream(bout);
			ByteArrayOutputStream berr = new ByteArrayOutputStream();
			PrintStream errPw = new PrintStream(berr);
			
			// Run build file and extract output in a string
			Project project = new Project();
			ProjectHelper.configureProject(project, f);
			DefaultLogger consoleLogger = new DefaultLogger();
			consoleLogger.setErrorPrintStream(errPw);
			consoleLogger.setOutputPrintStream(outPw);
			consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
			project.addBuildListener(consoleLogger);
			
			for( String key : this.props.keySet() )
				project.setProperty(key, this.props.get(key));
			
			project.init();
			
			String sOut = "";
			try {
				if( target != null )
					project.executeTarget(target);
				else
					project.executeTarget(project.getDefaultTarget());
			} catch(Throwable e) {
				e.printStackTrace(System.err);
			} finally {
				sOut = new String(bout.toByteArray());
				
				// Log what the ant task returns
				System.err.println(new String(berr.toByteArray()));
				System.out.println(sOut);
			}
			
			// Extract lines from echo task
			List<String> ret = new ArrayList<String>();
			StringReader reader = new StringReader(sOut);
			BufferedReader breader = new BufferedReader(reader);
			String line = breader.readLine();
			while( line != null ) {
				int pos = line.indexOf("[echo] ");
				if( pos != -1 ) {
					ret.add(line.substring(pos + "[echo] ".length()));
				}
				line = breader.readLine();
			}
			
			return ret;
		} finally {
			if( !f.delete() )
				throw new RuntimeException("Unable to remove file " + f.getAbsolutePath());
		}
	}
}
