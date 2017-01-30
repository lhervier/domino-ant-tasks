package fr.asi.designer.anttasks.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;

import lotus.domino.Base;
import lotus.domino.NotesException;
import lotus.domino.Stream;

public class Utils {
	
	/**
	 * To detect if a string is empty
	 * @param s the string to test
	 * @return true if the string is empty ("" or null)
	 */
	public final static boolean isEmpty(String s) {
		if( s == null )
			return true;
		if( s.length() == 0 )
			return true;
		return false;
	}
	
	/**
	 * Read the content of a file.
	 * @param f the file to read
	 * @param encoding the content encoding
	 * @return the text content of the file
	 * @throws IOException 
	 */
	public final static String readFile(File f, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(f);
			return Utils.read(in, encoding);
		} finally {
			closeQuietly(in);
		}
	}
	
	/**
	 * Read the content of a stream.
	 * @param in the stream to read
	 * @param encoding the encoding to use
	 * @return the text content of the file
	 * @throws IOException 
	 */
	public final static String read(InputStream in, String encoding) throws IOException {
		StringBuffer sb = new StringBuffer();
		Reader reader = null;
		try {
			reader = new InputStreamReader(in, encoding);
			char[] buffer = new char[4 * 1024];
			int read = reader.read(buffer);
			while( read != -1 ) {
				sb.append(buffer, 0, read);
				read = reader.read(buffer);
			}
		} finally {
			closeQuietly(in);
			closeQuietly(reader);
		}
		return sb.toString();
	}
	
	/**
	 * To remove a folder (or a file) and all of its sub folders
	 * @param root the root folder (or file) to delete
	 */
	public final static void deltree(File root) {
		if( root.isDirectory() )
			for( File child : root.listFiles() )
				deltree(child);
		if( root.exists() )
			if( !root.delete() )
				throw new RuntimeException("Unable to remove '" + root.getAbsolutePath() + "'");
	}
	
	/**
	 * Close a object quietly
	 * @param o the object to close
	 */
	public final static void closeQuietly(Closeable o) {
		if( o == null )
			return;
		try {
			o.close();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Close a stream quietly
	 * @param o the stream to close
	 */
	public final static void closeQuietly(Stream o) {
		if( o == null )
			return;
		try {
			o.close();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a folder with all the needed parents folders
	 * @param folder the folder to create
	 */
	public final static void createFolder(File folder) {
		if( folder.exists() )
			return;
		Utils.createFolder(folder.getParentFile());
		folder.mkdir();
	}
	
	/**
	 * Create a new file with the given content
	 * @param content the content to set (using UTF-8 encoding)
	 * @return the newly created temporary file
	 * @throws IOException 
	 */
	public final static File createFile(String content, String encoding) throws IOException {
		return Utils.createFile(null, content, encoding);
	}
	
	/**
	 * Create a new file from the classpath
	 * @param path path to the content in the classpath
	 * @return the newly created temporary file
	 * @throws IOException
	 */
	public final static File createFileFromClassPath(String path) throws IOException {
		return Utils.createFileFromClassPath(null, path);
	}
	
	/**
	 * Create a file from the classpath
	 * @param f the file to fill with
	 * @param path path to the content in the classpath
	 * @return the newly created temporary file
	 * @throws IOException
	 */
	public final static File createFileFromClassPath(File f, String path) throws IOException {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
			return createFile(f, in);
		} finally {
			Utils.closeQuietly(in);
		}
	}

	/**
	 * Create a new file with the given content
	 * @param f the file to create
	 * @param content the content to set
	 * @param encoding the encoding of the content
	 * @return the file
	 * @throws IOException 
	 */
	public final static File createFile(File f, String content, String encoding) throws IOException {
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(content.getBytes(encoding));
			return createFile(f, bais);
		} finally {
			Utils.closeQuietly(bais);
		}
	}
	
	/**
	 * Create a new file with the given content
	 * @param file the file to create
	 * @param in inputstream to the content of the file
	 * @return the file
	 * @throws IOException 
	 */
	public final static File createFile(File f, InputStream in) throws IOException {
		// Create a temp file if needed
		if( f == null ) {
			f = File.createTempFile("file", "tmp");
			f.deleteOnExit();
		}
		
		// On s'assure que son dossier parent existe
		createFolder(f.getParentFile());
		
		// Vide le fichier
		if( f.exists() ) {
			if( !f.delete() )
				throw new RuntimeException("Unable to remove file '" + f.getAbsolutePath() + "'");
		}
		f.createNewFile();
		
		// Envoi le contenu dans le fichier
		OutputStream out = null;
		try {
			out = new FileOutputStream(f);
			byte[] buffer = new byte[4 * 1024];
			int read = in.read(buffer);
			while( read != -1 ) {
				out.write(buffer, 0, read);
				read = in.read(buffer);
			}
		} finally {
			Utils.closeQuietly(out);
		}
		return f;
	}
	
	/**
	 * Recycle a notes object
	 * @param o the object to recycle
	 */
	public static final void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return a field for a given class
	 * @param cl the class
	 * @param name the name of the field
	 * @return the value
	 * @throws NoSucFieldException if the field does not exists
	 */
	public static Field getField(Class<?> cl, String name) throws NoSuchFieldException {
		try {
			return cl.getDeclaredField(name);
		} catch(NoSuchFieldException e) {
			if( cl.equals(Object.class) )
				throw e;
			else
				return getField(cl.getSuperclass(), name);
		}
	}
}
