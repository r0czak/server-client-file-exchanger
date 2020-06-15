package server;

import client.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServer extends Thread {
  public ConcurrentHashMap<Integer, ClientInfo> UserList;
  public HashMapHandler hashMapHandler;
  private ServerSocket socketServer;
  private int port;

  public class MessageHandlerReturn {
    public static final int STOP = 1;
    public static final int CONTINUE = 2;
  }

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
    hashMapHandler = new HashMapHandler();
    try {
      UserList = hashMapHandler.LoadHashMap();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    try {
      socketServer = new ServerSocket(port);
      while (true) {
        new SocketServer.ServerHandler(socketServer.accept(), UserList, hashMapHandler).start();
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
    public HashMapHandler hashMapHandler;
    public ObjectOutputStream ObjectOut;
    public ObjectInputStream ObjectIn;
    public Message request;
    private int ClientId;

    public ServerHandler(
            Socket socket,
            ConcurrentHashMap<Integer, ClientInfo> UserList,
            HashMapHandler hashMapHandler) {
      this.clientSocket = socket;
      this.UserList = UserList;
      this.hashMapHandler = hashMapHandler;
    }

    @Override
    public void run() {
      try {
        ObjectOut = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectIn = new ObjectInputStream(clientSocket.getInputStream());

        if (registerClient((String) ObjectIn.readObject(), (String) ObjectIn.readObject())) {
          ObjectOut.writeObject(true);
          System.out.println(UserList.get(ClientId).ClientName + " joined server");
        } else {
          ObjectOut.writeObject(false);
          stopConnection();
          return;
        }

        while (true) {
          request = (Message) ObjectIn.readObject();
          if (MessageHandler(request) == MessageHandlerReturn.STOP) {
            hashMapHandler.SaveHashMap(UserList);
            break;
          }
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

    public boolean registerClient(String ClientName, String FolderPath) {
      boolean existsFlag = false;
      boolean isActive = false;
      for (int i : UserList.keySet()) {
        if (ClientName.equals(UserList.get(i).ClientName)) {
          if (UserList.get(i).Active == true) {
            isActive = true;
          } else {
            this.ClientId = i;
            existsFlag = true;
          }
          break;
        }
      }

      if (isActive) {
        return false;
      }

      if (existsFlag) {
        UserList.get(ClientId).FolderPath = FolderPath;
        UserList.get(ClientId).Active = true;
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
        UserList.get(newId).Active = true;
        this.ClientId = newId;

        new File("/home/roczak/server/" + this.ClientId).mkdir();
      }
      return true;
    }

    public int MessageHandler(Message request) {
      if (request.LogoutFlag) {
        System.out.println(UserList.get(ClientId).ClientName + " left server");
        UserList.get(ClientId).Active = false;
        stopConnection();
        return MessageHandlerReturn.STOP;
      } else if (request.SendFilesFlag) {
        return MessageHandlerReturn.CONTINUE;
      } else if (request.DownloadFilesFlag) {
        return MessageHandlerReturn.CONTINUE;
      } else if (request.DownloadClientListFlag) {
        return MessageHandlerReturn.CONTINUE;
      } else if (request.TransferFileFlag) {
        return MessageHandlerReturn.CONTINUE;
      }
      return MessageHandlerReturn.CONTINUE;
    }
  }
}
