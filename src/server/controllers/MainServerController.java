package server.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import server.ClientInfo;
import server.SocketServer;

public class MainServerController extends Thread {
  public SocketServer server;
  Stage stage;

  @FXML
  protected void initialize() {
    server = new SocketServer(6666);
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

  @FXML
  public void UpdatePanes() {

  }

  @FXML
  public void listUsers() {
    for (int j : server.UserList.keySet()) {
      if (server.UserList.get(j).Active == true) {

      }
    }
  }

  @FXML
  public void close() {
    stage.setOnCloseRequest(e -> {
      server.stopServer();
      Platform.exit();
      System.exit(0);
    });
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
