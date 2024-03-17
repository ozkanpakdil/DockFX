module dockfx {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.base;
    requires javafx.media;
    requires lombok;
    requires org.slf4j;

    exports org.dockfx;
    opens org.dockfx to javafx.fxml;
}