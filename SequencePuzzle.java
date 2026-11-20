// Room 3: find the next number in a pattern.
// Inheritance: it reuses the checking logic of MathPuzzle and only changes
// the title and how the question is built (polymorphism via overriding).
public class SequencePuzzle extends MathPuzzle {

    public SequencePuzzle(int[] sequence, int answer, String hint, String reward) {
        super(buildQuestion(sequence), answer, hint, reward);
    }

    // Uses an array of numbers to build the question text.
    private static String buildQuestion(int[] sequence) {
        String text = "What comes next in the pattern?\n\n";
        for (int number : sequence) {
            text += number + ", ";
        }
        return text + "?";
    }

    @Override
    public String getTitle() {
        return "The Final Gate";
    }
}