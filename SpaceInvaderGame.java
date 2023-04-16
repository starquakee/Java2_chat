import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpaceInvaderGame extends Application {

    private Pane root = new Pane();


    private Sprite player = new Sprite(300, 650, 40, 40, "player", Color.BLUE);
    private Sprite e1;
    private Sprite e2;
    private Sprite e3;
    private Sprite e4;
    private Sprite e5;

    private Parent createContent() {
        root.setPrefSize(600, 700);

        root.getChildren().add(player);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                System.out.println(now);
                update();
            }
        };

        timer.start();

        createEnemies();

        return root;
    }

    private void createEnemies() {
        for (int i = 0; i < 5; i++) {
            Sprite s = new Sprite(90 + i*100, 150, 30, 30, "enemy", Color.RED);
            if (i==0){
                e1=s;
            }else if (i==1){
                e2=s;
            }else if (i==2){
                e3=s;
            }else if (i==3){
                e4=s;
            }else {
                e5=s;
            }

            root.getChildren().add(s);
        }
    }

    private List<Sprite> sprites() {
        return root.getChildren().stream().map(n -> (Sprite)n).collect(Collectors.toList());
    }

    private void update() {

        sprites().forEach(s -> {
            switch (s.type) {

                case "enemybullet":
                    // enemy's bullet moves down
                    s.moveDown();

                    // enemy's bullet hits the player
                    if (s.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        player.dead = true; // player is dead
                        s.dead = true; // bullet is dead
                    }

                    break;

                case "playerbullet":
                    s.moveUp();

                    // enemy's bullet hits the player
                    if (s.getBoundsInParent().intersects(e1.getBoundsInParent())) {
                        e1.dead = true;
                        s.dead = true; // bullet is dead
                    }else if (s.getBoundsInParent().intersects(e2.getBoundsInParent())) {
                        e2.dead = true;
                        s.dead = true; // bullet is dead
                    }else if (s.getBoundsInParent().intersects(e3.getBoundsInParent())) {
                        e3.dead = true;
                        s.dead = true; // bullet is dead
                    }else if (s.getBoundsInParent().intersects(e4.getBoundsInParent())) {
                        e4.dead = true;
                        s.dead = true; // bullet is dead
                    }else if (s.getBoundsInParent().intersects(e5.getBoundsInParent())) {
                        e5.dead = true;
                        s.dead = true; // bullet is dead
                    }

                    // TODO player's bullet should move up
                    // TODO should also check whether the bullet hits each enemy

                    break;

                case "enemy":
                    // TODO enemies should shoot with random intervals

                    if(new Random().nextInt(100)<=1){
                        shoot(s);
                    }
                    break;


                case "player":
                    shoot(s);


                    break;
            }
        });

        // remove dead sprites from the screen
        root.getChildren().removeIf(n -> {
            Sprite s = (Sprite) n;
            return s.dead;
        });

    }

    private void shoot(Sprite who) {
        if(who.dead){
            return;
        }
        // a rectangle with width 5, which looks like a bullet
        Sprite s = new Sprite((int) who.getTranslateX() + 20, (int) who.getTranslateY(), 5, 20, who.type + "bullet", Color.BLACK);

        root.getChildren().add(s);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT:
                    player.moveLeft();
                    break;
                case RIGHT:
                    player.moveRight();
                    break;
                case UP:
                    player.moveUp();
                    break;
                case DOWN:
                    player.moveDown();
                    break;
                case SPACE:
                    shoot(player);
                    break;
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    private static class Sprite extends Rectangle {
        boolean dead = false;
        final String type;

        Sprite(int x, int y, int w, int h, String type, Color color) {
            super(w, h, color);

            this.type = type;
            setTranslateX(x);
            setTranslateY(y);
        }

        void moveLeft() {
            setTranslateX(getTranslateX() - 5);
        }

        void moveRight() {
            setTranslateX(getTranslateX() + 5);
        }

        void moveUp() {
            setTranslateY(getTranslateY() - 5);
        }

        void moveDown() {
            setTranslateY(getTranslateY() + 5);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}