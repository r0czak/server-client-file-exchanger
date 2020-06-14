package client;

import java.io.*;
import java.net.Socket;

public class SocketClient {

  /*-------------------------------------------------*/
  // Socket client application

  private Socket clientSocket;
  public ObjectOutputStream ObjectOut;
  public ObjectInputStream ObjectIn;

  public void startConnection(String host_name, int port) {
    try {
      clientSocket = new Socket(host_name, port);

      ObjectOut = new ObjectOutputStream(clientSocket.getOutputStream());
      ObjectIn = new ObjectInputStream(clientSocket.getInputStream());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String sendString(String msg) {
    try {
      ObjectOut.writeObject(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public void SendMessage(Message msg) {
    try {
      ObjectOut.writeObject(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stopConnection() {
    try {
      ObjectIn.close();
      ObjectOut.close();
      clientSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
