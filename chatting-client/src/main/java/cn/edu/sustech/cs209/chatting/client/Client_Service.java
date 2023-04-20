package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client_Service implements Runnable {
    Socket s;
    private Scanner in;
    Label currentOnlineCnt;
    private PrintWriter out;
    List<String> user_names;
    public String user_name;
    Map<String, Socket> user_socket;
    Map<String, List<String>> name_messages;
    Map<String, Integer> name_mess_num;
    ListView<Message> chatContentList;
    Map<String, List<Message>> name_content;
    Map<String, List<Message>> name_content_group;
    String username;
    public Client_Service(Socket socket, List<String> user_names, Map<String, List<String>> name_messages,
                          Map<String, Integer> name_mess_num, Map<String, Socket> user_socket,
                          ListView<Message> chatContentList, String username, Label currentOnlineCnt,
                          Map<String, List<Message>> name_content, Map<String, List<Message>> name_content_group){
        this.s=socket;
        this.user_names=user_names;
        this.name_messages=name_messages;
        this.name_mess_num=name_mess_num;
        this.user_socket=user_socket;
        this.chatContentList=chatContentList;
        this.username=username;
        this.currentOnlineCnt=currentOnlineCnt;
        this.name_content=name_content;
        this.name_content_group=name_content_group;
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
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void doService() throws IOException, InterruptedException {
        while (true) {
            if(name_messages.get(user_name)!=null){
                System.out.println(user_name+" "+name_messages.get(user_name)+" "+name_mess_num.get(user_name));
                if(name_messages.get(user_name).size()>name_mess_num.get(user_name)){
                    System.out.println("Server get mess");
                }
            }
            if (!in.hasNext()) return;
            String mess = in.nextLine();
            Main.recv = mess;
            System.out.println("mess"+mess);
            Main.users.clear();
            for (int i=0;i<mess.split("!").length;i++){
                if(!Objects.equals(mess.split("!")[i], username)){
                    Main.users.add(mess.split("!")[i]);
                }
            }
            executeCommand(mess);
        }
    }
    public void executeCommand(String message) throws IOException, InterruptedException {
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
                out_to.println("Get_message"+"!"+time+"!"+send_by+"!"+send_to+"!"+input);
                out_to.flush();
                //                name_mess_num.merge(send_to, 1, Integer::sum);
                break;
            case "Get_message":
                Long time_get = Long.valueOf(message.split("!")[1]);
                String send_by_get = message.split("!")[2];
                String send_to_get = message.split("!")[3];
                String input_get = message.split("!")[4];
                System.out.println("input get: "+input_get);
                Message message_get = new Message(time_get,send_by_get,send_to_get,input_get);
                Thread.sleep(50);
                List<Message> content= name_content.get(send_to_get);
                if (content!=null){
                    content.add(message_get);
                }else {
                    content=new ArrayList<>();
                    content.add(message_get);
                }

                name_content.put(send_by_get, content);
                name_content.put(send_to_get, content);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        chatContentList.getItems().add(message_get);
                    }
                });

                break;
            case "Get_group_message":
                Long time_get_group = Long.valueOf(message.split("!")[1]);
                String send_by_get_group = message.split("!")[2];
                String send_to_get_group = message.split("!")[3];
                String input_get_group = message.split("!")[4];
                System.out.println("input get: "+input_get_group);
                Message message_get_group = new Message(time_get_group,send_by_get_group,send_to_get_group,input_get_group);
                Thread.sleep(50);
                List<Message> content_group= name_content_group.get(send_to_get_group);
                if (content_group!=null){
                    content_group.add(message_get_group);
                }else {
                    content_group=new ArrayList<>();
                    content_group.add(message_get_group);
                }

                name_content_group.put(send_by_get_group, content_group);
                name_content_group.put(send_to_get_group, content_group);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        chatContentList.getItems().add(message_get_group);
                    }
                });

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







