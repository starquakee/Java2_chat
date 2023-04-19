package cn.edu.sustech.cs209.chatting.client;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client_Service implements Runnable {
    Socket s;
    private Scanner in;
    private PrintWriter out;
    List<String> user_names;

    public String user_name;

    Map<String, Socket> user_socket;

    Map<String, List<String>> name_messages;

    Map<String, Integer> name_mess_num;
    public Client_Service(Socket socket, List<String> user_names, Map<String, List<String>> name_messages,Map<String, Integer> name_mess_num, Map<String, Socket> user_socket){
        this.s=socket;
        this.user_names=user_names;
        this.name_messages=name_messages;
        this.name_mess_num=name_mess_num;
        this.user_socket=user_socket;
    }
    @Override
    public void run() {
        System.out.println("Client connected!");
        try {
            try {
                in = new Scanner(s.getInputStream());
                out = new PrintWriter(s.getOutputStream());
                doService();
            } finally {
                s.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void doService() throws IOException {


        while (true) {
            if(name_messages.get(user_name)!=null){
                System.out.println(user_name+" "+name_messages.get(user_name)+" "+name_mess_num.get(user_name));
                if(name_messages.get(user_name).size()>name_mess_num.get(user_name)){
                    System.out.println("Server get mess");
                }
            }
            if (!in.hasNext()) return;
            String mess = in.next();
            System.out.println(mess);
            excuteCommand(mess);
        }
    }
    public void excuteCommand(String message) throws IOException {
        String command = message.split("!")[0];
        switch (command){
            case "Get_user_names":
                String msg = String.join("!", user_names);
                System.out.println("user_names:"+user_names);
                out.println(msg);
                out.flush();
                break;
            case "Send_message":
                Long time = Long.valueOf(message.split("!")[1]);
                String send_by = message.split("!")[2];
                String send_to = message.split("!")[3];
                String input = message.split("!")[4];
                List<String> messages = name_messages.get(send_to);
                System.out.println(name_messages);
                System.out.println(messages);
                messages.add(message);
                name_messages.put(send_to, messages);
                System.out.println(name_messages);
                Socket send_to_socket = user_socket.get(send_to);
                PrintWriter out_to = new PrintWriter(send_to_socket.getOutputStream());
                out_to.println(time+"!"+send_by+"!"+send_to+"!"+input);
                out_to.flush();

                //                name_mess_num.merge(send_to, 1, Integer::sum);
                break;
            case "Store_name":
                user_name = message.split("!")[1];
                String has_name = "false";
                if (user_names.contains(user_name)){
                    has_name = "true";
                }
                out.println(has_name);
                out.flush();
                user_names.add(user_name);
                user_socket.put(user_name, s);
                for (String userName : user_names) {
                    name_mess_num.putIfAbsent(userName, 0);
                    List<String> m = new ArrayList<>();
                    name_messages.putIfAbsent(userName, m);
                }
                break;
        }
//            if(name_messages.get(user_name)!=null){
//                System.out.println(user_name+" "+name_messages.get(user_name)+" "+name_mess_num.get(user_name));
//                if(name_messages.get(user_name).size()>name_mess_num.get(user_name)){
//                    System.out.println("Server get mess");
//                }
//            }











    }

    //以下为关闭各种要关闭的资源
//            fos.close();
//            is.close();
//            os.close();
//            socket.close();

}






