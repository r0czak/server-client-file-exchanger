package client;

import java.io.Serializable;

public class Message implements Serializable {
  public boolean LogoutFlag = false;
  public boolean UploadFilesFlag = false;
  public boolean DownloadFilesFlag = false;
  public boolean DownloadClientListFlag = false;
  public boolean TransferFileFlag = false;

  public synchronized void clear() {
    LogoutFlag = false;
    UploadFilesFlag = false;
    DownloadFilesFlag = false;
    DownloadClientListFlag = false;
    TransferFileFlag = false;
  }

  public synchronized void Logout() {
    LogoutFlag = true;
  }

  public synchronized void UploadFiles() {
    UploadFilesFlag = true;
  }

  public synchronized void DownloadFiles() {
    DownloadFilesFlag = true;
  }

  public synchronized void DownloadClientList() {
    DownloadClientListFlag = true;
  }

  public synchronized void TransferFile() {
    TransferFileFlag = true;
  }
}
