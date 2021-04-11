module com.sprutuswesticus {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.sprutuswesticus to javafx.fxml;
    exports com.sprutuswesticus;
}