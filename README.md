# qwickie #
This is an eclipse plugin for the Java Webframework Wicket from apache.org 

**qwickie needs the IDE for Java EE Developers package (html editor)**

## eclipse update site: ##

```
#!java

https://bitbucket.org/count_negative/qwickie/qwickie.updatesite/
```

Current version: **1.1.8**

## Usage: ##

Just mouse click on the wicket:id while pressing ctrl in the java code editor
![java.png](https://bitbucket.org/repo/69AR6M/images/3439330838-java.png)
![wicketcomponenthyperlink.png](https://bitbucket.org/repo/69AR6M/images/2293275585-wicketcomponenthyperlink.png)

to open the default html editor and mark the clicked wicket:id.

And - vice versa - mouse click on the wicket:id while pressing ctrl in the default html eclipse editor
![html.png](https://bitbucket.org/repo/69AR6M/images/1652911205-html.png)
![wicket_message.png](https://bitbucket.org/repo/69AR6M/images/1208440521-wicket_message.png)

Mouseover shows the line in html file
![hover.png](https://bitbucket.org/repo/69AR6M/images/746257561-hover.png)

Anyone having problems with mouseover on mac, please see http://code.google.com/p/qwickie/issues/detail?id=24&can=1


## Features: ##

  * Navigate from java code elements to the corresponding html element via wicket:id
  * Show the corresponding html fragment from the java code element
  * Show the wicket:id line in the html file on mouse over
  * Rename HTML and properties (with variations) when renaming a wicketized java file (supertype Component)
  * http://code.google.com/p/qwickie/issues/detail?id=8  (find html files in other locations)
  * http://code.google.com/p/qwickie/issues/detail?id=11 (jump to properties files)  
  * http://code.google.com/p/qwickie/issues/detail?id=21 (jump to xml files)
  * http://code.google.com/p/qwickie/issues/detail?id=22 (support for other wicket namespaces)
  * http://code.google.com/p/qwickie/issues/detail?id=17 (Wizard for new wicket pages)
  * http://code.google.com/p/qwickie/issues/detail?id=23 (Quickfix to add missing components to onInitialize)
  * http://code.google.com/p/qwickie/issues/detail?id=38 configure wicket id errors to be shown as error/warning/info.
  * http://code.google.com/p/qwickie/issues/detail?id=38 configure wicket id check exclude paths. This is a per project setting. You can define a set of paths (comma separated) where qwickie doesn't check your wicket ids.
  * Code assist for wicket:id in Wicket Java files. e.g. new Label("<press Ctrl-Space>")
![javaproposal.png](https://bitbucket.org/repo/69AR6M/images/1754825465-javaproposal.png)
  * Rename wicket:id and wicket id in a wicket component when renaming a property of a bean that is used as model. When a property with the same name is used in more than one bean, than it get's renamed too. Be careful when using this refactoring! It's just "like updating textual occurrences in comments and strings" and needs a preview.
  * http://code.google.com/p/qwickie/issues/detail?id=42&can=1 (control-option-1 to jump to the java file and control-option-2 to jump to the related HTML file.)
  * Fixed http://code.google.com/p/qwickie/issues/detail?id=34. Thanks to Willem Voogd!
  * Autosave feature when deactivating eclipse and refreshing the browser window. (so no ctrl-s is needed). Removed to a [http://code.google.com/p/eclatosa/ new project], because it's useful even not for wicket development.

There is a qwickie Nature available (now put in the project - configure menu), that checks if wicket:ids in html and java files are matching.
There where some errors reported and maintaining this thing is pretty time consuming so I decided to give you some settings.

![nature.png](https://bitbucket.org/repo/69AR6M/images/3852462915-nature.png)
![id_not_found.png](https://bitbucket.org/repo/69AR6M/images/3104525219-id_not_found.png)
![id_not_found2.png](https://bitbucket.org/repo/69AR6M/images/2961075506-id_not_found2.png)
![quickfix.png](https://bitbucket.org/repo/69AR6M/images/1416264087-quickfix.png)


The preferences page (Window - Preferences - Web - QWickie)

![project_settings.png](https://bitbucket.org/repo/69AR6M/images/1176741357-project_settings.png)