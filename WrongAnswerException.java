 // Custom checked exception.
// A puzzle throws it when the player's answer is wrong.
public class WrongAnswerException extends Exception {

    public WrongAnswerException(String message) {
        super(message);
    }
} 