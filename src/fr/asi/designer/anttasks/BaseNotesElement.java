package fr.asi.designer.anttasks;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.util.ObjectHolder;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A task that runs inside a Notes Session
 * @author Lionel HERVIER
 */
public abstract class BaseNotesElement<E> {

	/**
	 * The project
	 */
	private Project project;
	
	/**
	 * Password of the local id file
	 */
	private String password;
	
	/**
	 * The notes session
	 */
	private Session session;
	
	/**
	 * Execution
	 * @throws NotesException in cas of trouble...
	 */
	protected abstract E run(Session session) throws NotesException;
	
	/**
	 * Execution
	 */
	public final E run() {
		final ObjectHolder<Throwable> exHolder = new ObjectHolder<Throwable>();
		final ObjectHolder<E> valueHolder = new ObjectHolder<E>();
		Thread t = new NotesThread() {
			public void runNotes() {
				try {
					BaseNotesElement.this.session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) BaseNotesElement.this.password
					);
					valueHolder.value = BaseNotesElement.this.run(BaseNotesElement.this.session);
				} catch(Throwable e) {
					exHolder.value = e;
				} finally {
					Utils.recycleQuietly(BaseNotesElement.this.session);
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		if( exHolder.value != null ) {
			exHolder.value.printStackTrace(System.err);
			throw new BuildException(exHolder.value);
		}
		return valueHolder.value;
	}
	
	/**
	 * Returns a delegated task
	 * @param cl the class of the object to return
	 * @return the delegated task
	 */
	public <T> T delegate(Class<T> cl) {
		T ret;
		try {
			// Extract every readable property value
			BeanInfo srcBeanInfo = Introspector.getBeanInfo(this.getClass());
			PropertyDescriptor[] srcDescriptors = srcBeanInfo.getPropertyDescriptors();
			Map<String, Object> mSrcDescriptors = new HashMap<String, Object>();
			for( PropertyDescriptor desc : srcDescriptors ) {
				String name = desc.getName();
				
				// Try to read from getter method
				if( desc.getReadMethod() != null ) {
					mSrcDescriptors.put(name, desc.getReadMethod().invoke(this, new Object[] {}));
				
				// Try to read the value from a field
				} else {
					try {
						Field field = Utils.getField(this.getClass(), name);
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						mSrcDescriptors.put(name, field.get(this));
						field.setAccessible(accessible);
					} catch(NoSuchFieldException e) {
						// Field does not exists...
					}
				}
			}
			
			// Instanciate the new (delegated) object
			ret = cl.newInstance();
			
			// Define each property from the dest object from the value of the src object
			BeanInfo destBeanInfo = Introspector.getBeanInfo(cl);
			PropertyDescriptor[] destDescriptors = destBeanInfo.getPropertyDescriptors();
			for( PropertyDescriptor desc : destDescriptors ) {
				String name = desc.getName();
				
				// Only define properties present in the source object
				if( !mSrcDescriptors.containsKey(name) )
					continue;
				Object value = mSrcDescriptors.get(name);
				
				// Try to define value with the setter method
				if( desc.getWriteMethod() != null ) {
					desc.getWriteMethod().invoke(ret, new Object[] {value});
				
				// Try to set value of a field
				} else {
					try {
						Field field = Utils.getField(cl, name);
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						field.set(ret, value);
						field.setAccessible(accessible);
					} catch(NoSuchFieldException e) {
						// Field does not exists
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new BuildException(e);
		} catch (InstantiationException e) {
			throw new BuildException(e);
		} catch (IntrospectionException e) {
			throw new BuildException(e);
		} catch (IllegalArgumentException e) {
			throw new BuildException(e);
		} catch (InvocationTargetException e) {
			throw new BuildException(e);
		} catch (SecurityException e) {
			throw new BuildException(e);
		}
		
		return ret;
	}
	
	/**
	 * Log a message
	 */
	public void log(String message) {
		if( this.project == null )
			System.out.println(message);
		else
			this.project.log(message, Project.MSG_INFO);
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	// ========================= GETTERS AND SETTERS ======================================
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}
}
