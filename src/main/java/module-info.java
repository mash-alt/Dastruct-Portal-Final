module org.finalproject.loginregisterfx {    requires javafx.controls;
    requires javafx.fxml;
    requires transitive com.google.gson;
    requires okhttp3;
    requires kotlin.stdlib;
    requires javafx.graphics;


    opens org.finalproject.loginregisterfx to javafx.fxml;
    opens org.finalproject.loginregisterfx.models to javafx.base;
    exports org.finalproject.loginregisterfx;
    exports org.finalproject.loginregisterfx.models;
    exports org.finalproject.loginregisterfx.Service;
}