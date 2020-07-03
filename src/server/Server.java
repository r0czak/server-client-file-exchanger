package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.stage.Stage;
import server.controllers.MainServerController;

/** Główna klasa, ktora ładuje plik fxml oraz uruchamia graficzny interfejs serwera. */
public class Server extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader =
        new FXMLLoader(this.getClass().getResource("/resources/mainScreenServer.fxml"));
    Accordion root = loader.load();
    Scene scene = new Scene(root);
    MainServerController controller = loader.getController();
    controller.setStage(primaryStage);
    controller.panesUpdater();

    primaryStage.setTitle("Server");
    primaryStage.setScene(scene);
    primaryStage.show();
    controller.close();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
