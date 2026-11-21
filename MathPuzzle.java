 // Room 2: a number puzzle.
public class MathPuzzle implements Puzzle {

    private String question;
    private int answer;
    private String hint;
    private String reward;

    public MathPuzzle(String question, int answer, String hint, String reward) {
        this.question = question;
        this.answer = answer;
        this.hint = hint;
        this.reward = reward;
    }

    @Override
    public String getTitle() {
        return "The Guard's Lock";
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public String getHint() {
        return hint;
    }

    @Override
    public String getReward() {
        return reward;
    }

    @Override
    public void checkAnswer(String input) throws WrongAnswerException {
        try {
            int value = Integer.parseInt(input.trim());
            if (value != answer) {
                throw new WrongAnswerException("Wrong number! The lock won't move.");
            }
        } catch (NumberFormatException e) {
            // The player typed letters or left the box empty.
            throw new WrongAnswerException("That is not a number!");
        }
    }
} 
    
