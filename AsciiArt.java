public // Holds all the ASCII art used by the game.
public class AsciiArt {

    public static final String WARRIOR = """
               ,====.         /\\
              | o  o |       /  \\
              |  ()  |       |  |
              '-.__.-'       |  |
             /|######|\\      |  |
            / |######| \\     |  |
              |######|      -+--+-
              |  ||  |        ||
             _|  ||  |_""";

    public static final String THIEF = """
                ,---.
               / ~ ~ \\        -+-
              |  o o  |        |
               \\  _  /         |
              __)---(__        v
             /  | $ |  \\
            (   |   |   )
                |   |
               _|   |_""";

    public static final String TROPHY = """
               ___________
              |  _______  |
             (| |       | |)
              | |  WIN  | |
               \\ |_____| /
                '-.___.-'
                   | |
                  _| |_
                 |_____|""";

    public static final String SKULL = """
                 _______
               .'       '.
              /   O   O   \\
             |      ^      |
              \\   |||||   /
               '.  ---  .'
                 '-----'""";

    // Template for a door. %-3s is replaced by the room number in Roman numerals.
    private static final String DOOR = """
               _______________
              |  ___________  |
              | |           | |
              | |    %-3s    | |
              | |           | |
              | |         o | |
              | |___________| |
              |_______________|""";

    // Returns the door picture for room 1, 2 or 3.
    public static String door(int roomNumber) {
        String[] romans = {"I", "II", "III"};
        return DOOR.formatted(romans[roomNumber - 1]);
    }

    // Draws a text frame around one or more lines.
    public static String box(String... lines) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        String border = "+" + "-".repeat(width + 2) + "+";
        StringBuilder sb = new StringBuilder(border + "\n");
        for (String line : lines) {
            sb.append("| ").append(line);
            sb.append(" ".repeat(width - line.length())).append(" |\n");
        }
        sb.append(border);
        return sb.toString();
    }
} {
    
}
