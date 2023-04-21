package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;

    @FXML
    Button serverOn;

    @FXML
    ListView<String> chatList;



    @FXML
    TextArea inputArea;

    Map<String, List<Message>> name_content = new HashMap<>();

    Map<String, List<Message>> name_content_group = new HashMap<>();


    Set<String> chatSet = new HashSet<>();

    Set<String> chatGroupSet = new HashSet<>();

    Set<String> allChatSet = new HashSet<>(); //包括个人和群组

    Map<String, List<Message>> chatname_messages = new HashMap<>();

    @FXML
    Label currentOnlineCnt;

    String current_selected;

    String username;

    @FXML
    Label currentUsername;

    final int PORT = 9999;

    Socket socket = new Socket("localhost", PORT);
    PrintWriter out = new PrintWriter(socket.getOutputStream());
    Scanner in = new Scanner(socket.getInputStream());

    public Controller() throws IOException {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");


        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                Scanner in = new Scanner(socket.getInputStream());
                out.println("Store_name"+"!"+input.get());
                out.flush();

                String has_name = in.next();
                if (has_name.equals("false")){
                    username = input.get();
                    Main.recv = username;
                    currentUsername.setText("Current User: "+username);
                }else{
                    while (has_name.equals("true")){
                        System.out.println("reinput user name");
                        dialog.setContentText("Please change username:");
                        input = dialog.showAndWait();
                        out.println("Store_name"+"!"+input.get());
                        out.flush();

                        has_name = in.next();
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */

        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        chatContentList.setCellFactory(new MessageCellFactory());


        chatList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(item);

                setOnMouseClicked(event -> {
                    chatList.setStyle("-fx-control-inner-background: white;");
                    // 设置选中项的背景颜色为黄色
//                    ( event.getTarget()).setStyle("-fx-control-inner-background: yellow;");
                    chatContentList.getItems().clear();
                    if (!isEmpty()) {
                        String selectedItem = getItem();
                        chatContentList.getItems().clear();
                        if(!selectedItem.contains(",")){
                            for(int i=0;i<name_content.get(selectedItem).size();i++){
//                                if(Objects.equals(name_content.get(selectedItem).get(i).getSentBy(), selectedItem) || Objects.equals(name_content.get(selectedItem).get(i).getSendTo(), selectedItem)){
                                chatContentList.getItems().add(name_content.get(selectedItem).get(i));

                            }
                        }else {
                            for(int i=0;i<name_content_group.get(selectedItem).size();i++){
                                chatContentList.getItems().add(name_content_group.get(selectedItem).get(i));
                                System.out.println(name_content_group.get(selectedItem).get(i).getSentBy()+", "+name_content_group.get(selectedItem).get(i).getSendTo()+", "+name_content_group.get(selectedItem).get(i).getData());

                            }
                        }
                        System.out.println("chatContentList:");
                        for(int i=0;i<chatContentList.getItems().size();i++){
                            System.out.println(chatContentList.getItems().get(i).getSentBy()+" "+chatContentList.getItems().get(i).getSendTo()+" "+chatContentList.getItems().get(i).getData());
                        }

//                        chatContentList.getItems().addAll(name_content.get(getItem())) ;
//                        System.out.println("name_content:");
//                        for (String name: name_content.keySet()){
//                            List<Message> value = name_content.get(name);
//                            System.out.println(name);
//                            for(int i=0;i<value.size();i++){
//                                System.out.println(value.get(i).getSentBy()+", "+value.get(i).getSendTo()+", "+value.get(i).getData());
//                            }
//                        }
//                        System.out.println("name_content_group:");
//                        for (String name: name_content_group.keySet()){
//                            List<Message> value = name_content_group.get(name);
//                            System.out.println(name);
//                            for(int i=0;i<value.size();i++){
//                                System.out.println(value.get(i).getSentBy()+", "+value.get(i).getSendTo()+", "+value.get(i).getData());
//                            }
//                        }
//
//                        System.out.println("selectedItem: "+selectedItem); // 打印所选项目的名称
                    }
                });
            }
        });



        List<String> user_names = new ArrayList<>();
        Map<String, List<String>> name_messages = new HashMap<>();
        Map<String, Integer> name_mess_num = new HashMap<>();
        Map<String, Socket> user_socket = new HashMap<>();

        new Thread(new Client_Service(socket, user_names, name_messages, name_mess_num, user_socket, chatContentList, username,currentOnlineCnt, name_content, name_content_group,chatList)).start();

        try {
            Socket socket_checker = new Socket("localhost", PORT); // 连接到指定的主机和端口
            ConnectionChecker connectionChecker = new ConnectionChecker(socket_checker, serverOn); // 创建一个ConnectionChecker实例
            Thread thread = new Thread(connectionChecker); // 创建一个线程
            thread.start(); // 启动线程
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    public void createPrivateChat() throws IOException, InterruptedException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();


        out.println("Get_user_names");
        out.flush();


//        String usernames = in.next();
        Thread.sleep(50);
        List<String> usernames = Main.users;
        System.out.println("usernames: "+usernames);
        System.out.println(Main.users);

        currentOnlineCnt.setText("Online: "+Main.users.size());

        System.out.println(Main.users);

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll(Main.users);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        if(user.get()!=null){
            chatSet.add(user.get());
            allChatSet.add(user.get());
            List<Message> content = new ArrayList<>();
            name_content.putIfAbsent(user.get(), content);

            chatList.getItems().clear();

            chatList.getItems().addAll(chatSet);
            chatList.getItems().addAll(chatGroupSet);
//            chatContentList.getItems().add(new Message(System.currentTimeMillis(),username,user.get(),"\uD83D\uDE00"));
        }


        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }


    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws IOException, InterruptedException {
        List<AtomicReference<String>> users = new ArrayList<>();
        List<String> users_ = new ArrayList<>();
        String group_name = "";
        Stage stage = new Stage();
        out.println("Get_user_names");
        out.flush();

        Thread.sleep(50);
        List<String> usernames = Main.users;
        usernames.add(username);
        System.out.println("usernames: "+usernames);
        System.out.println(Main.users);
        currentOnlineCnt.setText("Online: "+Main.users.size());

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 20, 20, 20));
        List<CheckBox> checkBoxes = new ArrayList<>();
//        for(int i=0;i<usernames.split("!").length;i++){
//            checkBoxes.add(new CheckBox(usernames.split("!")[i]));
//        }
        for (String s : usernames) {
            checkBoxes.add(new CheckBox(s));
        }
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    AtomicReference<String> user = new AtomicReference<>();
                    user.set(checkBox.getText());
                    users.add(user);
                    users_.add(user.get());
                }
            }
            Collections.sort(users_);
            System.out.println(users);
            System.out.println("users_:"+users_);
            stage.close();
        });

        System.out.println("users_"+users_);
        hbox.getChildren().addAll(checkBoxes);
        hbox.getChildren().addAll(okBtn);
        stage.setScene(new Scene(hbox));
        stage.showAndWait();
        if(users_.size()>0){
            if(users_.size()>3){
                group_name = users_.get(0) + ", " + users_.get(1) + ", " + users_.get(2) + "... (" + users_.size() + ")";
            }else {
                for(int i=0;i<users_.size()-1;i++){
                    group_name+=users_.get(i)+(", ");
                }
                group_name+=users_.get(users_.size()-1)+" ("+users_.size()+")";
            }
            System.out.println("group_name: "+group_name);
            if(group_name!=null){
                chatGroupSet.add(group_name);
                allChatSet.add(group_name);
                List<Message> content = new ArrayList<>();
                name_content_group.putIfAbsent(group_name, content);
                chatList.getItems().clear();

                chatList.getItems().addAll(chatSet);
                chatList.getItems().addAll(chatGroupSet);
            }
        }



    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        current_selected = chatList.getSelectionModel().getSelectedItem();
        System.out.println(current_selected);
        String send_to = current_selected;
        String send_by = username;
        String input = inputArea.getText();
        if(input.contains("\n")){
            input = input.replace("\n", "~");

        }
        inputArea.clear();
        Long time = System.currentTimeMillis();
//        chatContentList.getItems().add(new Message(time,send_by,send_to,input));
        if (send_to!=null && !Objects.equals(input, "")){
            Message message = new Message(time,send_by,send_to,input.replace("~", "\n"));
            System.out.println("input: "+input);
            List<Message> content;
            if(send_to.contains(",")){
                content= name_content_group.get(send_to);
                content.add(message);
                name_content_group.put(send_by, content);
                name_content_group.put(send_to, content);
            }else {
                content= name_content.get(send_to);
                content.add(message);
                name_content.put(send_by, content);
                name_content.put(send_to, content);
            }


//            System.out.println("name_content: ");
//            for (String name: name_content.keySet()){
//                List<Message> value = name_content.get(name);
//                System.out.println(name);
//                for(int i=0;i<value.size();i++){
//                    System.out.println(value.get(i).getSentBy()+"  "+value.get(i).getSendTo()+"  "+value.get(i).getData());
//                }
//            }
//            System.out.println("name_content_group: ");
//            for (String name: name_content_group.keySet()){
//                List<Message> value = name_content_group.get(name);
//                System.out.println(name);
//                for(int i=0;i<value.size();i++){
//                    System.out.println(value.get(i).getSentBy()+"  "+value.get(i).getSendTo()+"  "+value.get(i).getData());
//                }
//            }
            chatContentList.getItems().add(message);
            String command_type;
            if(send_to.contains(", ")){//多人
                command_type = "Send_group_message";
            }else {//单人
                command_type = "Send_message";
            }
            String message_str = command_type+"!"+time+"!"+send_by+"!"+send_to+"!"+input;
            out.println(message_str);
            out.flush();
        }



        // TODO
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {

            return new ListCell<Message>() {
                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
