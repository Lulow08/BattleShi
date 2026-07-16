module com.cuatrifasico.battleshi {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.cuatrifasico.battleshi to javafx.fxml;
    opens com.cuatrifasico.battleshi.controller to javafx.fxml;
    exports com.cuatrifasico.battleshi;
}