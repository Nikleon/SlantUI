module com.sprutuswesticus {
    requires transitive javafx.controls;
    requires javafx.fxml;

    opens com.sprutuswesticus to javafx.fxml;
    exports com.sprutuswesticus;
}