package server;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

public class Server {

  /*
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("mainScreenServer.fxml"));
    primaryStage.setTitle("Hello World");
    primaryStage.setScene(new Scene(root, 300, 275));
    primaryStage.show();
  }

   */

  /*-------------------------------------------------*/
  // Socket server application
  private ServerSocket serverSocket;

  public void initiate(int port) {
    try {
      serverSocket = new ServerSocket(port);
      while (true) {
        new ServerHandler(serverSocket.accept()).start();
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      stop();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private static class ServerHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ServerHandler(Socket socket) {
      this.clientSocket = socket;
    }

    @Override
    public void run() {
      try {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        ClientInfo Client1 = new ClientInfo();
        Client1.client_name = in.readLine();
        Client1.folder_path = Paths.get(in.readLine());
        System.out.println(Client1.client_name + " joined server");

        in.close();
        out.close();
        clientSocket.close();
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public static void main(String[] args) {
    Server server = new Server();
    server.initiate(6666);
  }
}
