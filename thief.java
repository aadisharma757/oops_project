
// Inheritance: Thief IS-A GameCharacter.
public class Thief extends GameCharacter {

    public Thief(String name) {
        super(name);
    }

    @Override
    public String getClassName() {
        return "Thief";
    }

    @Override
    public PixelArt getArt() {
        return AsciiArt.thief();
    }

    // Thief power: reveals the hint of the current puzzle.
    @Override
    public String usePower(Puzzle puzzle) {
        return "Sneaky Hint: " + puzzle.getHint();
    }
}