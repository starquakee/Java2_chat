package cn.edu.sustech.cs209.chatting.client;

import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Regist 注册
 * @author Administrator
 *
 */
public class fx_test extends Application{
    //网格布局
    GridPane gp = new GridPane();
    Scene scene = new Scene(gp, 250, 300);


    //下拉框 ChoiceBox
    Label l4 = new Label("地址");
    ChoiceBox<String> c = new ChoiceBox<String>();//也是一个 List集合

    //多选框 CheckBox
    Label l5 = new Label("爱好");
    CheckBox c1 = new CheckBox("吃饭");
    CheckBox c2 = new CheckBox("睡觉");
    CheckBox c3 = new CheckBox("打豆豆");
    //三变一
    HBox like = new HBox(c1,c2);





    //按钮
    Button b1 = new Button("注册");
    Button b2 = new Button("取消");

    public void start(Stage stage) throws Exception {


        gp.add(l4, 0, 3);
        gp.add(c, 1, 3);//地址
        c.getItems().addAll("湖南","湖北","四川","广东","河南");//设置下拉选项
        c.setValue("湖南");//默认地址 湖南

        gp.add(l5, 0, 4);
        gp.add(like, 1, 4);//爱好

        gp.add(b1, 0, 5);
        gp.add(b2, 1, 5);//按钮

        //点击事件
        //1、注册
        b1.setOnAction(a->{
            //获取输入值

            String address = c.getValue();//地址
            //字符连接器 StringJoiner
            StringJoiner j = new StringJoiner(",");//美化多选结果
            if(c1.isSelected()) {
                System.out.println(c1.getText());
                j.add(c1.getText());
            }//爱好：吃饭
            if(c2.isSelected()) {
                j.add(c2.getText());
            }//爱好：睡觉
            if(c3.isSelected()) {
                j.add(c3.getText());
            }//爱好：打豆豆
            String hobby = j.toString();//爱好 全部存进来
            //TODO:将数据放到数据库，插入
            //??? 作业  ???

            //打印

//            System.out.println(address);
//            System.out.println(hobby);
        });
        //2、取消
        b2.setOnAction(a->{//要理解 要理解 要理解
            Optional<ButtonType> b = new Alert(AlertType.CONFIRMATION,"你真的要退出嘛 (o°ω°o)",ButtonType.YES,ButtonType.NO).showAndWait();
            if(b.get()==ButtonType.YES) {
                Platform.exit();//关闭界面 （和 X效果相同）
            }
        });

        //设置网格布局
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(15);//水平间距
        gp.setVgap(15);//垂直间距

        stage.setScene(scene);//场景 和舞台 绑定
        stage.show();//显示 舞台
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("c");
        list.add("b");
        Collections.sort(list);
        System.out.println(list);
        Application.launch();
    }
}