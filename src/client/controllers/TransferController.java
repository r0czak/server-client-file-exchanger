package client.controllers;

import client.Message;
import client.SocketClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TransferController {
  private Path folderPath;
  @FXML
  private ListView<String> UserListView;
  @FXML
  Button transferButton;
  @FXML
  TextField clientnameTextField, fileTextField;
  private AppController appController;

  @FXML
  public void transferFile() {
    String transferClientname = clientnameTextField.getText();
    String filename = fileTextField.getText();
    if (transferClientname.isEmpty() || !UserListView.getItems().contains(transferClientname)) {
      System.out.println("Wrong username");
      Stage stage = (Stage) transferButton.getScene().getWindow();
      stage.close();
      return;
    }

    File f = new File(folderPath + "/" + filename);
    if (filename.isEmpty() || !f.exists()) {
      System.out.println("Wrong file");
      Stage stage = (Stage) transferButton.getScene().getWindow();
      stage.close();
      return;
    }

    appController.setTransferFlag(true);
    appController.setTransferClientname(transferClientname);
    appController.setTransferFilename(filename);
    Stage stage = (Stage) transferButton.getScene().getWindow();
    stage.close();
  }

  public void setFolderPath(Path folderPath) {
    this.folderPath = folderPath;
  }

  public void setUserListView(ListView<String> userListView) {
    this.UserListView = userListView;
  }

  public void setAppController(AppController appController) {
    this.appController = appController;
  }
}
