We've set things up so that it just works out of the box with Eclipse and Ant. 


ECLIPSE:
We've provided .project and .classpath files, which you can directly import into eclipse. To do this, select File -> Import.  In the dialog box which pops up, expand the folder called "General" and choose the option "Existing Projects into Workspace".  Finally, set the root directory to be the java directory within the original project directory: pa4-ner/java.  

To set up a run configuration for NER, right-click NER.java in the project explorer and select "Run As" -> "Run Configuration". Under the Main tab, type "NER" for project and "NER" for main class. Then, proceed to the arguments tab and type "../data/train ../data/dev -print" for program arguments and "-Xmx1G" for VM arguments without the quotations. Then, click "Apply" and "Run" buttons. This will run your program.

To set up a run configuration for Submit, right-click on Submit.java and follow the above steps but use "Submit" for main class, no arguments for program arguments, and "-Xmx1G" for VM arguments. Enter your e-mail and password in the Eclipse Console when prompted. 


ANT:
we've provided a basic build.xml file for use with ant.  Just call:

$ ant compile
$ ant run
$ ant submit


COMMAND LINE:
If you want to develop on the command line, use the following commands to build and run your code from the pa1-spamlord/java directory:

$ mkdir classes
$ javac *.java org/json/*.java -d classes
$ java -cp classes -Xmx1G NER ../data/train ../data/dev -print
$ java -cp classes -Xmx1G Submit
