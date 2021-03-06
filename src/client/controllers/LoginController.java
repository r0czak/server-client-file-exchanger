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

/** Klasa typu kontroler obsługująca okno logowania na serwer */
public class LoginController {
  private MainClientController mainClientController;
  public SocketClient socketClient;

  @FXML private TextField username, folderPath;

  @FXML
  protected void initialize() {
    setTextLimit(username, 16);
    setTextLimit(folderPath, 100);
  }

  /** Funkcja wywoływana po wciśnięciu przycisku "Sign in" w oknie logowania */
  @FXML
  public void signIn() throws IOException, ClassNotFoundException {

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

    socketClient = new SocketClient();

    socketClient.startConnection("127.0.0.1", 6666);
    socketClient.sendString(getUsername());
    socketClient.sendString(getFolderPath());
    if (!(boolean) socketClient.objectIn.readObject()) {
      System.out.println("This User is active");
      return;
    }

    // Inicjacja wątków głównego okna aplikacji
    appController.setClient(socketClient);
    appController.update();
    appController.fileExchange();

    mainClientController.setScreen(appPane);
  }

  public void setMainClientController(MainClientController mainClientController) {
    this.mainClientController = mainClientController;
  }

  public String getUsername() {
    return username.getText();
  }

  public String getFolderPath() {
    return folderPath.getText();
  }

  /**
   * Funkcja ustawiająca limit tekstu dla TextField
   *
   * @param textField TextField dla którego ustawiany będzie limit
   * @param length Maksymalna liczba liter
   */
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
