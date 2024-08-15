module edu.upvictoria.sqlcodeeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires reactfx;
    requires org.fxmisc.richtext;
    requires plibs;
    requires org.java_websocket;

    opens edu.upvictoria.sqlcodeeditor to javafx.fxml;
    exports edu.upvictoria.sqlcodeeditor;
}