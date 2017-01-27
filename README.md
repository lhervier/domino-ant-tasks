# Ant tasks for Domino

This projet contains a set of ant tasks to work with IBM Domino servers.

## Download jar file ##

A ready to install version of the ant tasks is available at :

<a href="https://www.dropbox.com/s/k2jsqs1ack0u2sb/userlessbuild-ant-tasks-1.1.b2.jar?dl=0">Ant Tasks for Domino</a>

## Compile it yourself ##

- Install ant: http://ant.apache.org (intall java, unzip ant, create env variables JAVA\_HOME and ANT\_HOME)
- Update the build.xml to change the value of the following properties :
	- notes.home : Must point to the root of the install of your local Notes client.
	- ant.home : Must to point to the root of your local ant install.
- In a DOS console, run "ant"
- The compiled library will be made available in the "build" folder.

# Additionnal Ant tasks #

## Running ant using Notes JVM ##

First of all, you will have to install ant. This is pretty simple.

Next, you will have to ask ant to run build.xml files using the local Notes Client JVM. For this, make the JAVA_HOME environment variable point to

	${NOTES_ROOT}/jvm  

Where NOTES_ROOT is where you installed your notes client.

## Alternative: Running ant using another JVM ##

You will have to include your Notes client install folder in the PATH, and add the Notes.jar (present in jvm/lib/ext) file in your ant classpath.

## Declaring the tasks in a build.xml file ##

Put the jar file in a "lib" folder (for example), next to your build.xml file. 

To include the tasks, just use a standard taskdef tag :

	<project name="test">
		...
		<taskdef
				resource="userlessbuild.antlib.xml"
				classpath="lib/userlessbuild-ant-tasks.jar"/>
		...
	</project>

You can also use an old .properties file, but you will miss the additional conditions :

	<project name="test">
		...
		<taskdef
				resource="userlessbuild-ant-tasks.properties"
				classpath="lib/userlessbuild-ant-tasks.jar"/>
		...
	</project>

## Tasks to manipulate source code ##

### runDesignerCommands ###

This command will :

- Launch Domino Designer in userless mode (see http://www-10.lotus.com/ldd/ddwiki.nsf/dx/Headless\_Designer\_Wiki)
- Make it execute the commands you put inside the tag
- It will then loop until Designer stop
- While looping, it will check for the log files, and send their content to the console.

If an error is detected inside the log file, the task will fail.

	<runDesignerCommands designerPath="C:\Notes\">
		config,true,true
		exit
	</runDesignerCommands>

### waitForDesigner ###

Will wait for designer to shutdown. This task doesn't take any parameters.

	<waitForDesigner/>

### setManifestVersion ###

Will update the version inside a MANIFEST.MF file

	<setManifestVersion version="4.6.0" manifestFile="${basedir}/fr.asi.test/META-INF/MANIFEST.MF"/>

### setFeatureVersion ###

Will update the version defined in a feature.xml file

	<setFeatureVersion version="4.6.0" featureXmlFile="${basedir}/fr.asi.test.feature/feature.xml"/>

### setUpdateSiteVersion ###

Will update the features versions inside a site.xml file.

	<setUpdateSiteVersion version="4.6.0" siteXmlFile="${basedir}/fr.asi.test.site/site.xml"/>

### setOnDiskTitle ###

This task will update a ondisk project (mirror of the design of an NSF file) to change the database title.

	<setOnDiskTitle onDiskPath="${basedir}/mydatabase-ondisk" title="New Title"/>

### setOnDiskTemplate ###

This task will change the name of the template that a given database is declaring. It will make the needed changes inside an ondisk project (mirror of the design of an NSF file).

	<setOnDiskTemplate onDiskPath="${basedir}/mydatabase-ondisk" masterTemplateName="mytmpl"/>

## Tasks to manipulate data on Domino Servers ##

These tasks are using the standard notes apis. They will open sessions to the Domino server using the local Notes client. For this, you will have to give them the password of the local ID file.

### DatabaseSet ###

Some tasks supports a nested <databaseSet> tag which allows the task to run on multiple databases. On these tasks, you can define the following properties :

- password: Password of the local ID file to access to the server
- server: The default server to find the different databases on.
- database: A database to run on. This is a shortcut when processing only one database.

The nested databaseSets tags allows the following properties :

- database: The databaseSet will select only this database. 
- server: The server to search the databases on. Defaults to the server defined at the task level.
- template: The database set will correspond to all the databases that rely on the given template name.
- You can also add a standard ant condition element. Examples below...

Launch a task on a single database :

	<atask server="SERVER/ASI" database="mydb.nsf" password="mypassword"/>

Equivalent to :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet database="mydb.nsf"/>
	</atask>

And equivalent to :

	<atask password="mypassword">
		<databaseSet server="SERVER/ASI" database="mydb.nsf"/>
	</atask>

Run on multiple databases (on the same server) :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet database="mydb1.nsf"/>
		<databaseSet database="mydb2.nsf"/>
	</atask>
 
Run on multiple databases (on different servers) :

	<atask password="mypassword">
		<databaseSet server="SERVER1/ASI" database="mydb1.nsf"/>
		<databaseSet server="SERVER2/ASI" database="mydb2.nsf"/>
	</atask>

Run on all databases that inherits from template "tmpl" :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</atask>

Run on db1, db2 and on all database that inherits from tmpl :
	
	<atask server="SERVER/ASI" password="mypassword" database="db1.nsf">
		<databaseSet database="db2.nsf"/>
		<databaseSet template="tmpl"/>
	</atask>

Run a task on all databases that inherits from tmpl, excluding the ones that contains a document based on the 'Param' form :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet template="tmpl">
			<not>
				<documentExists formula="Form = 'Param'"/>
			</not>
		</databaseSet>
	</atask>

### sendConsole ###

This task allows you to send a console command, and wait for it to finish, or wait for it to start.

This task will send a "tell http quit", and will wait until the result of the "show task" command no longer contains the "HTTP Server" expression.

	<sendConsole 
		password="mypassword" 
		server="SERVER/ASI" 
		command="tell http quit" 
		taskRunningMessage="HTTP Server"/>

This task will launch the designer task, and wait for it to shutdown. It will loop while it found the "Designer" expression into the result of a "show task" command.

	<sendConsole
		password="mypassword"
		server="SERVER/ASI"
		command="load design -f names.nsf"
		taskRunningMessage="Designer"/>

This task will load the http task, and wait for it to start. It will loop while it does NOT found in the result of a "show task" command the expression given in the taskStartedMessage property.

	<sendConsole 
		password="mypassword"
		server="SERVER/ASI" 
		command="load http" 
		taskStartedMessage="HTTP Server[ ]*Listen for connect requests on TCP Port:"/>

Note that taskStartedMessage and taskRunningMessage are regular expressions.


### httpStop ###

Will stop the http task. Password is the password of your local ID file.

	<httpStop server="SERVER/ASI" password="mypassword"/>

### httpStart ###

Will start the http task. Password is the password of your local ID file.

	<httpStart server="SERVER/ASI" password="mypassword"/>

### databaseDelete ###

Will remove a database

	<databaseDelete server="SERVER/ASI" database="mydatabase.nsf" password="mypassword"/>

### databaseCopy ###

Will copy a database.

	<databaseCopy srcServer="" srcDatabase="mydb.ntf" destServer="SERVER/ASI" destDatabase="mydb.ntf" password="mypassword" templateCheck="mytmpl"/>

The "templatecheck" parameter will make the task check that the copied database is declaring the given template name (after copy). This will fail if the server already contains a database that declares the same template name. Yes, I love Domino... 

### databaseCreate ###

Will create a new empty database. The ACL will contain only one entry with default set to manager.

	<databaseCreate server="SERVER/ASI" database="newDb.nsf"/>

### signWithServerId ###

Will sign the given database with the server ID. This task will create an administration request, and wait for adminp to process it.

	<signWithServerId server="SERVER/ASI" database="mydatabase.nsf" password="mypassword"/>

### clearUpdateSiteDb ###

Deprecated. Use clearDb instead

This task will clear an update site database.

	<clearUpdateSiteDb server="SERVER/ASI" database="updateSite.nsf" password="mypassword"/>

### clearDb ###

This task will remove documents from a database.

	<clearDb server="SERVER/ASI" database="updateSite.nsf" password="mypassword" formula="Form = 'MyForm'/>

If no formula is specified, then all documents will be removed from the database.

### runAgent ###

This task will run an agent. It will make it run on a context document, on which you will be able to add fields using nested contextDocField tags.

	<runAgent server="SERVER/ASI" database="db.nsf" agent="myagent" password="mypassword">
		<contextDocField name="MyField" value="my value"/>
		<contextDocField name="MyField2" value="my other value"/>
	</runAgent>

This task can also work on a databaseSet :

	<runAgent server="SERVER/ASI" agent="myagent" password="mypassword">
		<contextDocField name="MyField" value="my value"/>
		<contextDocField name="MyField2" value="my other value"/>
		<databaseSet template="mytemplate"/>
	</runAgent>

### tellAmgrRun ###

This task will run a scheduled agent using a "tell amgr run" console command. It will NOT wait for the agent to finish.

	<tellAmgrRun
			server="SERVER/ASI"
			agent="dataMigration">
		<databaseSet template="mytemplate">
	</tellAmgrRun>

This will aunch the "dataMigration" agent on every database that relies on the "mytemplate" template.

### refreshDesign ###

This task will refresh the design of the given database. The refresh is done via the domino "designer" task. The task will end when it will detect that the domino "designer" task ends.

	<refreshDesign server="SERVER/ASI" database="mydb.nsf" password="mypassword"/>

This task also support nested <databaseSet> tags.

	<refreshDesign server="SERVER/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</refreshDesign>

### setOnBehalfOf ###

This task will make an agent run "on behalf of" a given user.

	<setOnBehalfOf server="SERVER/ASI" database="mydb.nsf" agent="myagent" onBehalfOf="CN=Lionel HERVIER/O=ASI" password="mypassword"/>

This task also support nested <databaseSet> tags.

	<setOnBehalfOf server="SERVER/ASI" agent="myagent" onBehalfOf="CN=Lionel HERVIER/O=ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</setOnBehalfOf>

### enableAgent ###

This task will allows you to enable an agent

	<enableAgent server="SERVER/ASI" database="mydb.nsf" agent="myagent" serverToRun="SERVER2/ASI" password="mypassword"/>

The "serverToRun" is the name of the server the agent will be activated on.

This task also support nested <databaseSet> tags.

	<enableAgent server="SERVER/ASI" agent="myagent" serverToRun="SERVER2/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</enableAgent>

### dxlExport ###

This task export a set of documents (selection based on a formula) to a dxl file.
The dxl file will be cleaned so that all information relative to the local database or local documents are removed: database replica id, document unid, last update date, last update author, etc...

	<dxlExport 
		password="mypassword" 
		server="SERVER/ASI" 
		database="mydb.nsf" 
		formula="Form = 'MyForm'" 
		toFile="export.dxl"/>

### dxlImport ###

This task will import documents stored in a dxl file.

	<dxlImport
		password="mypassword"
		server="SERVER/ASI"
		database="mydb.nsf"
		fromFile="file.dxl"/>

dxlImport can also work on a databaseSet. This task will import documents from the dxl file into every databases that rely on the template "mytemplate".

	<dxlImport password="mypassword" server="SERVER/ASI" fromFile="file.dxl">
		<databaseSet template="mytemplate"/>
	</dxlImport>

Using a condition inside the databaseSet allows you to import a document conditionnally :

	<dxlImport password="mypassword" server="SERVER/ASI" fromFile="param.dxl">
		<databaseSet template="mytemplate">
			<not>
				<documentExists formula="Form = 'Param'"/>
			</not>
		</databaseSet>
	</dxlImport>

### fieldExport ###

This task will only export a set of fields from a document

	<fieldExport
		password="mypassword"
		server="SERVER/ASI"
		database="mydb.nsf"
		formula="Form = 'param'"
		fields="Field1,Field2,Field3"
		toFile="fields.xml"/>

This will create a file named fields.xml that contains a subset of the DXL extract. All field types are supported (including rich text).

	<?xml version="1.0" encoding="UTF-8"?>
	<document xmlns="http://www.lotus.com/dxl">
  		<item name="Field1">
    		<text>value1</text>
  		</item>
		<item name="Field2">
			<text>value1</text>
			<text>value2</text>
		</item>
		<item name="Field3">
			<datetime>20161123T094516,00+01</datetime>
		</item>
	</document>

Note that the task will fail if the formula selects more than one document.

### fieldImport ###

This task will import a set of fields in a set of documents. This task can take databaseSets.

	<fieldImport
			password="mypassword"
			server="SERVER/ASI"
			formula="Form = 'MyForm'"
			fromFile="fields.xml">
		<databaseSet template="mytemplate"/>
	</fieldImport>

Every documents that are using the "MyForm" form in every databases that a using the "mytemplate" template will be updated with the fields defined in the fields.xml file.

If a field already exists in a document, it will be overwritten (including its type). If it does not exists, it will be created.

### clearProhibitDesignRefresh ###

This task allows you to remove the "Prohibit design refresh" flag on a set design elements.

	<clearProhibitDesignRefresh
			password="mypassword"
			server="SERVER/ASI"
			select="VIEWS"
			dryRun="false">
		<databaseSet template="mytemplate"/>
	</clearProhibitDesignRefresh>

This task will remove the flag from all the views that are present in any database that relies on the "mytemplate" template.

The dryRun parameter (defaults to false) allows you to run the task in "dry mode". It wont update design elements.

Possible values for the select property are :

	+------------------------+---------------------------+
	| ALL (default)          | All design elements       |
	| ACTIONS                | Actions                   |
	| AGENTS                 | Agents (LS and Java)      |
	| APPLETS                | Applets                   |
	| DATABASE_SCRIPTS       | Database scripts          |
	| COLUMNS                | Shared columns            |
	| DATA_CONNECTIONS       | Data connections          |
	| FILE_RESOURCE          | Files                     |
	| HIDDEN_FILE            | Hidden files (for xpages) |
	| CUSTOM_CONTROLS        | Custom controls           |
	| THEMES                 | Themes                    |
	| XPAGES                 | XPages                    |
	| FOLDERS                | Folders                   |
	| FORMS                  | Forms                     |
	| FRAMESETS              | Framesets                 |
	| NAVIGATORS             | Navigators                |
	| OUTLINES               | Outlines                  |
	| PAGES                  | Pages                     |
	| PROFILES               | Profiles documents        |
	| SCRIPT_LIBRARIES       | Script libraries          |
	| WEB_SERVICE_CONSUMERS  | Web service consumers     |
	| WEB_SERVICE_PROVIDERS  | Web service providers     |
	| SHARED_FIELDS          | Shared fields             |
	| SUBFORMS               | Subforms                  |
	| VIEWS                  | Views                     |
	| WIRING_PROPERTIES      | Wiring properties         |
	| COMPOSITE_APPLICATIONS | Composite applications    |
	| IMAGES                 | Image resources           |
	| STYLESHEETS            | Stylesheets               |
	| DB2_ACCESS_VIEWS       | DB2 access views          |
	| ICON                   | Icon document             |
	+------------------------+---------------------------+

### clearInheritTemplateFrom ###

This task will clear the name of the template that a design element depends on.

	<clearInheritTemplateFrom
			password="mypassword"
			server="SERVER/ASI"
			select="VIEWS"
			dryRun="false">
		<databaseSet template="mytemplate"/>
	</clearInheritTemplateFrom>

This taks will remove the name of the name of template that every view of every databases that depends on the "mytemplate" depends on.

The dryRun parameter (defaults to false) allows you to run the task in "dry mode". It wont update design elements.

For the "select" property, you can use the same list as the clearProhibitDesignRefresh task.

### checkXPageCompiled ###

This task will check that the given databases contains only XPages that have been compiled.

	<checkXPagesCompiled
			password="mypassword"
			server="SERVER/ASI"
		<databaseSet template="mytemplate"/>
	</checkXPagesCompiled>

# Additionnal Ant conditions #

When applicable, the conditions will support a databaseSet. But databaseSets can also support inner conditions.
Note that databaseSet declared inside a condition that is itself inside a databaseSet will be ignored. The condition will be forced to run on the current database, as defined in the databaseSet.

For example :

	<databaseDelete password="password">
		<databaseSet template="tmpl">
			<documentExists formula="Form = 'param'">
		</databaseSet>
	</databaseDelete>

If the "documentExists" condition defines a databaseSet (or a database/server attribute), they will be ignored as the condition will be ran on each database from the database set.

So the above script is equivalent to this one (which is non sens !!!)
	
	<databaseDelete password="password">
		<databaseSet template="tmpl">
			<documentExists formula="Form = 'param'" server="SERVER/ASI" database="mydb.nsf">
				<databaseSet template="othertmpl"/>
			</documentExists>
		</databaseSet>
	</databaseDelete>

The server/database attributes and the nested databaseSet will be ignored.

## documentExists ##

This condition check if the given database contains at least one document that match a given formula.

	<project name="test" basedir="." default="test" xmlns:if="ant:if">
		<property name="PASSWORD" value="mypassword"/>
		<property name="SERVER" value="SERVER/ASI"/>
		<property name="DATABASE" value="mydb.nsf"/>
		<target name="test">
			<condition property="param.exists">
				<documentExists 
						password="${PASSWORD}" 
						server="${SERVER}" 
						database="${DATABASE}" 
						formula="Form = 'param'"/>
			</condition>
			<dxlImport
					password="${PASSWORD}"
					server="${SERVER}"
					database="${DATABASE}"
					fromFile="param.dxl"
					if:set="document.exists"/>
		</target>
	</project>

This will create a default param document from the param.dxl file (file generated using the dxlExport task) only if such a document does not already exists in the database.

Note that to use "if:set", you will need ant 1.6 min.

The condition can also be inside a databaseSet tag.

	<dxlImport password="mypassword" server="SERVER/ASI" fromFile="params-prod.dxl">
		<databaseSet template="mytemplate">
			<not>
				<documentExists formula="Form = 'Param'"/>
			</not>
		</databaseSet>
	</dxlImport>

And the condition can contain a databaseSet tag :

	<condition property="param.exists.everywhere">
		<documentExists formula="Form = 'param'">
			<databaseSet template="tmpl"/>
		</documentExists>
	</condition>

This will check if every database on the server that are using the "tmpl" template defines at least one document that uses the "param" form. The property "param.exists.everywhere" will be defined is this is the case.

## fieldsExists ##

This is the same as documentExists, except that this task search for fields. It can also be used inside a databaseSet tag.

	<dxlFieldImport password="mypassword" server="SERVER/ASI" fromFile="X_API_KEY-prod.dxl">
		<databaseSet template="mytemplate">
			<not>
				<fieldsExists formula="Form = 'Param'" fields="X_API_KEY"/>
			</not>
		</databaseSet>
	</dxlFieldImport>

This will change the value of the X_API_KEY field into databases where it is not already defined.

## targetExists ##

OK, this task is not linked to the Domino world... It simply check if an external ant file contains a given target.

	<condition property="target.exists">
		<targetExists antFile="externalBuild.xml" target="myTarget"/>
	</condition>
	<ant 
		if:set="target.exists"
		antfile="externalBuild.xml"
		target="myTarget"/>

This script will launch the "myTarget" target from the "externalBuild.xml" ant file, only if the target exists.