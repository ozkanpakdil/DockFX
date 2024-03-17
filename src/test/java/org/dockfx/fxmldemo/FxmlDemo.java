package org.dockfx.fxmldemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FxmlDemo extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = fxmlLoader.load();
        stage.setTitle("FXML Welcome");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}
