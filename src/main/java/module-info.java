module com.mycompany.ftsskripsi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.ftsskripsi to javafx.fxml;
    exports com.mycompany.ftsskripsi;
    requires poi;
    requires com.google.gson;
    requires poi.ooxml;
}
