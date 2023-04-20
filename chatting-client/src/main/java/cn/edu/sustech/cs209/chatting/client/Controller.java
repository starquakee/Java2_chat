package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    ListView<String> chatList;



    @FXML
    TextArea inputArea;

    Map<String, List<Message>> name_content = new HashMap<>();


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
                    if (!isEmpty()) {
                        String selectedItem = getItem();
                        chatContentList.getItems().clear();
                        chatContentList.getItems().addAll(name_content.get(getItem())) ;
                        System.out.println(name_content);
                        System.out.println("selectedItem: "+selectedItem); // 打印所选项目的名称
                    }
                });
            }
        });



        List<String> user_names = new ArrayList<>();
        Map<String, List<String>> name_messages = new HashMap<>();
        Map<String, Integer> name_mess_num = new HashMap<>();
        Map<String, Socket> user_socket = new HashMap<>();

        new Thread(new Client_Service(socket, user_names, name_messages, name_mess_num, user_socket, chatContentList, username,currentOnlineCnt, name_content)).start();

    }

    @FXML
    public void createPrivateChat() throws IOException, InterruptedException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        Scanner in = new Scanner(socket.getInputStream());

        out.println("Get_user_names");
        out.flush();


//        String usernames = in.next();
        Thread.sleep(50);
        String usernames = Main.recv;
        System.out.println("recv: "+usernames);
        System.out.println(Main.users);

        currentOnlineCnt.setText("Online: "+Main.users.size());
//        List<String> list = new ArrayList<>();
//        for (int i=0;i<usernames.split("!").length;i++){
//            if(!Objects.equals(usernames.split("!")[i], username)){
//                list.add(usernames.split("!")[i]);
//            }
//        }
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
            name_content.put(user.get(),content);
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
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        Scanner in = new Scanner(socket.getInputStream());
        String group_name = "";
        Stage stage = new Stage();
        out.println("Get user names");
        out.flush();

        Thread.sleep(50);
        String usernames = Main.recv;
        System.out.println("recv: "+usernames);
        System.out.println(Main.users);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 20, 20, 20));
        List<CheckBox> checkBoxes = new ArrayList<>();
        for(int i=0;i<usernames.split("!").length;i++){
            checkBoxes.add(new CheckBox(usernames.split("!")[i]));
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
            System.out.println(users);
            stage.close();
        });
        hbox.getChildren().addAll(checkBoxes);
        hbox.getChildren().addAll(okBtn);
        stage.setScene(new Scene(hbox));
        stage.showAndWait();

        if(users_.size()>3){
            Collections.sort(users_);
            group_name = users_.get(0) + ", " + users_.get(1) + ", " + users_.get(2) + "... (" + users_.size() + ")";
        }else {
            for(int i=0;i<users_.size()-1;i++){
                group_name+=users_.get(i)+(", ");
            }

            group_name+=users_.get(users_.size()-1)+" ("+users_.size()+")";
        }
        chatGroupSet.add(group_name);
        allChatSet.add(group_name);
        List<Message> content = new ArrayList<>();
//        content.setCellFactory(new MessageCellFactory());
        name_content.put(group_name,content);
        chatList.getItems().clear();

        chatList.getItems().addAll(chatSet);
        chatList.getItems().addAll(chatGroupSet);
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
        inputArea.clear();
        Long time = System.currentTimeMillis();
//        chatContentList.getItems().add(new Message(time,send_by,send_to,input));
        if(!send_to.contains(", ")){ //单人聊天
            Message message = new Message(time,send_by,send_to,input);
            System.out.println("name_content: "+name_content);
            List<Message> content= name_content.get(send_to);
            content.add(message);
            name_content.put(send_by, content);
            name_content.put(send_to, content);
            chatContentList.getItems().add(message);
            String message_str = "Send_message"+"!"+time+"!"+send_by+"!"+send_to+"!"+input;
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());
            out.println(message_str);
            out.flush();
        }else { //多人聊天

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
//                    setOnMouseClicked(event -> {
//                        if (!isEmpty()) {
//
//                            String selectedItem = listView.getSelectionModel().getSelectedItem();
//                            chatContentList = name_content.get(getItem().g);
//
//                        }
//                    });

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };


        }
    }

}
