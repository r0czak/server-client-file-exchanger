package client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainServerController {
    @FXML
    private StackPane mainStackPane;

    @FXML
    public void initialize() {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/resources/loginPane.fxml"));
        Pane loginPane = null;
        try {
            loginPane = loader.load();
        } catch (IOException e) {
      e.printStackTrace();
    }
    LoginController loginController = loader.getController();
        loginController.setMainServerController(this);
        setScreen(loginPane);
  }

  public void setScreen(Pane pane) {
    mainStackPane.getChildren().clear();
    mainStackPane.getChildren().add(pane);
  }

  public void setScreen(SplitPane pane) {
    mainStackPane.getChildren().clear();
    mainStackPane.getChildren().add(pane);
  }
}
