package server;

import client.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServer extends Thread {
  public ConcurrentHashMap<Integer, ClientInfo> UserList;
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
    UserList = new ConcurrentHashMap<>();
    try {
      socketServer = new ServerSocket(port);
      while (true) {
        new SocketServer.ServerHandler(socketServer.accept(), UserList).start();
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      stopServer();
    }
  }

  public static class ServerHandler extends Thread {
    private Socket clientSocket;
    public ConcurrentHashMap<Integer, ClientInfo> UserList;
    public ObjectOutputStream ObjectOut;
    public ObjectInputStream ObjectIn;
    public Message request;
    private int ClientId;

    public ServerHandler(Socket socket, ConcurrentHashMap<Integer, ClientInfo> UserList) {
      this.clientSocket = socket;
      this.UserList = UserList;
    }

    @Override
    public void run() {
      try {
        ObjectOut = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectIn = new ObjectInputStream(clientSocket.getInputStream());

        registerClient((String) ObjectIn.readObject(), Paths.get((String) ObjectIn.readObject()));

        System.out.println(UserList.get(ClientId).ClientName + " joined server");

        while (true) {
          request = (Message) ObjectIn.readObject();
          MessageHandler(request);
        }

      } catch (Exception e) {
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

    public void registerClient(String ClientName, Path FolderPath) {
      boolean existsFlag = false;
      for (int i : UserList.keySet()) {
        if (ClientName.equals(UserList.get(i).ClientName)) {
          this.ClientId = i;
          existsFlag = true;
          break;
        }
      }
      if (existsFlag) {
        UserList.get(ClientId).FolderPath = FolderPath;
      } else {
        Random generator = new Random();
        boolean IdExistsFlag = true;
        int newId = 0;

        while (IdExistsFlag) {
          newId = generator.nextInt(9000) + 1000;
          IdExistsFlag = false;
          for (int i : UserList.keySet()) {
            if (i == newId) {
              IdExistsFlag = true;
              break;
            }
          }
        }
        ClientInfo newClient = new ClientInfo(ClientName, FolderPath);
        UserList.put(newId, newClient);
        this.ClientId = newId;
      }
    }

    public void MessageHandler(Message request) {
      if (request.LogoutFlag) {
        System.out.println(UserList.get(ClientId).ClientName + " left server");
        stopConnection();
      } else if (request.SendFilesFlag) {
      } else if (request.DownloadFilesFlag) {
      } else if (request.DownloadClientListFlag) {
      } else if (request.TransferFileFlag) {
      }
    }
  }
}
