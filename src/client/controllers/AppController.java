package client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.nio.file.Path;

public class AppController {
  private Path folderPath;
  @FXML
  private ListView<String> FileList;

  public void main() {
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
                          System.out.println("kurwa");
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
    if (FileList.getItems().equals(temp.getItems()) == false) {
      FileList.getItems().setAll(folderPath.toFile().list());
    }
  }

  public void setFolderPath(Path folderPath) {
    this.folderPath = folderPath;
  }
}
