package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConnectionChecker implements Runnable {
    private Socket socket;
    Button serverOn;

    public ConnectionChecker(Socket socket, Button serverOn) throws IOException {
        this.socket = socket;
        this.serverOn = serverOn;
    }

    @Override
    public void run() {
        while (true) {
            // 定义心跳包内容
            String heartbeatMsg = "heartbeat";

            // 发送心跳包
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(heartbeatMsg.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("LOST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
                        serverOn.setStyle("-fx-background-color: red;");
                    }
                });
                break;
            }

            // 等待一段时间后再发送心跳包
            try {
                Thread.sleep(5000); // 等待5秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

