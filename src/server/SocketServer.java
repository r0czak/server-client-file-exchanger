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

/** Klasa obsługująca połączenie serwera z klientem */
public class SocketServer extends Thread {
  /**
   * Hashmapa przechowująca unikalny ID klienta jako klucz oraz obiekt ClientInfo przechowujący dane
   * klienta
   */
  public ConcurrentHashMap<Integer, ClientInfo> userList;

  public HashMapHandler hashMapHandler;
  private ServerSocket socketServer;
  private int port;
  private static String serverFolderPath = "/home/roczak/server/";

  /**
   * Klasa służąca do powiadomienia serwera o kontynuowaniu lub zatrzymaniu połączenia z klientem
   */
  public class MessageHandlerReturn {
    public static final int STOP = 1;
    public static final int CONTINUE = 2;
  }

  public void stopServer() {
    try {
      socketServer.close();
      hashMapHandler.saveHashMap(userList, serverFolderPath + "userlist.ser");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public SocketServer(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    userList = new ConcurrentHashMap<>();
    hashMapHandler = new HashMapHandler();
    try {
      userList = hashMapHandler.loadHashMap(serverFolderPath + "userlist.ser");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    try {
      System.out.println("*************\nServer is on!\n*************\n\n");
      socketServer = new ServerSocket(port);
      while (true) {
        new SocketServer.ServerHandler(socketServer.accept(), userList, hashMapHandler).start();
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      stopServer();
    }
  }

  /** Klasa obsługująca komunikację między klientem a serwerem */
  public static class ServerHandler extends Thread {
    private Socket clientSocket;
    public ConcurrentHashMap<Integer, ClientInfo> userList;
    public HashMapHandler hashMapHandler;
    public ObjectOutputStream objectOut;
    public ObjectInputStream objectIn;
    public Message request;
    /** unikalne ID dla klienta obsługiwanego przez obiekt ServerHandler */
    private int clientId;

    public ServerHandler(
        Socket socket,
        ConcurrentHashMap<Integer, ClientInfo> userList,
        HashMapHandler hashMapHandler) {
      this.clientSocket = socket;
      this.userList = userList;
      this.hashMapHandler = hashMapHandler;
    }

    @Override
    public void run() {
      try {
        objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
        objectIn = new ObjectInputStream(clientSocket.getInputStream());

        if (registerClient((String) objectIn.readObject(), (String) objectIn.readObject())) {
          objectOut.writeObject(true);
          System.out.println(userList.get(clientId).clientName + " joined server");
        } else {
          objectOut.writeObject(false);
          stopConnection();
          return;
        }

        // Pętla odczytująca żądania od klienta
        while (true) {
          request = (Message) objectIn.readObject();
          if (messageHandler(request) == MessageHandlerReturn.STOP) {
            System.out.println(userList.get(clientId).clientName + " left server");
            break;
          }
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /** Funkcja zamykająca połączenie miedzy klientem a serwerem */
    public void stopConnection() {
      try {
        objectIn.close();
        objectOut.close();
        clientSocket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Funkcja rejestrująca klienta na serwerze
     *
     * @param ClientName Nazwa klienta
     * @param FolderPath Ścieżka do folderu lokalnego
     * @return Zwraca true jeżeli rejestracja przebiegnie pomyślnie.
     */
    public boolean registerClient(String ClientName, String FolderPath) {
      boolean existsFlag =
          false; // Flaga sprawdzająca czy użytkownik, był już kiedyś zarejestrowany
      boolean isActive = false; // Flaga sprawdzająca czy użytkownik jest aktywny
      for (int i : userList.keySet()) {
        if (ClientName.equals(userList.get(i).clientName)) {
          if (userList.get(i).active == true) {
            isActive = true;
          } else {
            this.clientId = i;
            existsFlag = true;
          }
          break;
        }
      }

      if (isActive) {
        return false;
      }

      if (existsFlag) {
        userList.get(clientId).active = true;
        userList.get(clientId).toDraw = true;
      } else {
        // Generowanie i przypisanie unikalnego ID do nowego klienta
        Random generator = new Random();
        boolean idExistsFlag = true;
        int newId = 0;

        while (idExistsFlag) {
          newId = generator.nextInt(9000) + 1000;
          idExistsFlag = false;
          for (int i : userList.keySet()) {
            if (i == newId) {
              idExistsFlag = true;
              break;
            }
          }
        }
        // Dodawanie klienta do hashmapy userList
        ClientInfo newClient = new ClientInfo(ClientName);
        userList.put(newId, newClient);
        userList.get(newId).active = true;
        userList.get(newId).toDraw = true;
        this.clientId = newId;
        // Tworzenie folderu dla nowego klienta
        new File(serverFolderPath + this.clientId).mkdir();
      }

      return true;
    }

    /**
     * Funkcja odczytująca żądanie oraz obsługująca żądania
     *
     * @param request Żądanie od klienta
     * @return Zwraca informacje o kontynuacji połączenia z klientem
     */
    public int messageHandler(Message request) throws IOException {
      if (request.logoutFlag) { // Żądanie wylogowania klienta
        userList.get(clientId).active = false;
        userList.get(clientId).toRemove = true;
        stopConnection();
        return MessageHandlerReturn.STOP;

      } else if (request.uploadFilesFlag) { // Żądanie wysłania plików z klienta na serwer
        String[] clientFileArr = null, serverFileArr;
        File temp = new File(serverFolderPath + clientId);
        serverFileArr = temp.list();
        try {
          clientFileArr = (String[]) objectIn.readObject();
        } catch (IOException e) {
          System.out.println(userList.get(clientId).clientName + " left server");
          userList.get(clientId).active = false;
          userList.get(clientId).toUpdate = false;
          userList.get(clientId).toRemove = true;
          stopConnection();
          return MessageHandlerReturn.STOP;
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        /*
        Klient wysyła listę plików w folderze lokalnym.
        Serwer porównuje obie listy oraz oblicza różnicę.
        (Lista plików w folderze lokalnym) - (lista plików znajdujących się w folderze lokalnym i na serwerze)
         */
        List<String> tempServerFileList = Arrays.asList(serverFileArr);
        List<String> tempClientFileList = Arrays.asList(clientFileArr);
        List<String> serverFileList = new ArrayList<>(tempServerFileList);
        List<String> clientFileList = new ArrayList<>(tempClientFileList);
        serverFileList.retainAll(clientFileList);
        clientFileList.removeAll(serverFileList);
        clientFileArr = clientFileList.stream().toArray(String[]::new);
        try {
          objectOut.writeObject(clientFileArr);
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (clientFileArr.length != 0) {
          // Pętla przesyłająca pliki z klienta na serwer
          for (int i = 0; i < clientFileArr.length; i++) {
            byte[] byteArray = new byte[1024];
            FileOutputStream fos =
                new FileOutputStream(serverFolderPath + clientId + "/" + clientFileArr[i]);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = objectIn.read(byteArray, 0, byteArray.length);
            bos.write(byteArray, 0, bytesRead);
            bos.flush();
            bos.close();
          }
          userList.get(clientId).toUpdate = true;
        }
        return MessageHandlerReturn.CONTINUE;

      } else if (request.downloadFilesFlag) { // Żądanie ściągnięcia plików z serwera do klienta
        String[] clientFileArr = null, serverFileArr;
        File temp = new File(serverFolderPath + clientId);
        serverFileArr = temp.list();
        try {
          clientFileArr = (String[]) objectIn.readObject();
        } catch (IOException e) {
          System.out.println(userList.get(clientId).clientName + " left server");
          userList.get(clientId).active = false;
          userList.get(clientId).toUpdate = false;
          userList.get(clientId).toRemove = true;
          stopConnection();
          return MessageHandlerReturn.STOP;
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        /*
        Klient wysyła listę plików w folderze lokalnym.
        Serwer porównuje obie listy oraz oblicza różnicę.
        (Lista plików w folderze lokalnym) - (lista plików znajdujących się w folderze lokalnym i na serwerze)
         */
        List<String> tempServerFileList = Arrays.asList(serverFileArr);
        List<String> tempClientFileList = Arrays.asList(clientFileArr);
        List<String> serverFileList = new ArrayList<>(tempServerFileList);
        List<String> clientFileList = new ArrayList<>(tempClientFileList);
        clientFileList.retainAll(serverFileList);
        serverFileList.removeAll(clientFileList);
        serverFileArr = serverFileList.stream().toArray(String[]::new);
        try {
          objectOut.writeObject(serverFileArr);
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (serverFileArr.length != 0) {
          // Pętla przesyłająca pliki z serwera do klienta
          for (int i = 0; i < serverFileArr.length; i++) {
            File transferFile = new File(serverFolderPath + clientId + "/" + serverFileArr[i]);
            byte[] byteArray = new byte[(int) transferFile.length()];
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(byteArray, 0, byteArray.length);
            objectOut.write(byteArray, 0, byteArray.length);
            objectOut.flush();
            bin.close();
          }
          objectOut.reset();
          userList.get(clientId).toUpdate = true;
        }
        return MessageHandlerReturn.CONTINUE;

      } else if (request.downloadClientListFlag) { // Żądanie pobrania listy aktywnych klientów
        List<String> activeUsersList = new ArrayList<>();
        for (int i : userList.keySet()) {
          if (userList.get(i).active == true
              && !userList.get(i).clientName.equals(userList.get(clientId).clientName)) {
            activeUsersList.add(userList.get(i).clientName);
          }
        }
        objectOut.reset();
        objectOut.writeObject(activeUsersList);
        return MessageHandlerReturn.CONTINUE;

      } else if (request.transferFileFlag) { // Żądanie przesłania plików między klientami
        try {
          String fileName = (String) objectIn.readObject();
          String transferClientName = (String) objectIn.readObject();
          int transferClientId = 0;

          for (int i : userList.keySet()) {
            if (userList.get(i).clientName.equals(transferClientName)) {
              transferClientId = i;
              break;
            }
          }

          if (transferClientId == 0) return MessageHandlerReturn.CONTINUE;

          File source = new File(serverFolderPath + clientId + "/" + fileName);
          File dest = new File(serverFolderPath + transferClientId + "/" + fileName);
          dest.createNewFile();

          Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        return MessageHandlerReturn.CONTINUE;
      } else if (request.deleteFileFlag) { // Żądanie usunięcia pliku z folderu na serwerze
        try {
          String fileName = (String) objectIn.readObject();
          File file = new File(serverFolderPath + clientId + "/" + fileName);
          file.delete();
          userList.get(clientId).toUpdate = true;
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }

        return MessageHandlerReturn.CONTINUE;
      }
      return MessageHandlerReturn.CONTINUE;
    }
  }
}
