package server;

import java.io.Serializable;

/**
 * Klasa przetrzymująca informacje o kliencie
 *
 * <p>Zobacz {@link server.controllers.MainServerController} odnośnie flag odpowiadających za zmiany
 * w podpanelach
 */
public class ClientInfo implements Serializable {

  public String clientName;

  public boolean active = false;
  /** odpowiada za powiadomienie o konieczności dodania nowego podpanelu na serwerze */
  public boolean toDraw = false;

  /** dpowiada za powiadomienie o konieczności usunięcia istniejącego podpanelu */
  public boolean toRemove = false;
  /** odpowiada za powiadomienie o konieczności aktualizacji listy plików na podpanelu */
  public boolean toUpdate = false;

  public ClientInfo(String clientName) {
    this.clientName = clientName;
  }
}
