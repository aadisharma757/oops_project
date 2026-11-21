
import java.util.ArrayList;

// Abstract class: the base for every playable character.
// It cannot be created directly, only through Warrior or Thief.
// usePower() comes from the Powerable interface and is left for the subclasses.
public abstract class GameCharacter implements Powerable {

    public static final int XP_TO_LEVEL = 100;

    // Encapsulation: fields are private, access is through methods only.
    private String name;
    private int xp;
    private int level;
    private ArrayList<String> powers;   // ArrayList of earned powers

    // Constructor sets the starting values.
    public GameCharacter(String name) {
        this.name = name;
        this.xp = 0;
        this.level = 1;
        this.powers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public ArrayList<String> getPowers() {
        return powers;
    }

    public void addPower(String power) {
        powers.add(power);
    }

    // Adds XP. Returns true if the character levelled up.
    public boolean gainXp(int amount) {
        xp += amount;
        boolean leveledUp = false;
        while (xp >= XP_TO_LEVEL) {
            levelUp();
            leveledUp = true;
        }
        return leveledUp;
    }

    private void levelUp() {
        xp -= XP_TO_LEVEL;
        level++;
    }

    // Each subclass supplies its own values.
    public abstract String getClassName();

    public abstract PixelArt getArt();
}