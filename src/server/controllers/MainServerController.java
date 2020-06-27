package server.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import server.SocketServer;

import java.io.File;

public class MainServerController extends Thread {
  @FXML
  Accordion serverPanes;
  public SocketServer server;
  Stage stage;

  @FXML
  protected void initialize() {
    server = new SocketServer(6666);
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

  @FXML
  public void updatePanes() {
    for (int i : server.UserList.keySet()) {
      if (server.UserList.get(i).toDraw) {
        File folderPath = new File("/home/roczak/server/" + i);
        ListView<String> temp = new ListView<>();
        temp.getItems().addAll(folderPath.list());
        TitledPane newTitledPane = new TitledPane(server.UserList.get(i).ClientName, temp);
        server.UserList.get(i).toDraw = false;
        serverPanes.getPanes().add(newTitledPane);
        System.out.println("newPane");
      } else if (server.UserList.get(i).toRemove) {
        for (int j = 0; j < serverPanes.getPanes().size(); j++) {
          if (serverPanes.getPanes().get(j).getText().equals(server.UserList.get(i).ClientName)) {
            serverPanes.getPanes().remove(j);
            server.UserList.get(i).toRemove = false;
            System.out.println("paneRemoved");
            break;
          }
        }
      }
    }
  }

  @FXML
  public void updateFileList() {
    for (int i : server.UserList.keySet()) {
      if (server.UserList.get(i).toUpdate) {
        for (int j = 0; j < serverPanes.getPanes().size(); j++) {
          if (serverPanes.getPanes().get(j).getText().equals(server.UserList.get(i).ClientName)) {
            File folderPath = new File("/home/roczak/server/" + i);
            ListView<String> temp = new ListView<>();
            temp.getItems().addAll(folderPath.list());
            serverPanes.getPanes().get(j).setContent(temp);
          }
        }
        server.UserList.get(i).toUpdate = false;
      }
    }

  }

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
