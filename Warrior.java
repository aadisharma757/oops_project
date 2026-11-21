
// Inheritance: Warrior IS-A GameCharacter.
public class Warrior extends GameCharacter {

    public Warrior(String name) {
        super(name);
    }

    @Override
    public String getClassName() {
        return "Warrior";
    }

    @Override
    public PixelArt getArt() {
        return AsciiArt.warrior();
    }

    // Warrior power: bonus XP.
    @Override
    public String usePower(Puzzle puzzle) {
        gainXp(20);
        return "Battle Cry! You gain 20 bonus XP.";
    }
}