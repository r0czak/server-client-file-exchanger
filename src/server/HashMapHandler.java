package server;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapHandler {
    public void SaveHashMap(ConcurrentHashMap<Integer, ClientInfo> UserList, String filepath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filepath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(UserList);
        out.close();
        fileOut.close();
        System.out.println("HashMap saved");
    }

    public ConcurrentHashMap<Integer, ClientInfo> LoadHashMap(String filepath)
            throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filepath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        ConcurrentHashMap<Integer, ClientInfo> UserList = null;
        UserList = (ConcurrentHashMap<Integer, ClientInfo>) in.readObject();
        in.close();
        fileIn.close();
        System.out.println("HashMap loaded");
        return UserList;
    }
}
