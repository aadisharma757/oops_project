
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
    private static final int CELL = 4;           // screen pixels per art cell

    private static final String BACKGROUND = "#05060f";
    private static final String TEXT_COLOR = "#ffc9a0";
    private static final String ACCENT = "#ff8a5c";

    private Stage stage;
    private GameCharacter player;                           // polymorphism: Warrior or Thief
    private ArrayList<Puzzle> rooms = new ArrayList<>();    // the 3 rooms
    private int currentRoom;
    private boolean powerUsed;
    private TimerThread timer;

    // Things on the room screen that change while playing
    private DotCanvas roomCanvas;
    private Label timerLabel;
    private Label roomLabel;
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

        DotCanvas banner = new DotCanvas(AsciiArt.WIDE_W, AsciiArt.TITLE_H, CELL);
        banner.show(AsciiArt.title());

        Label title = makeLabel(AsciiArt.box("D U N G E O N   E S C A P E",
                "Solve 3 puzzles before time runs out!"), 14);

        TextField nameField = makeTextField();
        nameField.setPromptText("Enter your hero name");
        nameField.setMaxWidth(250);

        Button warriorButton = makeButton("Play as Warrior");
        Button thiefButton = makeButton("Play as Thief");

        // Event handling: each button creates a different character object.
        warriorButton.setOnAction(e -> startGame(new Warrior(readName(nameField))));
        thiefButton.setOnAction(e -> startGame(new Thief(readName(nameField))));

        HBox choices = new HBox(60,
                makeChoice(AsciiArt.warrior(), "Power: Battle Cry (+20 XP)", warriorButton),
                makeChoice(AsciiArt.thief(), "Power: Sneaky Hint", thiefButton));
        choices.setAlignment(Pos.CENTER);

        root.getChildren().addAll(banner, title, nameField, choices);
        stage.setScene(new Scene(root, 800, 720));
    }

    // One character card: picture, short description and the button.
    private VBox makeChoice(PixelArt art, String description, Button button) {
        DotCanvas picture = new DotCanvas(AsciiArt.PORTRAIT_W, AsciiArt.PORTRAIT_H, CELL);
        picture.show(art);
        VBox box = new VBox(8, picture, makeLabel(description, 12), button);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void startGame(GameCharacter chosen) {
        player = chosen;
        currentRoom = 0;

        stage.setScene(new Scene(buildRoomScreen(), 880, 700));
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

        // The hero picture is drawn once, the room picture changes in loadRoom().
        DotCanvas heroCanvas = new DotCanvas(AsciiArt.PORTRAIT_W, AsciiArt.PORTRAIT_H, 3);
        heroCanvas.show(player.getArt());
        roomCanvas = new DotCanvas(AsciiArt.WIDE_W, AsciiArt.ROOM_H, CELL);

        HBox pictures = new HBox(30, heroCanvas, roomCanvas);
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

        DotCanvas art = new DotCanvas(AsciiArt.PORTRAIT_W, AsciiArt.PORTRAIT_H, 5);
        art.show(won ? AsciiArt.victory() : AsciiArt.defeat());

        Label heading = makeLabel(won
                ? AsciiArt.box("YOU ESCAPED THE DUNGEON!")
                : AsciiArt.box("TIME IS UP - GAME OVER"), 16);
        Label summary = makeLabel(buildStatusText(), 14);

        Button playAgain = makeButton("Play Again");
        playAgain.setOnAction(e -> showStartScreen());

        root.getChildren().addAll(art, heading, summary, playAgain);
        stage.setScene(new Scene(root, 800, 720));
    }

    // ---------- Game logic ----------

    private void loadRoom() {
        Puzzle room = rooms.get(currentRoom);
        powerUsed = false;

        roomLabel.setText("ROOM " + (currentRoom + 1) + " of " + rooms.size()
                + " - " + room.getTitle());
        roomCanvas.show(AsciiArt.room(currentRoom + 1));
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
                + " " + player.getXp() + "/" + GameCharacter.XP_TO_LEVEL
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
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");
        return root;
    }

    private Label makeLabel(String text, int size) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", size));
        label.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        return label;
    }

    private Button makeButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Monospaced", 14));
        button.setStyle("-fx-background-color: #1a1030; -fx-text-fill: " + ACCENT + ";"
                + " -fx-border-color: " + ACCENT + ";");
        return button;
    }

    private TextField makeTextField() {
        TextField field = new TextField();
        field.setFont(Font.font("Monospaced", 14));
        field.setStyle("-fx-background-color: #0c0a1c; -fx-text-fill: " + TEXT_COLOR + ";"
                + " -fx-border-color: " + ACCENT + "; -fx-prompt-text-fill: #8a5a48;");
        return field;
    }

    public static void main(String[] args) {
        launch(args);
    }
}