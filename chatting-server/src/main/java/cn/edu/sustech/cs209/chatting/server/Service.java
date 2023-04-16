package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Service implements Runnable {
    Socket s;
    List<String> user_names;
    public Service(Socket socket, List<String> user_names){
        this.s=socket;
        this.user_names=user_names;
    }
    @Override
    public void run() {
        System.out.println("Client connected!");
        InputStream inputStream = null;
        try {
            inputStream = s.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        byte[] buf = new byte[1024];
        int readLen;
        while (true){
            try {
                if (!((readLen = inputStream.read(buf))!=-1)) break;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            String user_name = new String(buf, 0, readLen);
            String has_name = "false";
            if (user_names.contains(user_name)){
                has_name = "true";
            }
            OutputStream outputStream = null;
            try {
                outputStream = s.getOutputStream();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            byte[] msg = has_name.getBytes();
            try {
                outputStream.write(msg);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("user_name: " + user_name);
            System.out.println("has_name: " + has_name);
            user_names.add(user_name);
        }

            //以下为关闭各种要关闭的资源
//            fos.close();
//            is.close();
//            os.close();
//            socket.close();

        }



    }



