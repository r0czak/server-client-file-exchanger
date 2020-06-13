module projekt_po2 {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens server;
    opens server.controllers;
    opens client;
    opens client.controllers;
}
