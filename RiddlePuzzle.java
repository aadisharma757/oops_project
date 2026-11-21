

// Room 1: a text riddle.
public class RiddlePuzzle implements Puzzle {

    private String question;
    private String answer;
    private String hint;
    private String reward;

    public RiddlePuzzle(String question, String answer, String hint, String reward) {
        this.question = question;
        this.answer = answer;
        this.hint = hint;
        this.reward = reward;
    }

    @Override
    public String getTitle() {
        return "The Riddle Door";
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
        if (!answer.equalsIgnoreCase(input.trim())) {
            throw new WrongAnswerException("Wrong answer! The door stays shut.");
        }
    }
}
