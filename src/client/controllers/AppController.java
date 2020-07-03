package client.controllers;

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
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Klasa typu kontroler odpowiedzialna za główne okno aplikacji klienckiej */
public class AppController {
  private Path folderPath;
  @FXML private ListView<String> fileList, userListView;
  @FXML private Button LogOutButton;
  public SocketClient client;
  public Message request;
  public boolean transferFlag =
      false; // Flaga informująca o próbie otworzenia okna do przesyłu plików między klientami
  public String transferClientname; // Nazwa klienta do którego przesyłamy plik
  public String transferFilename; // Nazwa pliku do przesłania

  @FXML
  protected void initialize() {
    request = new Message();
  }

  /** Funkcja tworząca wątek odpowiedzialny za aktualizację listy plików w folderze lokalnym */
  public void update() {
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

  /**
   * Funkcja tworząca wątek odpowiedzialny za komunikację z serwerem oraz za obserwowanie usunięcia
   * plików w folderze lokalnym
   */
  public void fileExchange() throws IOException {
    WatchService watchService = FileSystems.getDefault().newWatchService();
    WatchKey watchKey = folderPath.register(watchService, StandardWatchEventKinds.ENTRY_DELETE);

    Thread t =
        new Thread(
            () -> {
              while (true) {
                Platform.runLater(
                    () -> {
                      for (WatchEvent<?> event : watchKey.pollEvents()) {
                        String fileName = event.context().toString();
                        client.deleteFile(request, fileName);
                      }
                      try {
                        client.sendFiles(request, folderPath);
                        client.downloadFiles(request, folderPath);
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

  /** Funkcja sprawdzająca i aktualizująca listę plików w folderze lokalnym */
  @FXML
  public void listFiles() {
    ListView<String> temp = new ListView<>();
    temp.getItems().addAll(folderPath.toFile().list());
    if (!fileList.getItems().equals(temp.getItems())) {
      fileList.getItems().setAll(folderPath.toFile().list());
    }
  }

  /** Funkcja aktualizująca listę aktywnych klientów na serwerze */
  @FXML
  public synchronized void listActiveUsers() {
    ListView<String> temp = new ListView<>();
    List<String> UserList;
    UserList = client.downloadUserList(request);
    temp.getItems().addAll(UserList);
    if (!userListView.getItems().equals(temp.getItems())) {
      userListView.getItems().setAll(UserList);
    }
  }

  /** Wydarzenie odpowiedzialne za wylogowanie się z serwera i zamknięcie aplikacji klienckiej */
  @FXML
  public void logOut() {
    request.logout();
    client.sendRequest(request);
    request.clear();
    client.stopConnection();
    Stage stage = (Stage) LogOutButton.getScene().getWindow();
    stage.close();
  }

  /** Wydarzenie włączające okno do przesyłu plików między klientami */
  @FXML
  public void transferEvent() {
    FXMLLoader loader =
        new FXMLLoader(this.getClass().getResource("/resources/transferPopup.fxml"));
    try {
      Pane popupPane = loader.load();
      Scene scene = new Scene(popupPane);
      TransferController transferController = loader.getController();
      transferController.setFolderPath(folderPath);
      transferController.setUserListView(userListView);
      transferController.setAppController(this);

      Stage transferStage = new Stage();
      transferStage.setTitle("Transfer file");
      transferStage.setScene(scene);
      transferStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Funkcja wywołująca przesłanie plików między klientami */
  public void transferFile() {
    if (transferFlag) {
      client.transferFile(request, transferFilename, transferClientname);
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
