package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {
    static String recv;
    static List<String> users = new ArrayList<>();
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        final int PORT = 9999;

        Socket socket = new Socket("localhost", PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        stage.setOnCloseRequest(event -> {
            out.println("Delete_user!"+recv);
            out.flush();
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
    }
}
