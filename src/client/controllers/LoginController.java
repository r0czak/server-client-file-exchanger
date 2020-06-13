package client.controllers;

import client.SocketClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoginController {
    private MainServerController mainServerController;
    private SocketClient socketClient;

    @FXML
    private TextField username, folderPath;

    @FXML
    protected void initialize() {
        setTextLimit(username, 16);
        setTextLimit(folderPath, 100);
    }

  @FXML
  public void signIn() {

    if (getUsername().isEmpty()) {
      System.out.println("No username");
      return;
    }
    Path path = Paths.get(getFolderPath());
    if (!Files.exists(path) || getFolderPath().isEmpty()) {
      System.out.println("Wrong filepath");
      return;
    }

    FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/resources/AppScreen.fxml"));
    SplitPane appPane = null;
    try {
      appPane = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }

    AppController appController = loader.getController();
    appController.setFolderPath(path);
    appController.main();

      SocketClient client = new SocketClient();

      client.startConnection("127.0.0.1", 6666);
      client.out.println(getUsername());
      client.out.println(getFolderPath());

      mainServerController.setScreen(appPane);
  }

    public void setMainServerController(MainServerController mainServerController) {
        this.mainServerController = mainServerController;
    }

    public String getUsername() {
        return username.getText();
    }

    public String getFolderPath() {
        return folderPath.getText();
    }

  public static void setTextLimit(TextField textField, int length) {
    textField.setOnKeyTyped(
            event -> {
              String string = textField.getText();

              if (string.length() > length) {
                textField.setText(string.substring(0, length));
                textField.positionCaret(string.length());
              }
            });
  }
}
