package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Service implements Runnable {
    Socket s;
    List<String> user_names;

    Map<String, List<String>> name_messages;

    Map<String, Integer> name_mess_num;
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
        int readLen = 0;
        String user_name;
        while (true){

            try {
                if (!((readLen = inputStream.read(buf))!=-1)) break;
            } catch (IOException e) {
//                throw new RuntimeException(e);
                System.out.println();
            }
            if(!new String(buf, 0, readLen).equals("Get user names")){
                user_name = new String(buf, 0, readLen);
                String has_name = "false";
                if (user_names.contains(user_name)){
                    has_name = "true";
                }
                OutputStream outputStream = null;
                try {
                    outputStream = s.getOutputStream();
                } catch (IOException ignored) {

                }
                byte[] msg = has_name.getBytes();
                try {
                    outputStream.write(msg);
                } catch (IOException ignored) {

                }
                System.out.println("user_name: " + user_name);
                System.out.println("has_name: " + has_name);
                user_names.add(user_name);
            } else if (new String(buf, 0, readLen).equals("Get user names")) {
                OutputStream outputStream = null;
                try {
                    outputStream = s.getOutputStream();
                } catch (IOException ignored) {

                }
                byte[] msg = String.join("!", user_names).getBytes();
                try {
                    outputStream.write(msg);
                } catch (IOException e) {
                    System.out.println();
                }
            }


        }

            //以下为关闭各种要关闭的资源
//            fos.close();
//            is.close();
//            os.close();
//            socket.close();

        }



    }



