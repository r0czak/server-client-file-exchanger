package server;

import client.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
      hashMapHandler.SaveHashMap(UserList, "/home/roczak/server/userlist.ser");
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
      UserList = hashMapHandler.LoadHashMap("/home/roczak/server/userlist.ser");
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
            System.out.println(UserList.get(ClientId).ClientName + " left server");
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
        UserList.get(ClientId).toDraw = true;
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
        UserList.get(newId).toDraw = true;
        this.ClientId = newId;

        new File("/home/roczak/server/" + this.ClientId).mkdir();
      }

      return true;
    }

    public int MessageHandler(Message request) throws IOException {
      if (request.LogoutFlag) {
        UserList.get(ClientId).Active = false;
        UserList.get(ClientId).toRemove = true;
        stopConnection();
        return MessageHandlerReturn.STOP;

      } else if (request.UploadFilesFlag) {
        String[] ClientFileArr = null, ServerFileArr;
        File temp = new File("/home/roczak/server/" + ClientId);
        ServerFileArr = temp.list();
        try {
          ClientFileArr = (String[]) ObjectIn.readObject();
        } catch (IOException e) {
          System.out.println(UserList.get(ClientId).ClientName + " left server");
          UserList.get(ClientId).Active = false;
          UserList.get(ClientId).toUpdate = false;
          UserList.get(ClientId).toRemove = true;
          stopConnection();
          return MessageHandlerReturn.STOP;
          // e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }

        List<String> TempServerFileList = Arrays.asList(ServerFileArr);
        List<String> TempClientFileList = Arrays.asList(ClientFileArr);
        List<String> ServerFileList = new ArrayList<>(TempServerFileList);
        List<String> ClientFileList = new ArrayList<>(TempClientFileList);
        ServerFileList.retainAll(ClientFileList);
        ClientFileList.removeAll(ServerFileList);
        ClientFileArr = ClientFileList.stream().toArray(String[]::new);
        try {
          ObjectOut.writeObject(ClientFileArr);
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (ClientFileArr.length != 0) {
          for (int i = 0; i < ClientFileArr.length; i++) {
            byte[] byteArray = new byte[1024];
            FileOutputStream fos =
                    new FileOutputStream("/home/roczak/server/" + ClientId + "/" + ClientFileArr[i]);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = ObjectIn.read(byteArray, 0, byteArray.length);
            bos.write(byteArray, 0, bytesRead);
            bos.flush();
            bos.close();
          }
          UserList.get(ClientId).toUpdate = true;
        }
        return MessageHandlerReturn.CONTINUE;

      } else if (request.DownloadFilesFlag) {
        String[] ClientFileArr = null, ServerFileArr;
        File temp = new File("/home/roczak/server/" + ClientId);
        ServerFileArr = temp.list();
        try {
          ClientFileArr = (String[]) ObjectIn.readObject();
        } catch (IOException e) {
          System.out.println(UserList.get(ClientId).ClientName + " left server");
          UserList.get(ClientId).Active = false;
          UserList.get(ClientId).toUpdate = false;
          UserList.get(ClientId).toRemove = true;
          stopConnection();
          return MessageHandlerReturn.STOP;
          // e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }

        List<String> TempServerFileList = Arrays.asList(ServerFileArr);
        List<String> TempClientFileList = Arrays.asList(ClientFileArr);
        List<String> ServerFileList = new ArrayList<>(TempServerFileList);
        List<String> ClientFileList = new ArrayList<>(TempClientFileList);
        ClientFileList.retainAll(ServerFileList);
        ServerFileList.removeAll(ClientFileList);
        ServerFileArr = ServerFileList.stream().toArray(String[]::new);
        try {
          ObjectOut.writeObject(ServerFileArr);
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (ServerFileArr.length != 0) {
          for (int i = 0; i < ServerFileArr.length; i++) {
            File transferFile =
                    new File("/home/roczak/server/" + ClientId + "/" + ServerFileArr[i]);
            byte[] byteArray = new byte[(int) transferFile.length()];
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(byteArray, 0, byteArray.length);
            ObjectOut.write(byteArray, 0, byteArray.length);
            ObjectOut.flush();
            bin.close();
          }
          ObjectOut.reset();
          UserList.get(ClientId).toUpdate = true;
        }
        return MessageHandlerReturn.CONTINUE;

      } else if (request.DownloadClientListFlag) {
        List<String> ActiveUsersList = new ArrayList<>();
        for (int i : UserList.keySet()) {
          if (UserList.get(i).Active == true) {
            ActiveUsersList.add(UserList.get(i).ClientName);
          }
        }
        ObjectOut.reset();
        ObjectOut.writeObject(ActiveUsersList);
        return MessageHandlerReturn.CONTINUE;

      } else if (request.TransferFileFlag) {
        try {
          String fileName = (String) ObjectIn.readObject();
          String transferClientName = (String) ObjectIn.readObject();
          int transferClientId = 0;

          for (int i : UserList.keySet()) {
            if (UserList.get(i).ClientName.equals(transferClientName)) {
              transferClientId = i;
              break;
            }
          }

          if (transferClientId == 0) return MessageHandlerReturn.CONTINUE;

          File source = new File("/home/roczak/server/" + ClientId + "/" + fileName);
          File dest = new File("/home/roczak/server/" + transferClientId + "/" + fileName);
          System.out.println("nowyplik");
          dest.createNewFile();

          Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        return MessageHandlerReturn.CONTINUE;
      }
      return MessageHandlerReturn.CONTINUE;
    }
  }
}
