package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Klasa obsługująca połączenie klienta z serwerem */
public class SocketClient {

  private Socket clientSocket;
  public ObjectOutputStream objectOut;
  public ObjectInputStream objectIn;

  /** Funkcja rozpoczynająca połączenie klienta z serwerem */
  public void startConnection(String host_name, int port) {
    try {
      clientSocket = new Socket(host_name, port);

      objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
      objectIn = new ObjectInputStream(clientSocket.getInputStream());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String sendString(String msg) {
    try {
      objectOut.reset();
      objectOut.writeObject(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public void sendRequest(Message request) {
    try {
      objectOut.reset();
      objectOut.writeObject(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stopConnection() {
    try {
      objectIn.close();
      objectOut.close();
      clientSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Funkcja odpowiedzialna za wysyłanie plików z klienta na serwer */
  public void sendFiles(Message request, Path folderPath) throws IOException {
    request.uploadFiles();
    sendRequest(request);
    request.clear();
    // Wysłanie listy plików w folderze lokalnym
    try {
      objectOut.writeObject(folderPath.toFile().list());
    } catch (IOException e) {
      e.printStackTrace();
    }
    String[] filesToSend = null;
    // Pobranie listy plików do wysłania na serwer
    try {
      filesToSend = (String[]) objectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (filesToSend.length != 0) {
      // Pętla odpowiadająca za przesył plików z folderu lokalnego do serwera
      for (int i = 0; i < filesToSend.length; i++) {
        File transferFile = new File(folderPath + "/" + filesToSend[i]);
        byte[] byteArray = new byte[(int) transferFile.length()];
        FileInputStream fin = new FileInputStream(transferFile);
        BufferedInputStream bin = new BufferedInputStream(fin);
        bin.read(byteArray, 0, byteArray.length);
        objectOut.write(byteArray, 0, byteArray.length);
        objectOut.flush();
        bin.close();
      }
      objectOut.reset();
    }
  }

  /** Funkcja odpowiedzialna za pobranie plików z serwera do folderu lokalnego */
  public void downloadFiles(Message request, Path folderPath) throws IOException {
    request.downloadFiles();
    sendRequest(request);
    request.clear();
    // Wysłanie listy plików w folderze lokalnym
    try {
      objectOut.writeObject(folderPath.toFile().list());
    } catch (IOException e) {
      e.printStackTrace();
    }
    String[] filesToDownload = null;
    // Pobranie listy plików do pobrania z serwera
    try {
      filesToDownload = (String[]) objectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (filesToDownload.length != 0) {
      // Pętla odpowiadająca za pobranie plików z serwera do folderu lokalnego
      for (int i = 0; i < filesToDownload.length; i++) {
        byte[] byteArray = new byte[1024];
        File transferFile = new File(folderPath + "/" + filesToDownload[i]);
        FileOutputStream fos = new FileOutputStream(transferFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = objectIn.read(byteArray, 0, byteArray.length);
        bos.write(byteArray, 0, bytesRead);
        bos.flush();
        bos.close();
      }
    }
  }

  /**
   * Funkcja odpowiedzialna za pobranie listy aktywnych klientów na serwerze
   *
   * @return Zwraca listę klientów na serwerze
   */
  public List<String> downloadUserList(Message request) {
    request.downloadClientList();
    sendRequest(request);
    request.clear();

    List<String> ActiveUsersList = new ArrayList<>();
    try {
      ActiveUsersList = (List<String>) objectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return ActiveUsersList;
  }

  /**
   * Funkcja odpowiedzialna za przesył pliku pomiędzy klientami
   *
   * @param fileName Nazwa pliku
   * @param transferClientName Nazwa klienta do którego chcemy przesłać plik
   */
  public void transferFile(Message request, String fileName, String transferClientName) {
    request.transferFile();
    sendRequest(request);
    request.clear();
    try {
      objectOut.writeObject(fileName);
      objectOut.writeObject(transferClientName);
      objectOut.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Funkcja odpowiedzialna za wysłanie żądania o usunięcie pliku z serwera */
  public void deleteFile(Message request, String fileName) {
    request.deleteFile();
    sendRequest(request);
    request.clear();

    try {
      objectOut.writeObject(fileName);
      objectOut.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
