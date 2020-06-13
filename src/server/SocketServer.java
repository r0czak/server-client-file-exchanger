package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class SocketServer extends Thread {
  private ServerSocket socketServer;
  private int port;


  public void stopServer() {
    try {
      socketServer.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public SocketServer(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      socketServer = new ServerSocket(port);
      while (true) {
        new SocketServer.ServerHandler(socketServer.accept()).start();
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      stopServer();
    }
  }

  public static class ServerHandler extends Thread {
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
}
