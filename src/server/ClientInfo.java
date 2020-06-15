package server;

import java.io.Serializable;
import java.nio.file.Path;

public class ClientInfo implements Serializable {
  public String ClientName;
  public String FolderPath;
  public boolean Active = false;

  public ClientInfo(String ClientName, String FolderPath) {
    this.ClientName = ClientName;
    this.FolderPath = FolderPath;
  }
}
