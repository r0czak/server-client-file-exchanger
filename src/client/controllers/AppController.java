package client.controllers;

import client.Client;
import client.Message;
import client.SocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppController {
  private Path folderPath;
  @FXML
  private ListView<String> FileList, UserListView;
  @FXML
  private Button LogOutButton, transferFileButton;
  public SocketClient client;
  public Message request;
  public AtomicBoolean terminate = new AtomicBoolean(false);
  public boolean transferFlag = false;
  public String transferClientname;
  public String transferFilename;

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
                                    transferFile();
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
    ListView<String> temp = new ListView<>();
    List<String> UserList;
    UserList = client.downloadUserList(request);
    temp.getItems().addAll(UserList);
    if (!UserListView.getItems().equals(temp.getItems())) {
      UserListView.getItems().setAll(UserList);
    }
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

  @FXML
  public void transferEvent() {
    FXMLLoader loader =
            new FXMLLoader(this.getClass().getResource("/resources/transferPopup.fxml"));
    try {
      Pane popupPane = loader.load();
      Scene scene = new Scene(popupPane);
      TransferController transferController = loader.getController();
      transferController.setFolderPath(folderPath);
      transferController.setUserListView(UserListView);
      transferController.setAppController(this);

      Stage transferStage = new Stage();
      transferStage.setTitle("Transfer file");
      transferStage.setScene(scene);
      transferStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void transferFile() {
    if (transferFlag) {
      System.out.println(transferClientname + transferFilename);
      client.TransferFile(request, transferFilename, transferClientname);
      transferFlag = false;
      transferClientname = "";
      transferFilename = "";
    }
  }

  public void setClient(SocketClient client) {
    this.client = client;
  }

  public void setFolderPath(Path folderPath) {
    this.folderPath = folderPath;
  }

  public void setTransferFlag(boolean transferFlag) {
    this.transferFlag = transferFlag;
  }

  public void setTransferClientname(String transferClientname) {
    this.transferClientname = transferClientname;
  }

  public void setTransferFilename(String transferFile) {
    this.transferFilename = transferFile;
  }
}
