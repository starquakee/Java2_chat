package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Service implements Runnable {
    Socket s;
    private Scanner in;
    private PrintWriter out;
    List<String> user_names;

    public String user_name;

    Map<String, Socket> user_socket;

    Map<String, List<String>> name_messages;

    Map<String, Integer> name_mess_num;
    public Service(Socket socket, List<String> user_names, Map<String, List<String>> name_messages,Map<String, Integer> name_mess_num, Map<String, Socket> user_socket){
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

            try {
                Thread.sleep(1000); // 等待1秒钟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!s.isConnected()) {
                System.out.println("Connection lost!"); // 打印“Connection lost!”
                break;
            }else {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

            if(name_messages.get(user_name)!=null){
                System.out.println(user_name+" "+name_messages.get(user_name)+" "+name_mess_num.get(user_name));
                if(name_messages.get(user_name).size()>name_mess_num.get(user_name)){
                    System.out.println("Server get mess");
                }
            }
            if (!in.hasNext()) return;
            String mess = in.nextLine();
//            System.out.println("line: "+line);
//            if(mess.startsWith("Get")||mess.startsWith("Send")||mess.startsWith("Store")){
//
//            }
//            else{
//                String line;
//                while ((line = in.nextLine()) != null) {
//                    System.out.println("line: "+line);
//                    mess = mess +"\n"+ line;
//
//                }
//            }

            System.out.println("mess: "+mess);
            executeCommand(mess);
        }
    }
    public void executeCommand(String message) throws IOException {
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
                System.out.println("input: "+input);
                List<String> m = new ArrayList<>();
                name_messages.putIfAbsent(send_to, m);
                List<String> messages = name_messages.get(send_to);
                System.out.println(name_messages);
                System.out.println("send to: "+send_to);
                System.out.println(messages);
                messages.add(message);
                name_messages.put(send_to, messages);
                System.out.println(name_messages);
                System.out.println(user_socket);
                Socket send_to_socket = user_socket.get(send_to);
                PrintWriter out_to = new PrintWriter(send_to_socket.getOutputStream());
                out_to.println("Get_message"+"!"+time+"!"+send_by+"!"+send_to+"!"+input);
                out_to.flush();
                //                name_mess_num.merge(send_to, 1, Integer::sum);
                break;
            case "Send_group_message":
                Long time_group = Long.valueOf(message.split("!")[1]);
                String send_by_group = message.split("!")[2];
                String send_to_group = message.split("!")[3];
                String input_group = message.split("!")[4];
                System.out.println("input: "+input_group);
                List<String> m_group = new ArrayList<>();
                name_messages.putIfAbsent(send_to_group, m_group);
                String[] group_users;
                if(send_to_group.contains("...")){
                    group_users = send_to_group.split("...")[0].split(", ");
                }else {
                    group_users = send_to_group.split(" \\(")[0].split(", ");
                }
                List<String> messages_group = name_messages.get(send_to_group);
                messages_group.add(message);
                name_messages.put(send_to_group, messages_group);
                for(String group_user:group_users){
                    if(!Objects.equals(group_user, send_by_group)){
                        Socket send_to_socket_group = user_socket.get(group_user);
                        PrintWriter out_to_group = new PrintWriter(send_to_socket_group.getOutputStream());
                        out_to_group.println("Get_group_message"+"!"+time_group+"!"+send_by_group+"!"+send_to_group+"!"+input_group);
                        out_to_group.flush();
                    }

                }
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
                    List<String> mm = new ArrayList<>();
                    name_messages.putIfAbsent(userName, mm);
                }
                break;
            case "Delete_user" :
                String user_to_delete = message.split("!")[1];
                user_names.remove(user_to_delete);
                break;
        }
    }
}







