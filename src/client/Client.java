package client;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
    primaryStage.setTitle("Client");

    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  /*-------------------------------------------------*/
  // Socket client application

  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;

  public void startConnection(String host_name, int port) {
    try {
      clientSocket = new Socket(host_name, port);
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public String sendMessage(String msg) {
    try {
      out.println(msg);
    } catch (Exception e) {
      System.out.println(e);
    }
    return "";
  }

  public void stopConnection() {
    try {
      in.close();
      out.close();
      clientSocket.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static boolean CheckTimeRange(String TimeRange) {
    LocalTime TimeRangeTime = LocalTime.parse(TimeRange);
    LocalTime presentTime = LocalTime.now();

    return true;
  }

  public static void main(String[] args) {
    launch(args);
    Client client = new Client();

    Scanner input = new Scanner(System.in);
    String host_name;
    host_name = input.nextLine();

    client.startConnection("127.0.0.1", 6666);

    client.out.println(host_name);
    input.nextLine();

    client.stopConnection();
  }
}
