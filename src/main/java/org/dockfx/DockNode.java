/**
 * @file DockNode.java
 * @brief Class implementing basic dock node with floating and styling.
 * @section License
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 **/

package org.dockfx;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Objects;

/**
 * Base class for a dock node that provides the layout of the content along with a title bar and a
 * styled border. The dock node can be detached and floated or closed and removed from the layout.
 * Dragging behavior is implemented through the title bar.
 *
 * @since DockFX 0.1
 */
public class DockNode extends VBox implements EventHandler<MouseEvent> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockNode.class);
    /**
     * CSS pseudo class selector representing whether this node is currently floating.
     */
    private static final PseudoClass FLOATING_PSEUDO_CLASS = PseudoClass.getPseudoClass("floating");
    /**
     * CSS pseudo class selector representing whether this node is currently docked.
     */
    private static final PseudoClass DOCKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("docked");
    /**
     * CSS pseudo class selector representing whether this node is currently maximized.
     */
    private static final PseudoClass MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");
    /**
     * The style this dock node should use on its stage when set to floating.
     * -- SETTER --
     * The stage style that will be used when the dock node is floating. This must be set prior to
     * setting the dock node to floating.
     */
    private StageStyle stageStyle = StageStyle.TRANSPARENT;
    /**
     * The stage that this dock node is currently using when floating.
     */
    private Stage stage;
    /**
     * The contents of the dock node, i.e. a TreeView or ListView.
     */
    private Node contents;
    /**
     * The title bar that implements our dragging and state manipulation.
     */
    private DockTitleBar dockTitleBar;
    /**
     * The border pane used when floating to provide a styled custom border.
     */
    private BorderPane borderPane;
    /**
     * The dock pane this dock node belongs to when not floating.
     */
    private DockPane dockPane;

    /**
     * Boolean property maintaining whether this node is currently maximized.
     * {@code @defaultValue} false
     */
    private final BooleanProperty maximizedProperty = new SimpleBooleanProperty(false) {

        @Override
        protected void invalidated() {
            DockNode.this.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
            if (borderPane != null) {
                borderPane.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
            }

            Stage rootWindow = stage;
            while (rootWindow.getOwner() != null) {
                rootWindow = (Stage) rootWindow.getOwner();
            }
            if (this.get()) {
                stage.setX(rootWindow.getX());
                stage.setY(rootWindow.getY());
                stage.setWidth(rootWindow.getWidth());
                stage.setHeight(rootWindow.getHeight());
            }

            stage.setMaximized(get());

            // TODO: This is a work around to fill the screen bounds and not overlap the task bar when
            // the window is undecorated as in Visual Studio. A similar work around needs applied for
            // JFrame in Swing. http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4737788
            // Bug report filed:
            // https://bugs.openjdk.java.net/browse/JDK-8133330
            if (this.get()) {
                Screen screen = Screen
                        .getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                        .get(0);
                Rectangle2D bounds = screen.getVisualBounds();

                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());

                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
            }
        }

        @Override
        public String getName() {
            return "maximized";
        }
    };
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<>(new ImageView(new Image(Objects.requireNonNull(getClass().getResource("docknode.png")).toExternalForm()))) {
        @Override
        public String getName() {
            return "graphic";
        }
    };
    private final StringProperty titleProperty = new SimpleStringProperty("Dock") {
        @Override
        public String getName() {
            return "title";
        }
    };
    private final BooleanProperty customTitleBarProperty = new SimpleBooleanProperty(true) {
        @Override
        public String getName() {
            return "customTitleBar";
        }
    };
    private final BooleanProperty floatingProperty = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            DockNode.this.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
            if (borderPane != null) {
                borderPane.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
            }
        }

        @Override
        public String getName() {
            return "floating";
        }
    };
    private final BooleanProperty initializedProperty = new SimpleBooleanProperty(false) {
        @Override
        public String getName() {
            return "initialized";
        }
    };
    private final BooleanProperty floatableProperty = new SimpleBooleanProperty(true) {
        @Override
        public String getName() {
            return "floatable";
        }
    };
    private final BooleanProperty closableProperty = new SimpleBooleanProperty(true) {
        @Override
        public String getName() {
            return "closable";
        }
    };
    private final BooleanProperty stageResizableProperty = new SimpleBooleanProperty(true) {
        @Override
        public String getName() {
            return "resizable";
        }
    };
    private final BooleanProperty dockedProperty = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (getDockTitleBar() != null) {
                    getDockTitleBar().setVisible(true);
                    getDockTitleBar().setManaged(true);
                }
            }

            DockNode.this.pseudoClassStateChanged(DOCKED_PSEUDO_CLASS, get());
        }

        @Override
        public String getName() {
            return "docked";
        }
    };
    /**
     * The last position of the mouse that was within the minimum layout bounds.
     */
    private Point2D sizeLast;
    /**
     * Whether we are currently resizing in a given direction.
     */
    private boolean sizeWest = false, sizeEast = false, sizeNorth = false, sizeSouth = false;

    /**
     * The position of the dock node relative to the dock pane.
     */
    private DockPosition dockPosition;

    /**
     * Whether the node is currently floating.
     *
     * @param floating    Whether the node is currently floating.
     * @param translation null The offset of the node after being set floating. Used for aligning it
     *                    with its layout bounds inside the dock pane when it becomes detached. Can be null
     *                    indicating no translation.
     */
    public void setFloating(boolean floating, Point2D translation) {
        if (floating && !this.isFloating()) {
            // position the new stage relative to the old scene offset
            Point2D floatScene = this.localToScene(0, 0);
            Point2D floatScreen = this.localToScreen(0, 0);

            // setup window stage
            getDockTitleBar().setVisible(this.isCustomTitleBar());
            getDockTitleBar().setManaged(this.isCustomTitleBar());

            // apply the floating property so we can get its padding size
            // while it is floating to offset it by the drop shadow
            // this way it pops out above exactly where it was when docked
            this.floatingProperty.set(floating);
            this.applyCss();

            if (this.isDocked()) {
                this.undock();
            }

            stage = new Stage();
            stage.titleProperty().bind(titleProperty);
            if (dockPane != null && dockPane.getScene() != null && dockPane.getScene().getWindow() != null) {
                stage.initOwner(dockPane.getScene().getWindow());
            }
            stage.initStyle(stageStyle);

            // offset the new stage to cover exactly the area the dock was local to the scene
            // this is useful for when the user presses the + sign and we have no information
            // on where the mouse was clicked
            Point2D stagePosition;
            if (this.isDecorated()) {
                Window owner = stage.getOwner();
                stagePosition = floatScene.add(new Point2D(owner.getX(), owner.getY()));
            } else {
                stagePosition = floatScreen;
            }
            if (translation != null) {
                stagePosition = stagePosition.add(translation);
            }

            // the border pane allows the dock node to have a drop shadow effect on the border
            // but also maintain the layout of contents such as a tab that has no content
            borderPane = new BorderPane();
            borderPane.getStyleClass().add("dock-node-border");
            borderPane.setCenter(this);

            Scene scene = new Scene(borderPane);

            // apply the border pane css so that we can get the insets and position the stage properly
            dockPane.initializeDefaultUserAgentStylesheet();
            Platform.runLater(() -> {
                if (scene != null && !scene.getStylesheets().contains(dockPane.getDefaultUserAgentStylesheet()))
                    scene.getStylesheets().add(dockPane.getDefaultUserAgentStylesheet());
                borderPane.applyCss();
            });
            Insets insetsDelta = borderPane.getInsets();

            double insetsWidth = insetsDelta.getLeft() + insetsDelta.getRight();
            double insetsHeight = insetsDelta.getTop() + insetsDelta.getBottom();

            stage.setX(stagePosition.getX() - insetsDelta.getLeft());
            stage.setY(stagePosition.getY() - insetsDelta.getTop());

            stage.setMinWidth(borderPane.minWidth(this.getHeight()) + insetsWidth);
            stage.setMinHeight(borderPane.minHeight(this.getWidth()) + insetsHeight);

            borderPane.setPrefSize(this.getWidth() + insetsWidth, this.getHeight() + insetsHeight);

            stage.setScene(scene);

            if (stageStyle == StageStyle.TRANSPARENT) {
                scene.setFill(null);
            }

            stage.setResizable(this.isStageResizable());
            if (this.isStageResizable()) {
                stage.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
                stage.addEventFilter(MouseEvent.MOUSE_MOVED, this);
                stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
            }

            // we want to set the client area size
            // without this it subtracts the native border sizes from the scene
            // size
            stage.sizeToScene();

            stage.show();
        } else if (!floating && this.isFloating()) {
            this.floatingProperty.set(floating);

            stage.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
            stage.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
            stage.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);

            stage.close();
        }
    }

    /**
     * The dock pane that was last associated with this dock node. Either the dock pane that it is
     * currently docked to or the one it was detached from. Can be null if the node was never docked.
     *
     * @return The dock pane that was last associated with this dock node.
     */
    public final DockPane getDockPane() {
        return dockPane;
    }

    /**
     * The dock title bar associated with this dock node.
     *
     * @return The dock title bar associated with this node.
     */
    public final DockTitleBar getDockTitleBar() {
        return this.dockTitleBar;
    }

    /**
     * Changes the title bar in the layout of this dock node. This can be used to remove the dock
     * title bar from the dock node by passing null.
     *
     * @param dockTitleBar null The new title bar of this dock node, can be set null indicating no
     *                     title bar is used.
     */
    public void setDockTitleBar(DockTitleBar dockTitleBar) {
        if (dockTitleBar != null) {
            if (getDockTitleBar() != null) {
                this.getChildren().set(this.getChildren().indexOf(getDockTitleBar()), dockTitleBar);
            } else {
                this.getChildren().add(0, dockTitleBar);
            }
        } else {
            this.getChildren().remove(getDockTitleBar());
        }

        this.dockTitleBar = dockTitleBar;
    }

    /**
     * The stage associated with this dock node. Can be null if the dock node was never set to
     * floating.
     *
     * @return The stage associated with this node.
     */
    public final Stage getStage() {
        return stage;
    }

    /**
     * The border pane used to parent this dock node when floating. Can be null if the dock node was
     * never set to floating.
     *
     * @return The stage associated with this node.
     */
    public final BorderPane getBorderPane() {
        return borderPane;
    }

    /**
     * The contents managed by this dock node.
     *
     * @return The contents managed by this dock node.
     */
    public final Node getContents() {
        return contents;
    }

    /**
     * Changes the contents of the dock node.
     *
     * @param contents The new contents of this dock node.
     */
    public void setContents(Node contents) {
        if (getChildren() != null && getChildren().indexOf(this.contents) > 0)
            getChildren().set(getChildren().indexOf(this.contents), contents);
        this.contents = contents;
        initMe();
    }

    /**
     * Object property maintaining bidirectional state of the caption graphic for this node with the
     * dock title bar or stage.
     * <p>
     * {@code @defaultValue} null
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }

    public final Node getGraphic() {
        return graphicProperty.get();
    }

    public final void setGraphic(Node graphic) {
        this.graphicProperty.setValue(graphic);
        initMe();
    }

    /**
     * Boolean property maintaining bidirectional state of the caption title for this node with the
     * dock title bar or stage.
     *
     * @defaultValue "Dock"
     */
    public final StringProperty titleProperty() {
        return titleProperty;
    }

    public final String getTitle() {
        return titleProperty.get();
    }

    public final void setTitle(String title) {
        this.titleProperty.setValue(title);
        initMe();
    }

    /**
     * Boolean property maintaining whether this node is currently using a custom title bar. This can
     * be used to force the default title bar to show when the dock node is set to floating instead of
     * using native window borders.
     *
     * @defaultValue true
     */
    public final BooleanProperty customTitleBarProperty() {
        return customTitleBarProperty;
    }

    public final boolean isCustomTitleBar() {
        return customTitleBarProperty.get();
    }

    public final void setUseCustomTitleBar(boolean useCustomTitleBar) {
        if (this.isFloating()) {
            getDockTitleBar().setVisible(useCustomTitleBar);
            getDockTitleBar().setManaged(useCustomTitleBar);
        }
        this.customTitleBarProperty.set(useCustomTitleBar);
    }

    /**
     * Boolean property maintaining whether this node is currently floating.
     *
     * @defaultValue false
     */
    public final BooleanProperty floatingProperty() {
        return floatingProperty;
    }

    public final boolean isFloating() {
        return floatingProperty.get();
    }

    /**
     * Whether the node is currently floating.
     *
     * @param floating Whether the node is currently floating.
     */
    public void setFloating(boolean floating) {
        setFloating(floating, null);
    }

    /**
     * Boolean property maintaining whether this node is currently floatable.
     * <p>
     * {@code @defaultValue} true
     */
    public final BooleanProperty floatableProperty() {
        return floatableProperty;
    }

    public final boolean isFloatable() {
        return floatableProperty.get();
    }

    public final void setFloatable(boolean floatable) {
        if (!floatable && this.isFloating()) {
            this.setFloating(false);
        }
        this.floatableProperty.set(floatable);
    }

    /**
     * Boolean property maintaining whether this node is currently closable.
     *
     * @defaultValue true
     */
    public final BooleanProperty closableProperty() {
        return closableProperty;
    }

    public final boolean isClosable() {
        return closableProperty.get();
    }

    public final void setClosable(boolean closable) {
        this.closableProperty.set(closable);
    }

    /**
     * Boolean property maintaining whether this node is currently resizable.
     *
     * @defaultValue true
     */
    public final BooleanProperty resizableProperty() {
        return stageResizableProperty;
    }

    public final boolean isStageResizable() {
        return stageResizableProperty.get();
    }

    public final void setStageResizable(boolean resizable) {
        stageResizableProperty.set(resizable);
    }

    /**
     * Boolean property maintaining whether this node is currently docked. This is used by the dock
     * pane to inform the dock node whether it is currently docked.
     *
     * @defaultValue false
     */
    public final BooleanProperty dockedProperty() {
        return dockedProperty;
    }

    public final boolean isDocked() {
        return dockedProperty.get();
    }

    public final BooleanProperty maximizedProperty() {
        return maximizedProperty;
    }

    public final boolean isMaximized() {
        return maximizedProperty.get();
    }

    /**
     * Whether the node is currently maximized.
     *
     * @param maximized Whether the node is currently maximized.
     */
    public final void setMaximized(boolean maximized) {
        maximizedProperty.set(maximized);
    }

    public final boolean isDecorated() {
        return stageStyle != StageStyle.TRANSPARENT && stageStyle != StageStyle.UNDECORATED;
    }

    /**
     * Dock this node into a dock pane.
     *
     * @param dockPane     The dock pane to dock this node into.
     * @param dockPosition The docking position relative to the sibling of the dock pane.
     * @param sibling      The sibling node to dock this node relative to.
     */
    void dock(DockPane dockPane, DockPosition dockPosition, Node sibling) {
        setDockPane(dockPane);
        setDockPosition(dockPosition);
        dockPane.dock(this, getDockPosition(), sibling);
    }

    public DockPosition getDockPosition() {
        return dockPosition;
    }

    /**
     * Dock this node into a dock pane.
     *
     * @param dockPane     The dock pane to dock this node into.
     * @param dockPosition The docking position relative to the sibling of the dock pane.
     */
    void dock(DockPane dockPane, DockPosition dockPosition) {
        dockPane.dock(this, getDockPosition());
    }

    public void setDockPosition(DockPosition dockPos) {
        this.dockPosition = dockPos;
        initMe();
    }

    public void setDockPane(DockPane dockPane) {
        if (isFloating()) {
            setFloating(false);
        }
        this.dockPane = dockPane;
        this.dockedProperty.set(true);
        initMe();
    }

    private void initMe() {
        if (!initializedProperty.get()
                && dockPane != null
                && dockPosition != null
                && contents != null) {
            initializedProperty.set(true);
            initializeDockNode(contents, titleProperty.get(), graphicProperty.get());
        }
    }

    /**
     * Detach this node from its previous dock pane if it was previously docked.
     */
    public void undock() {
        if (dockPane != null) {
            dockPane.undock(this);
        }
        this.dockedProperty.set(false);
    }

    /**
     * Close this dock node by setting it to not floating and making sure it is detached from any dock
     * pane.
     */
    public void close() {
        if (isFloating()) {
            setFloating(false);
        } else if (isDocked()) {
            undock();
        }
    }

    /**
     * Gets whether the mouse is currently in this dock node's resize zone.
     *
     * @return Whether the mouse is currently in this dock node's resize zone.
     */
    public boolean isMouseResizeZone() {
        return sizeWest || sizeEast || sizeNorth || sizeSouth;
    }

    @Override
    public void handle(MouseEvent event) {
        Cursor cursor = Cursor.DEFAULT;

        // TODO: use escape to cancel resize/drag operation like visual studio
        if (!this.isFloating() || !this.isStageResizable()) {
            return;
        }

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            sizeLast = new Point2D(event.getScreenX(), event.getScreenY());
        } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
            Insets insets = borderPane.getPadding();

            sizeWest = event.getX() < insets.getLeft();
            sizeEast = event.getX() > borderPane.getWidth() - insets.getRight();
            sizeNorth = event.getY() < insets.getTop();
            sizeSouth = event.getY() > borderPane.getHeight() - insets.getBottom();

            if (sizeWest) {
                if (sizeNorth) {
                    cursor = Cursor.NW_RESIZE;
                } else if (sizeSouth) {
                    cursor = Cursor.SW_RESIZE;
                } else {
                    cursor = Cursor.W_RESIZE;
                }
            } else if (sizeEast) {
                if (sizeNorth) {
                    cursor = Cursor.NE_RESIZE;
                } else if (sizeSouth) {
                    cursor = Cursor.SE_RESIZE;
                } else {
                    cursor = Cursor.E_RESIZE;
                }
            } else if (sizeNorth) {
                cursor = Cursor.N_RESIZE;
            } else if (sizeSouth) {
                cursor = Cursor.S_RESIZE;
            }

            this.getScene().setCursor(cursor);
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && this.isMouseResizeZone()) {
            Point2D sizeCurrent = new Point2D(event.getScreenX(), event.getScreenY());
            Point2D sizeDelta = sizeCurrent.subtract(sizeLast);

            double newX = stage.getX(), newY = stage.getY(), newWidth = stage.getWidth(),
                    newHeight = stage.getHeight();

            if (sizeNorth) {
                newHeight -= sizeDelta.getY();
                newY += sizeDelta.getY();
            } else if (sizeSouth) {
                newHeight += sizeDelta.getY();
            }

            if (sizeWest) {
                newWidth -= sizeDelta.getX();
                newX += sizeDelta.getX();
            } else if (sizeEast) {
                newWidth += sizeDelta.getX();
            }

            // TODO: find a way to do this synchronously and eliminate the flickering of moving the stage
            // around, also file a bug report for this feature if a work around can not be found this
            // primarily occurs when dragging north/west but it also appears in native windows and Visual
            // Studio, so not that big of a concern.
            // Bug report filed:
            // https://bugs.openjdk.java.net/browse/JDK-8133332
            double currentX = sizeLast.getX(), currentY = sizeLast.getY();
            if (newWidth >= stage.getMinWidth()) {
                stage.setX(newX);
                stage.setWidth(newWidth);
                currentX = sizeCurrent.getX();
            }

            if (newHeight >= stage.getMinHeight()) {
                stage.setY(newY);
                stage.setHeight(newHeight);
                currentY = sizeCurrent.getY();
            }
            sizeLast = new Point2D(currentX, currentY);
            // we do not want the title bar getting these events
            // while we are actively resizing
            if (sizeNorth || sizeSouth || sizeWest || sizeEast) {
                event.consume();
            }
        }
    }

    /**
     * Sets DockNodes contents, title and title bar graphic
     *
     * @param contents The contents of the dock node which may be a tree or another scene graph node.
     * @param title    The caption title of this dock node which maintains bidirectional state with the
     *                 title bar and stage.
     * @param graphic  The caption title of this dock node which maintains bidirectional state with the
     *                 title bar and stage.
     */
    private void initializeDockNode(Node contents, String title, Node graphic) {
        if (contents == null) {
            log.warn("contents is null, can not draw without contents");
            return;
        }
        if (dockPosition == null) {
            log.warn("dockPosition is null, can not draw without dockPosition");
            return;
        }
        if (dockPane == null) {
            log.warn("dockPane is null, can not draw without dockPane");
            return;
        }
        if (getDockTitleBar() == null) {
            log.warn("dockTitleBar is null, this can not be moved title:{}", titleProperty);
        }
        this.titleProperty.setValue(title);
        this.graphicProperty.setValue(graphic);
        this.contents = contents;

        if (!"Dock".equals(title)) {
            dockTitleBar = new DockTitleBar(this);
        } else {
            log.warn("title is default value, not creating new title bar. this is the main central window,{}", contents);
        }
        if (getDockTitleBar() != null)
            getChildren().addAll(getDockTitleBar(), contents);
        else
            getChildren().add(contents);
        VBox.setVgrow(contents, Priority.ALWAYS);

        this.getStyleClass().add("dock-node");
        dock(dockPane, dockPosition);
        dockPane.initializeDefaultUserAgentStylesheet();
    }

    /**
     * Loads Node from fxml file located at FXMLPath and returns it.
     *
     * @param FXMLPath Path to fxml file.
     * @return Node loaded from fxml file or StackPane with Label with error message.
     */
    private FXMLLoader loadNode(String FXMLPath) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.load(DockNode.class.getResourceAsStream(FXMLPath));
        } catch (Exception e) {
            e.printStackTrace();
            loader.setRoot(new StackPane(new Label("Could not load FXML file")));
        }
        initMe();
        return loader;
    }
}
