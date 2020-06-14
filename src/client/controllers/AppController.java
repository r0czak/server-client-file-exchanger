package client.controllers;

import client.Message;
import client.SocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.nio.file.Path;

public class AppController {
  private Path folderPath;
  @FXML
  private ListView<String> FileList;
  @FXML
  private Button LogOutButton;
  public SocketClient client;
  public Message request;

  public void main() {
    request = new Message(1);
    listFiles();
    Thread updater =
            new Thread(
                    () -> {
                      while (true) {
                        Platform.runLater(
                                () -> {
                                  listFiles();
                                });
                        try {
                          Thread.sleep(1000);
                        } catch (InterruptedException e) {
                          e.printStackTrace();
                          break;
                        }
                      }
                    });
    updater.setDaemon(true);
    updater.start();
  }

  @FXML
  public void listFiles() {
    ListView<String> temp = new ListView<>();
    temp.getItems().addAll(folderPath.toFile().list());
    if (!FileList.getItems().equals(temp.getItems())) {
      FileList.getItems().setAll(folderPath.toFile().list());
    }
  }

  @FXML
  public void logOut() {
    request.Logout();
    client.SendMessage(request);
    request.clear();
    client.stopConnection();
    Stage stage = (Stage) LogOutButton.getScene().getWindow();
    stage.close();
  }

  public void setClient(SocketClient client) {
    this.client = client;
  }

  public void setFolderPath(Path folderPath) {
    this.folderPath = folderPath;
  }
}
