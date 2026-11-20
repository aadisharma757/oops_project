import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// The JavaFX window: start screen, room screen and end screen.
public class Main extends Application {

    private static final int TOTAL_TIME = 180;   // 3 minutes for the whole dungeon
    private static final int XP_PER_ROOM = 50;
    private static final int WRONG_PENALTY = 10; // seconds lost for a wrong answer

    private Stage stage;
    private Hero player;                                    // polymorphism: Warrior or Thief
    private ArrayList<Puzzle> rooms = new ArrayList<>();    // the 3 rooms
    private int currentRoom;
    private boolean powerUsed;
    private TimerThread timer;

    // Labels that change while playing
    private Label timerLabel;
    private Label roomLabel;
    private Label doorLabel;
    private Label questionLabel;
    private Label messageLabel;
    private Label statusLabel;
    private TextField answerField;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Dungeon Escape");
        setupRooms();
        showStartScreen();
        stage.show();
    }

    // ---------- Setup ----------

    private void setupRooms() {
        rooms.add(new RiddlePuzzle(
                "I have keys but open no locks.\nI have space but no room.\n"
                        + "You can enter, but never leave.\nWhat am I?",
                "keyboard",
                "You are probably using one right now.",
                "Torch of Insight"));

        rooms.add(new MathPuzzle(
                "The guard asks:\nWhat is 12 x 7 - 9 ?",
                75,
                "12 x 7 is 84.",
                "Shield of Focus"));

        int[] pattern = {2, 4, 8, 16};
        rooms.add(new SequencePuzzle(
                pattern,
                32,
                "Each number is double the one before it.",
                "Crown of the Dungeon"));
    }

    // ---------- Screens ----------

    private void showStartScreen() {
        VBox root = makeRoot();

        Label title = makeLabel(AsciiArt.box("D U N G E O N   E S C A P E",
                "Solve 3 puzzles before time runs out!"), 16);

        TextField nameField = makeTextField();
        nameField.setPromptText("Enter your hero name");
        nameField.setMaxWidth(250);

        Button warriorButton = makeButton("Play as Warrior");
        Button thiefButton = makeButton("Play as Thief");

        // Event handling: each button creates a different Hero object.
        warriorButton.setOnAction(e -> startGame(new Warrior(readName(nameField))));
        thiefButton.setOnAction(e -> startGame(new Thief(readName(nameField))));

        VBox warriorBox = new VBox(10, makeLabel(AsciiArt.WARRIOR, 14),
                makeLabel("Power: Battle Cry (+20 XP)", 12), warriorButton);
        VBox thiefBox = new VBox(10, makeLabel(AsciiArt.THIEF, 14),
                makeLabel("Power: Sneaky Hint", 12), thiefButton);
        warriorBox.setAlignment(Pos.CENTER);
        thiefBox.setAlignment(Pos.CENTER);

        HBox choices = new HBox(60, warriorBox, thiefBox);
        choices.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, nameField, choices);
        stage.setScene(new Scene(root, 800, 640));
    }

    private void startGame(Hero chosenHero) {
        player = chosenHero;
        currentRoom = 0;

        stage.setScene(new Scene(buildRoomScreen(), 800, 700));
        loadRoom();

        // Start the countdown on a separate thread.
        timer = new TimerThread(TOTAL_TIME, timerLabel, this::gameOver);
        timer.setDaemon(true);   // thread stops when the window closes
        timer.start();
    }

    private VBox buildRoomScreen() {
        VBox root = makeRoot();

        timerLabel = makeLabel("TIME LEFT: 03:00", 18);
        roomLabel = makeLabel("", 16);
        doorLabel = makeLabel("", 12);
        questionLabel = makeLabel("", 15);
        messageLabel = makeLabel("", 14);
        statusLabel = makeLabel("", 13);

        answerField = makeTextField();
        answerField.setPromptText("Type your answer here");
        answerField.setMaxWidth(300);
        answerField.setOnAction(e -> handleSubmit());   // pressing Enter also submits

        Button submitButton = makeButton("Submit");
        Button powerButton = makeButton("Use Power");
        submitButton.setOnAction(e -> handleSubmit());
        powerButton.setOnAction(e -> handlePower());

        HBox pictures = new HBox(50, makeLabel(player.getArt(), 12), doorLabel);
        pictures.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(20, submitButton, powerButton);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(timerLabel, roomLabel, pictures, questionLabel,
                answerField, buttons, messageLabel, statusLabel);
        return root;
    }

    private void showEndScreen(boolean won) {
        timer.stopTimer();
        VBox root = makeRoot();

        Label art = makeLabel(won ? AsciiArt.TROPHY : AsciiArt.SKULL, 14);
        Label heading = makeLabel(won
                ? AsciiArt.box("YOU ESCAPED THE DUNGEON!")
                : AsciiArt.box("TIME IS UP - GAME OVER"), 16);
        Label summary = makeLabel(buildStatusText(), 14);

        Button playAgain = makeButton("Play Again");
        playAgain.setOnAction(e -> showStartScreen());

        root.getChildren().addAll(art, heading, summary, playAgain);
        stage.setScene(new Scene(root, 800, 640));
    }

    // ---------- Game logic ----------

    private void loadRoom() {
        Puzzle room = rooms.get(currentRoom);
        powerUsed = false;

        roomLabel.setText("ROOM " + (currentRoom + 1) + " of " + rooms.size()
                + " - " + room.getTitle());
        doorLabel.setText(AsciiArt.door(currentRoom + 1));
        questionLabel.setText(room.getQuestion());
        answerField.clear();
        messageLabel.setText("");
        updateStatus();
    }

    private void handleSubmit() {
        Puzzle room = rooms.get(currentRoom);
        try {
            room.checkAnswer(answerField.getText());   // throws if wrong

            // Reaching this line means the answer was correct.
            String message = ">> Correct! You gained: " + room.getReward();
            player.addPower(room.getReward());
            if (player.gainXp(XP_PER_ROOM)) {
                message += "   *** LEVEL UP! ***";
            }

            currentRoom++;
            if (currentRoom == rooms.size()) {
                showEndScreen(true);
            } else {
                loadRoom();
                messageLabel.setText(message);
            }
        } catch (WrongAnswerException e) {
            timer.subtractTime(WRONG_PENALTY);
            messageLabel.setText(">> " + e.getMessage() + " (-" + WRONG_PENALTY + " seconds)");
        }
        if (currentRoom < rooms.size()) {
            updateStatus();
        }
    }

    private void handlePower() {
        if (powerUsed) {
            messageLabel.setText(">> You already used your power in this room.");
            return;
        }
        powerUsed = true;
        // Polymorphism: the same call behaves differently for Warrior and Thief.
        messageLabel.setText(">> " + player.usePower(rooms.get(currentRoom)));
        updateStatus();
    }

    // Called by the timer thread (through the UI thread) when time runs out.
    private void gameOver() {
        if (currentRoom < rooms.size()) {
            showEndScreen(false);
        }
    }

    // ---------- Helper methods ----------

    private void updateStatus() {
        statusLabel.setText(buildStatusText());
    }

    private String buildStatusText() {
        String powers = player.getPowers().isEmpty() ? "none" : player.getPowers().toString();
        return player.getName() + " the " + player.getClassName()
                + "\nLEVEL " + player.getLevel() + "   XP " + xpBar()
                + " " + player.getXp() + "/" + Hero.XP_TO_LEVEL
                + "\nPOWERS: " + powers;
    }

    // Draws the XP bar in ASCII, e.g. [#####-----]
    private String xpBar() {
        int filled = player.getXp() / 10;
        return "[" + "#".repeat(filled) + "-".repeat(10 - filled) + "]";
    }

    private String readName(TextField field) {
        String name = field.getText().trim();
        return name.isEmpty() ? "Hero" : name;
    }

    private VBox makeRoot() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: black;");
        return root;
    }

    private Label makeLabel(String text, int size) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", size));
        label.setStyle("-fx-text-fill: #33ff33;");
        return label;
    }

    private Button makeButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Monospaced", 14));
        button.setStyle("-fx-background-color: #003300; -fx-text-fill: #33ff33;"
                + " -fx-border-color: #33ff33;");
        return button;
    }

    private TextField makeTextField() {
        TextField field = new TextField();
        field.setFont(Font.font("Monospaced", 14));
        field.setStyle("-fx-background-color: black; -fx-text-fill: #33ff33;"
                + " -fx-border-color: #33ff33; -fx-prompt-text-fill: #1a8a1a;");
        return field;
    }

    public static void main(String[] args) {
        launch(args);
    }
}