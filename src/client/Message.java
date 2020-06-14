package client;

import java.io.Serializable;

public class Message implements Serializable {
  public int ClientId;
  public int TransferClientId;
  public String Filename;
  public boolean LogoutFlag = false;
  public boolean SendFilesFlag = false;
  public boolean DownloadFilesFlag = false;
  public boolean DownloadClientListFlag = false;
  public boolean TransferFileFlag = false;

  public Message(int id) {
    this.ClientId = id;
  }

  public void clear() {
    LogoutFlag = false;
    SendFilesFlag = false;
    DownloadFilesFlag = false;
    DownloadClientListFlag = false;
    TransferFileFlag = false;
  }

  public void Logout() {
    LogoutFlag = true;
  }

  public void SendFiles() {
    SendFilesFlag = true;
  }

  public void DownloadFiles() {
    DownloadFilesFlag = true;
  }

  public void DownloadClientList() {
    DownloadClientListFlag = true;
  }

  public void TransferFile(int TransferClientId, String Filename) {
    this.TransferClientId = TransferClientId;
    this.Filename = Filename;
    TransferFileFlag = true;
  }
}
