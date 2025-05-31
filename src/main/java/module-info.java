module com.example.javafxapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.javafxapp to javafx.fxml;
    exports com.example.javafxapp;
    exports com.example.javafxapp.model;
    opens com.example.javafxapp.model to javafx.fxml;
    exports com.example.javafxapp.util;
    opens com.example.javafxapp.util to javafx.fxml;
    exports com.example.javafxapp.controller;
    opens com.example.javafxapp.controller to javafx.fxml;
}