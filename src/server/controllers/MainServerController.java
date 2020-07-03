package server.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import server.SocketServer;

import java.io.File;

/** Klasa typu controller odpowiadająca za sterowanie graficznym interfejsem serwera */
public class MainServerController extends Thread {

  /** obiekt przetrzymujący podpanele z klientami */
  @FXML Accordion serverPanes;

  public SocketServer server;
  /**
   * ścieżka do folderu w którym będą zapisywane pliki klientów oraz hashmapa {@link
   * server.HashMapHandler}
   */
  private static String serverFolderPath = "/home/roczak/server/";
  /** główna scena aplikacji serwera */
  Stage stage;

  /** Funkcja inicjalizująca serwer na oddzielnym wątku */
  @FXML
  protected void initialize() {
    server = new SocketServer(6666);
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

  /** Funkcja dodająca i usuwająca podpanele w aplikacji serwera */
  @FXML
  public void updatePanes() {
    for (int i : server.userList.keySet()) {
      if (server.userList.get(i).toDraw) {
        /* Ustawienie flagi toDraw powoduje dodanie podpanelu z listą plików klienta */
        File folderPath = new File(serverFolderPath + i);
        ListView<String> temp = new ListView<>();
        temp.getItems().addAll(folderPath.list());
        TitledPane newTitledPane = new TitledPane(server.userList.get(i).clientName, temp);

        server.userList.get(i).toDraw = false;
        serverPanes.getPanes().add(newTitledPane);

      } else if (server.userList.get(i).toRemove) {
        /* Ustawienie flagi toRemove powoduje usunięcie podpanelu z okna aplikacji serwera */
        for (int j = 0; j < serverPanes.getPanes().size(); j++) {
          if (serverPanes.getPanes().get(j).getText().equals(server.userList.get(i).clientName)) {
            serverPanes.getPanes().remove(j);
            server.userList.get(i).toRemove = false;
            break;
          }
        }
      }
    }
  }

  /** Funkcja odpowiadająca za aktualizację listy plików dla każdego podpanelu */
  @FXML
  public void updateFileList() {
    for (int i : server.userList.keySet()) {
      if (server.userList.get(i).toUpdate) {
        /** Ustawienie flagi toUpdate powoduje aktualizację listy plików klienta */
        for (int j = 0; j < serverPanes.getPanes().size(); j++) {
          if (serverPanes.getPanes().get(j).getText().equals(server.userList.get(i).clientName)) {
            File folderPath = new File(serverFolderPath + i);
            ListView<String> temp = new ListView<>();
            temp.getItems().addAll(folderPath.list());
            serverPanes.getPanes().get(j).setContent(temp);
          }
        }
        server.userList.get(i).toUpdate = false;
      }
    }
  }

  /** Funkcja tworząca wątek odpowiedzialny za obsługę podpaneli */
  public void panesUpdater() {
    Thread updater =
        new Thread(
            () -> {
              while (true) {
                Platform.runLater(
                    () -> {
                      updatePanes();
                      updateFileList();
                    });
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
    updater.setDaemon(true);
    updater.start();
  }

  /** Funkcja odpowiadająca za wyłączenie serwera po wyłączeniu okna aplikacji */
  @FXML
  public void close() {
    stage.setOnCloseRequest(
        e -> {
          server.stopServer();
          Platform.exit();
          System.exit(0);
        });
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
