package server;

import client.Client;

import java.nio.file.Path;

public class ClientInfo {
  public String ClientName;
  public Path FolderPath;

  public ClientInfo(String ClientName, Path FolderPath) {
    this.ClientName = ClientName;
    this.FolderPath = FolderPath;
  }
}
