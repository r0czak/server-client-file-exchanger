package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {

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
      ObjectOut.reset();
      ObjectOut.writeObject(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public void SendMessage(Message request) {
    try {
      ObjectOut.reset();
      ObjectOut.writeObject(request);
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

  public void SendFiles(Message request, Path folderPath) throws IOException {
    request.UploadFiles();
    SendMessage(request);
    request.clear();
    try {
      ObjectOut.writeObject(folderPath.toFile().list());
    } catch (IOException e) {
      e.printStackTrace();
    }
    String[] FilesToSend = null;
    try {
      FilesToSend = (String[]) ObjectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (FilesToSend.length != 0) {
      for (int i = 0; i < FilesToSend.length; i++) {
        File transferFile = new File(folderPath + "/" + FilesToSend[i]);
        byte[] byteArray = new byte[(int) transferFile.length()];
        FileInputStream fin = new FileInputStream(transferFile);
        BufferedInputStream bin = new BufferedInputStream(fin);
        bin.read(byteArray, 0, byteArray.length);
        ObjectOut.write(byteArray, 0, byteArray.length);
        ObjectOut.flush();
        bin.close();
      }
      ObjectOut.reset();
    }
  }

  public void DownloadFiles(Message request, Path folderPath) throws IOException {
    request.DownloadFiles();
    SendMessage(request);
    request.clear();
    try {
      ObjectOut.writeObject(folderPath.toFile().list());
    } catch (IOException e) {
      e.printStackTrace();
    }
    String[] FilesToDownload = null;
    try {
      FilesToDownload = (String[]) ObjectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (FilesToDownload.length != 0) {
      for (int i = 0; i < FilesToDownload.length; i++) {
        byte[] byteArray = new byte[1024];
        File transferFile = new File(folderPath + "/" + FilesToDownload[i]);
        FileOutputStream fos = new FileOutputStream(transferFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = ObjectIn.read(byteArray, 0, byteArray.length);
        bos.write(byteArray, 0, bytesRead);
        bos.flush();
        bos.close();
      }
    }
  }

  public List<String> downloadUserList(Message request) {
    request.DownloadClientList();
    SendMessage(request);
    request.clear();

    List<String> ActiveUsersList = new ArrayList<>();
    try {
      ActiveUsersList = (List<String>) ObjectIn.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return ActiveUsersList;
  }
}
