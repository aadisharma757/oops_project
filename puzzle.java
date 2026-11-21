
// Interface: every room in the dungeon has one puzzle that follows this contract.
public interface Puzzle {

    String getTitle();      // name of the room, e.g. "The Riddle Door"

    String getQuestion();   // text shown to the player

    String getHint();       // used by the Thief's power

    String getReward();     // power gained for solving the puzzle

    // Does nothing if the answer is right, throws an exception if it is wrong.
    void checkAnswer(String input) throws WrongAnswerException;
}