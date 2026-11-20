// Inheritance: Thief IS-A Hero.
public class Thief extends Hero {

    public Thief(String name) {
        super(name);
    }

    @Override
    public String getClassName() {
        return "Thief";
    }

    @Override
    public String getArt() {
        return AsciiArt.THIEF;
    }

    // Thief power: reveals the hint of the current puzzle.
    @Override
    public String usePower(Puzzle puzzle) {
        return "Sneaky Hint: " + puzzle.getHint();
    }
}