package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws IOException {
        List<String> user_names = new ArrayList<>();
        Map<String, List<String>> name_messages = new HashMap<>();
        Map<String, Integer> name_mess_num = new HashMap<>();
        Map<String, Socket> user_socket = new HashMap<>();
        user_names.add("f");
        user_names.add("ff");
        user_names.add("fff");
        user_names.add("ffff");
        user_names.add("fffff");
        user_names.add("ffffff");
        user_names.add("fffffff");
        final int PORT = 9999;
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Waiting for clients to connect...");
        while (true){
            Socket s = server.accept();
            new Thread(new Service(s, user_names, name_messages, name_mess_num, user_socket)).start();
        }
    }
}
