package client.controllers;

import client.Message;
import client.SocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppController {
  private Path folderPath;
  @FXML
  private ListView<String> FileList, UserListView;
  @FXML
  private Button LogOutButton;
  public SocketClient client;
  public Message request;
  public AtomicBoolean terminate = new AtomicBoolean(false);

  @FXML
  protected void initialize() {
    request = new Message();
  }

  public void Update() {
    listFiles();
    Thread updater =
            new Thread(
                    () -> {
                      while (!terminate.get()) {
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

  public void FileExchange() {
    Thread t =
            new Thread(
                    () -> {
                      while (!terminate.get()) {
                        Platform.runLater(
                                () -> {
                                  try {
                                    client.SendFiles(request, folderPath);
                                    client.DownloadFiles(request, folderPath);
                                    listActiveUsers();
                                  } catch (IOException e) {
                                    e.printStackTrace();
                                  }
                                });
                        try {
                          Thread.sleep(5000);
                        } catch (InterruptedException e) {
                          e.printStackTrace();
                        }
                      }
                    });
    t.setDaemon(true);
    t.start();
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
  public synchronized void listActiveUsers() {
    List<String> UserList;
    UserList = client.downloadUserList(request);
    UserListView.getItems().setAll(UserList);
  }

  @FXML
  public void logOut() {
    request.Logout();
    client.SendMessage(request);
    request.clear();
    terminate.set(true);
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
