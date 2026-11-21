import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Inheritance: a DotCanvas IS-A JavaFX Canvas.
// It turns a PixelArt into glowing round dots: bright cells get big dots, dark cells tiny dots.
public class DotCanvas extends Canvas {

    private final int cell;   // size of one cell in screen pixels

    public DotCanvas(int columns, int rows, int cell) {
        super(columns * cell, rows * cell);
        this.cell = cell;
    }

    public void show(PixelArt art) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#05060f"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        for (int y = 0; y < art.getHeight(); y++) {
            for (int x = 0; x < art.getWidth(); x++) {
                double brightness = art.getBrightness(x, y);
                double radius = cell * (0.14 + 0.38 * Math.sqrt(brightness));
                int c = art.getColor(x, y);
                gc.setFill(Color.rgb((c >> 16) & 255, (c >> 8) & 255, c & 255));
                gc.fillOval(x * cell + cell / 2.0 - radius, y * cell + cell / 2.0 - radius,
                        radius * 2, radius * 2);
            }
        }
    }
}
