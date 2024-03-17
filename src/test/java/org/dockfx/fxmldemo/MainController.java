package org.dockfx.fxmldemo;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPosition;

public class MainController {
    @FXML
    DockNode debugDockNode;
    @FXML
    DockPane dockPaneMain;
    @FXML
    DockNode mainNode;
    @FXML
    private TextArea console;
    private DockNode storedDockNode;

    @FXML
    public void initialize() {
        // could not find a way to set this from fxml , for now I am setting from here.
        mainNode.setDockPane(dockPaneMain);
        debugDockNode.setDockPane(dockPaneMain);
    }

    public void menuFileExit() {
        System.exit(0);
    }

    public void openDebugWindow() {
        console.setText("Opening debug window");
        if (debugDockNode != null && debugDockNode.isVisible()) {
            storedDockNode = debugDockNode;
            debugDockNode.setVisible(!debugDockNode.isVisible());
            dockPaneMain.undock(debugDockNode);
        } else if (storedDockNode != null) {
            debugDockNode = storedDockNode;
            debugDockNode.setVisible(true);
            dockPaneMain.dock(debugDockNode, DockPosition.BOTTOM);
            storedDockNode = null;
        }
    }

    public void someAction() {
        console.setText("Some action \n" + console.getText());
    }
}
