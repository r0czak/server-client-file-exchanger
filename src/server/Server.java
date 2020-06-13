package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.stage.Stage;
import server.controllers.MainServerController;

public class Server extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader =
            new FXMLLoader(this.getClass().getResource("/resources/mainScreenServer.fxml"));
    Accordion root = loader.load();
    Scene scene = new Scene(root);

    primaryStage.setTitle("Server");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
