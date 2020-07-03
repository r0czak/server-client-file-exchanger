package client;

import java.io.Serializable;

/**
 * Klasa odpowiadająca za treść żądań wysyłanych z klienta do serwera. Posiada funkcje ustawiające
 * za ustawianie flag
 */
public class Message implements Serializable {

  /** flaga odpowiadająca za żądanie wylogowania się z serwera */
  public boolean logoutFlag = false;
  /** flaga odpowiadająca za żądanie wysłania plików z klienta na serwer */
  public boolean uploadFilesFlag = false;

  /** flaga odpowiadająca za żądanie pobrania plików z serwera do klienta */
  public boolean downloadFilesFlag = false;

  /** flaga odpowiadająca za żądanie pobrania listy aktywnych użytkowników z serwera */
  public boolean downloadClientListFlag = false;

  /** flaga odpowiadająca za żądanie przesłania pliku pomiędzy klientami */
  public boolean transferFileFlag = false;

  /** flaga odpowiadająca za powiadomienie u usunięciu pliku w folderze lokalnym */
  public boolean deleteFileFlag = false;

  public synchronized void clear() {
    logoutFlag = false;
    uploadFilesFlag = false;
    downloadFilesFlag = false;
    downloadClientListFlag = false;
    transferFileFlag = false;
    deleteFileFlag = false;
  }

  public synchronized void logout() {
    logoutFlag = true;
  }

  public synchronized void uploadFiles() {
    uploadFilesFlag = true;
  }

  public synchronized void downloadFiles() {
    downloadFilesFlag = true;
  }

  public synchronized void downloadClientList() {
    downloadClientListFlag = true;
  }

  public synchronized void transferFile() {
    transferFileFlag = true;
  }

  public synchronized void deleteFile() {
    deleteFileFlag = true;
  }
}
