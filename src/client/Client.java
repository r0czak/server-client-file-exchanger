package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Główna klasa, ktora ładuje plik fxml oraz uruchamia graficzny interfejs klienta. */
public class Client extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader =
        new FXMLLoader(this.getClass().getResource("/resources/mainScreenClient.fxml"));
    StackPane root = loader.load();
    Scene scene = new Scene(root);
    primaryStage.setTitle("Client");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
