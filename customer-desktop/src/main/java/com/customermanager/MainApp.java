package com.customermanager;

import com.customermanager.ui.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application entry point.
 * Run with:  mvn javafx:run
 *       or:  java -jar customer-desktop-1.0-SNAPSHOT.jar
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController root = new MainController(primaryStage);

        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("Customer Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(480);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
