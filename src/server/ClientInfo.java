package server;

import java.io.Serializable;

public class ClientInfo implements Serializable {
  public String ClientName;
  public String FolderPath;
  public boolean Active = false;
  public boolean toDraw = false;
  public boolean toRemove = false;
  public boolean toUpdate = false;

  public ClientInfo(String ClientName, String FolderPath) {
    this.ClientName = ClientName;
    this.FolderPath = FolderPath;
  }
}
