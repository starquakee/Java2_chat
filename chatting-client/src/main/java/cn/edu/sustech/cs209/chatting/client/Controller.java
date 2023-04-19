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


    Set<String> chatSet = new HashSet<>();

    Set<String> chatGroupSet = new HashSet<>();

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


                OutputStream outputStream = socket.getOutputStream();
                byte[] msg = input.get().getBytes();
                outputStream.write(msg);
                outputStream.flush();

                InputStream inputStream = socket.getInputStream(); //接收是否已有这个名字
                byte[] buf = new byte[1024];
                int readLen;
                readLen = inputStream.read(buf);
                String has_name = new String(buf, 0, readLen);
                if (has_name.equals("false")){
                    System.out.println(has_name);
                    System.out.println(input.get());
                    username = input.get();
                    currentUsername.setText("Current User: "+username);
                }else{
                    while (has_name.equals("true")){
                        dialog.setContentText("Please change username:");
                        input = dialog.showAndWait();
                        msg = input.get().getBytes();
                        outputStream.write(msg);
                        inputStream = socket.getInputStream(); //接收是否已有这个名字
                        buf = new byte[1024];
                        readLen = inputStream.read(buf);
                        has_name = new String(buf, 0, readLen);
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
    }

    @FXML
    public void createPrivateChat() throws IOException {
        AtomicReference<String> user = new AtomicReference<>();
//        List<AtomicReference<String>> users = new ArrayList<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        OutputStream outputStream = socket.getOutputStream();
        byte[] msg = "Get user names".getBytes();
        outputStream.write(msg);
        outputStream.flush();

        InputStream inputStream = socket.getInputStream(); //接收是否已有这个名字
        byte[] buf = new byte[1024];
        int readLen;
        readLen = inputStream.read(buf);
        String usernames = new String(buf, 0, readLen);
        currentOnlineCnt.setText("Online: "+usernames.split("!").length);
//        List<String> list = Arrays.asList(usernames.split("!"));
        List<String> list = new ArrayList<>();
        for (int i=0;i<usernames.split("!").length;i++){
            if(!Objects.equals(usernames.split("!")[i], username)){
                list.add(usernames.split("!")[i]);
            }
        }
        System.out.println(list);

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll(list);

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
            chatList.getItems().clear();

            chatList.getItems().addAll(chatSet);
            chatList.getItems().addAll(chatGroupSet);
            chatContentList.getItems().add(new Message(System.currentTimeMillis(),username,user.get(),"\uD83D\uDE00"));
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
    public void createGroupChat() throws IOException {
        List<AtomicReference<String>> users = new ArrayList<>();
        List<String> users_ = new ArrayList<>();
        String group_name = "";
        Stage stage = new Stage();
        OutputStream outputStream = socket.getOutputStream();
        byte[] msg = "Get user names".getBytes();
        outputStream.write(msg);
        outputStream.flush();

        InputStream inputStream = socket.getInputStream(); //接收是否已有这个名字
        byte[] buf = new byte[1024];
        int readLen;
        readLen = inputStream.read(buf);
        String usernames = new String(buf, 0, readLen);
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
    public void doSendMessage() {
        current_selected = chatList.getSelectionModel().getSelectedItem();
        System.out.println(inputArea.getText());
        inputArea.clear();

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

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
