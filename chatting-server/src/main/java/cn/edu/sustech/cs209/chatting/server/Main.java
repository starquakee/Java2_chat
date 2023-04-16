package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) throws IOException {
        List<String> user_names = new ArrayList<>();
        user_names.add("f");
        final int PORT = 9999;
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Waiting for clients to connect...");
        while (true){
            Socket s = server.accept();
            new Thread(new Service(s, user_names)).start();
        }
    }
}
