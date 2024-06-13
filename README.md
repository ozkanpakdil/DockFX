# how it looks 
[Local screen record](https://github.com/ozkanpakdil/DockFX/assets/604405/2a6e7c72-9fe8-45ed-a036-f99fbcae724f)

# requirements
Download the SDK from https://gluonhq.com/products/javafx/ and extract it to a folder, then add the path to the folder to the module path in the command below. in my case /home/oz-mint/tools/javafx-sdk-21.0.2/lib

# demo.dockFX local run command
I am leaving the all parameters below for linux below, change it accordingly to your system

```bash
/home/oz-mint/.sdkman/candidates/java/22.1.0.1.r17-gln/bin/java --module-path /home/oz-mint/tools/javafx-sdk-21.0.2/lib --add-modules=javafx.controls,javafx.web --add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED -javaagent:/home/oz-mint/.local/share/JetBrains/Toolbox/apps/intellij-idea-ultimate/lib/idea_rt.jar=42347:/home/oz-mint/.local/share/JetBrains/Toolbox/apps/intellij-idea-ultimate/bin -Dfile.encoding=UTF-8 -classpath /home/oz-mint/Downloads/DockFX/target/test-classes:/home/oz-mint/Downloads/DockFX/target/classes:/home/oz-mint/.m2/repository/org/openjfx/javafx-web/21/javafx-web-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-web/21/javafx-web-21-linux.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-media/21/javafx-media-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-media/21/javafx-media-21-linux.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-controls/21/javafx-controls-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-controls/21/javafx-controls-21-linux.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-graphics/21/javafx-graphics-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-graphics/21/javafx-graphics-21-linux.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-base/21/javafx-base-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-base/21/javafx-base-21-linux.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-fxml/21/javafx-fxml-21.jar:/home/oz-mint/.m2/repository/org/openjfx/javafx-fxml/21/javafx-fxml-21-linux.jar:/home/oz-mint/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.9.2/junit-jupiter-api-5.9.2.jar:/home/oz-mint/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/home/oz-mint/.m2/repository/org/junit/platform/junit-platform-commons/1.9.2/junit-platform-commons-1.9.2.jar:/home/oz-mint/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar:/home/oz-mint/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.9.2/junit-jupiter-engine-5.9.2.jar:/home/oz-mint/.m2/repository/org/junit/platform/junit-platform-engine/1.9.2/junit-platform-engine-1.9.2.jar:/home/oz-mint/.m2/repository/org/testfx/testfx-junit5/4.0.17/testfx-junit5-4.0.17.jar:/home/oz-mint/.m2/repository/org/testfx/testfx-core/4.0.17/testfx-core-4.0.17.jar:/home/oz-mint/.m2/repository/org/osgi/org.osgi.core/6.0.0/org.osgi.core-6.0.0.jar:/home/oz-mint/.m2/repository/org/hamcrest/hamcrest/2.1/hamcrest-2.1.jar:/home/oz-mint/.m2/repository/org/assertj/assertj-core/3.13.2/assertj-core-3.13.2.jar:/home/oz-mint/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.9.2/junit-jupiter-params-5.9.2.jar org.dockfx.demo.DockFX
```

I added run configuration and intellij files just for future, configuring javafx in local is complex.

# DockFX [![Build Status](https://goombert.visualstudio.com/DockFX/_apis/build/status/RobertBColton.DockFX?branchName=master)](https://goombert.visualstudio.com/DockFX/_build/latest?definitionId=1&branchName=master)
<table>
<tr>
<th><img alt="look 1" src="http://sites.psu.edu/robertbcolton/wp-content/uploads/sites/19608/2014/10/dockfxhover.png" ></th>
<th><img alt="look 2" src="http://sites.psu.edu/robertbcolton/wp-content/uploads/sites/19608/2014/10/dockfxdocked.png" ></th>
</tr>
</table>

## About
This library was created to fill the void for docking frameworks available in the JavaFX RIA platform. Its intention is to provide you with a fully featured docking library. This project and its source code is licensed under the [Mozilla Public License version 2](https://www.mozilla.org/en-US/MPL/2.0/) and you should feel free to make adaptations of this work. Please see the included LICENSE file for further details.

DockFX has a number of features:
* Full documentation
* Gratis and open source
* CSS and styling support

Features to be added in a to be determined future version:
* FXML support
* Scene builder integration
* DockBar support for floating toolbars
* Tab pane stacking of dock nodes with draggable headers
* A light docking library using no detachable windows

## Using the Library
add the following to your pom.xml
```xml
<dependency>
    <groupId>io.github.ozkanpakdil</groupId>
    <artifactId>dockfx</artifactId>
    <version>0.1.3</version>
</dependency>
```
or to your build.gradle
```groovy
implementation 'io.github.ozkanpakdil:dockfx:0.1.3'
```
## Example
[fxml example](./src/test/resources/main.fxml) and [fxml controller](./src/test/java/org/dockfx/fxmldemo/MainController.java) and [application class](./src/test/java/org/dockfx/fxmldemo/FxmlDemo.java) are provided in the test folder.

[java example](./src/test/java/org/dockfx/demo/DockFX.java) is provided in the test folder.

```java
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPosition;
//.....
DockPane dockPane = new DockPane();

DockNode tabsDock = new DockNode(tabs, "Tabs Dock", new ImageView(dockImage));
        tabsDock.

setPrefSize(300,100);
        tabsDock.

dock(dockPane, DockPosition.TOP);

DockNode tableDock = new DockNode(tableView);
// let's disable our table from being undocked
        tableDock.

setDockTitleBar(null);
        tableDock.

setPrefSize(300,100);
        tableDock.

dock(dockPane, DockPosition.BOTTOM);

```
check [here](./src/test/java/org/dockfx/demo/DockFX.java) for full example.
## Compiling from Source
The project was originally written in the Eclipse IDE but is also configured for Apache Maven. The project will continue to facilitate development with both command line tools and the Eclipse IDE. Default icons are included from the [Calico icon set](https://github.com/enigma-dev/Calico-Icon) for the dock indicators and title bar.

## Contributing
Adaptations of the project are welcome but you are encouraged to send fixes upstream to the master repository. I use the [Google Java style conventions](https://github.com/google/styleguide) which you can download an Eclipse plugin for. After importing the Eclipse formatter you can use CTRL+SHIFT+F to run the formatter on your code. It is requested that commits sent to this repository follow these conventions. Please see the following [link](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse) for instructions on configuring the Google style conventions with the Eclipse or IntelliJ IDE.
