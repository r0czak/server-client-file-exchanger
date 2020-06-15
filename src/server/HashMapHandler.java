package server;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapHandler {
  public void SaveHashMap(ConcurrentHashMap<Integer, ClientInfo> UserList) throws IOException {
    FileOutputStream fileOut = new FileOutputStream("/home/roczak/server/userlist.ser");
    ObjectOutputStream out = new ObjectOutputStream(fileOut);
    out.writeObject(UserList);
    out.close();
    fileOut.close();
    System.out.println("HashMap saved");
  }

  public ConcurrentHashMap<Integer, ClientInfo> LoadHashMap()
          throws IOException, ClassNotFoundException {
    FileInputStream fileIn = new FileInputStream("/home/roczak/server/userlist.ser");
    ObjectInputStream in = new ObjectInputStream(fileIn);
    ConcurrentHashMap<Integer, ClientInfo> UserList = null;
    UserList = (ConcurrentHashMap<Integer, ClientInfo>) in.readObject();
    in.close();
    fileIn.close();
    System.out.println("HashMap loaded");
    return UserList;
  }
}
