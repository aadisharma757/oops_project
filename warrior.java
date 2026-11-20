// Inheritance: Warrior IS-A Hero.
public class Warrior extends Hero {

    public Warrior(String name) {
        super(name);
    }

    @Override
    public String getClassName() {
        return "Warrior";
    }

    @Override
    public String getArt() {
        return AsciiArt.WARRIOR;
    }

    // Warrior power: bonus XP.
    @Override
    public String usePower(Puzzle puzzle) {
        gainXp(20);
        return "Battle Cry! You gain 20 bonus XP.";
    }
}