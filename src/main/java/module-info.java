module org.finalproject.loginregisterfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires okhttp3;


    opens org.finalproject.loginregisterfx to javafx.fxml;
    exports org.finalproject.loginregisterfx;
}