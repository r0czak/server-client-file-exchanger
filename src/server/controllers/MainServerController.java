package server.controllers;

import javafx.fxml.FXML;
import server.SocketServer;

public class MainServerController extends Thread {
  public SocketServer server;

  @FXML
  protected void initialize() {
    server = new SocketServer(6666);
    Thread serverThread = new Thread(server);
    serverThread.start();
  }
}
