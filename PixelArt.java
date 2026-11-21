// A small picture made of coloured cells (a 2D grid of pixels).
// The game draws every cell as a round dot, which gives the dot-matrix "ASCII art" look.
// This class has NO JavaFX code in it: it only stores colours and has drawing methods.
public class PixelArt {

    private final int width;
    private final int height;

    // Colour of every cell, stored as three 2D arrays (value 0 to 255 for each channel).
    private final double[][] red;
    private final double[][] green;
    private final double[][] blue;

    public PixelArt(int width, int height) {
        this.width = width;
        this.height = height;
        red = new double[height][width];
        green = new double[height][width];
        blue = new double[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ---------- Reading cells ----------

    // Returns the colour of a cell as 0xRRGGBB.
    public int getColor(int x, int y) {
        return (toByte(red[y][x]) << 16) | (toByte(green[y][x]) << 8) | toByte(blue[y][x]);
    }

    // Returns how bright a cell is (0.0 = black, 1.0 = brightest). Bright cells become big dots.
    public double getBrightness(int x, int y) {
        return Math.max(red[y][x], Math.max(green[y][x], blue[y][x])) / 255.0;
    }

    // ---------- Colour helpers ----------

    private static int toByte(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }

    // Mixes two colours. t = 0 gives colour a, t = 1 gives colour b.
    public static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    // Makes a colour darker (factor below 1) or brighter (factor above 1).
    public static int shade(int color, double factor) {
        int r = toByte(((color >> 16) & 255) * factor);
        int g = toByte(((color >> 8) & 255) * factor);
        int b = toByte((color & 255) * factor);
        return (r << 16) | (g << 8) | b;
    }

    // A repeatable "random" number between 0 and 1 for a position.
    // The same x and y always give the same value, so the art never flickers.
    public static double noise(double x, double y) {
        double n = Math.sin(x * 12.9898 + y * 78.233) * 43758.5453;
        return n - Math.floor(n);
    }

    // ---------- Drawing ----------

    // Paints one cell. alpha 1 = replace the old colour, 0.5 = half mix, 0 = no change.
    public void paint(int x, int y, int color, double alpha) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        red[y][x] = red[y][x] * (1 - alpha) + ((color >> 16) & 255) * alpha;
        green[y][x] = green[y][x] * (1 - alpha) + ((color >> 8) & 255) * alpha;
        blue[y][x] = blue[y][x] * (1 - alpha) + (color & 255) * alpha;
    }

    // Fills rows y1 to y2 with a gradient from the top colour to the bottom colour.
    public void fillGradient(int y1, int y2, int top, int bottom) {
        for (int y = y1; y <= y2; y++) {
            double t = (y2 == y1) ? 0 : (double) (y - y1) / (y2 - y1);
            int color = mix(top, bottom, t);
            for (int x = 0; x < width; x++) {
                paint(x, y, color, 1);
            }
        }
    }

    public void fillRect(double x1, double y1, double x2, double y2, int color, double alpha) {
        for (int y = (int) Math.ceil(y1); y <= (int) Math.floor(y2); y++) {
            for (int x = (int) Math.ceil(x1); x <= (int) Math.floor(x2); x++) {
                paint(x, y, color, alpha);
            }
        }
    }

    public void fillRect(double x1, double y1, double x2, double y2, int color) {
        fillRect(x1, y1, x2, y2, color, 1);
    }

    public void fillEllipse(double cx, double cy, double rx, double ry, int color, double alpha) {
        for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++) {
            for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++) {
                double dx = (x - cx) / rx;
                double dy = (y - cy) / ry;
                if (dx * dx + dy * dy <= 1) {
                    paint(x, y, color, alpha);
                }
            }
        }
    }

    public void fillEllipse(double cx, double cy, double rx, double ry, int color) {
        fillEllipse(cx, cy, rx, ry, color, 1);
    }

    public void fillCircle(double cx, double cy, double r, int color, double alpha) {
        fillEllipse(cx, cy, r, r, color, alpha);
    }

    public void fillCircle(double cx, double cy, double r, int color) {
        fillEllipse(cx, cy, r, r, color, 1);
    }

    // Only the outline of a circle, "thickness" cells wide.
    public void drawRing(double cx, double cy, double radius, double thickness, int color, double alpha) {
        int reach = (int) Math.ceil(radius + thickness);
        for (int y = (int) cy - reach; y <= (int) cy + reach; y++) {
            for (int x = (int) cx - reach; x <= (int) cx + reach; x++) {
                double d = Math.hypot(x - cx, y - cy);
                if (Math.abs(d - radius) <= thickness / 2) {
                    paint(x, y, color, alpha);
                }
            }
        }
    }

    public void fillTriangle(double x1, double y1, double x2, double y2,
                             double x3, double y3, int color, double alpha) {
        int minX = (int) Math.floor(Math.min(x1, Math.min(x2, x3)));
        int maxX = (int) Math.ceil(Math.max(x1, Math.max(x2, x3)));
        int minY = (int) Math.floor(Math.min(y1, Math.min(y2, y3)));
        int maxY = (int) Math.ceil(Math.max(y1, Math.max(y2, y3)));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double d1 = side(x, y, x1, y1, x2, y2);
                double d2 = side(x, y, x2, y2, x3, y3);
                double d3 = side(x, y, x3, y3, x1, y1);
                boolean hasNegative = d1 < 0 || d2 < 0 || d3 < 0;
                boolean hasPositive = d1 > 0 || d2 > 0 || d3 > 0;
                if (!(hasNegative && hasPositive)) {   // inside = all on the same side
                    paint(x, y, color, alpha);
                }
            }
        }
    }

    public void fillTriangle(double x1, double y1, double x2, double y2,
                             double x3, double y3, int color) {
        fillTriangle(x1, y1, x2, y2, x3, y3, color, 1);
    }

    private static double side(double px, double py, double ax, double ay, double bx, double by) {
        return (px - bx) * (ay - by) - (ax - bx) * (py - by);
    }

    // A thick line: every cell closer than half the thickness to the line is painted.
    public void drawLine(double x1, double y1, double x2, double y2,
                         double thickness, int color, double alpha) {
        double half = thickness / 2;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;
        for (int y = (int) Math.floor(Math.min(y1, y2) - half); y <= Math.ceil(Math.max(y1, y2) + half); y++) {
            for (int x = (int) Math.floor(Math.min(x1, x2) - half); x <= Math.ceil(Math.max(x1, x2) + half); x++) {
                double t = (lengthSquared == 0) ? 0 : ((x - x1) * dx + (y - y1) * dy) / lengthSquared;
                t = Math.max(0, Math.min(1, t));
                double distance = Math.hypot(x - (x1 + t * dx), y - (y1 + t * dy));
                if (distance <= half) {
                    paint(x, y, color, alpha);
                }
            }
        }
    }

    public void drawLine(double x1, double y1, double x2, double y2, double thickness, int color) {
        drawLine(x1, y1, x2, y2, thickness, color, 1);
    }

    // ---------- Lighting and effects ----------

    // Adds soft light around a point (torches, the moon, magic). The light fades with distance.
    public void addGlow(double cx, double cy, double radius, int color, double strength) {
        for (int y = (int) Math.floor(cy - radius); y <= (int) Math.ceil(cy + radius); y++) {
            for (int x = (int) Math.floor(cx - radius); x <= (int) Math.ceil(cx + radius); x++) {
                if (x < 0 || y < 0 || x >= width || y >= height) {
                    continue;
                }
                double distance = Math.hypot(x - cx, y - cy);
                if (distance < radius) {
                    double f = 1 - distance / radius;
                    f = f * f * strength;
                    red[y][x] = Math.min(255, red[y][x] + ((color >> 16) & 255) * f);
                    green[y][x] = Math.min(255, green[y][x] + ((color >> 8) & 255) * f);
                    blue[y][x] = Math.min(255, blue[y][x] + (color & 255) * f);
                }
            }
        }
    }

    // Randomly scatters single dots (stars, embers, sparkles) inside a rectangle.
    // A higher "threshold" (closer to 1) gives fewer dots.
    public void sprinkle(int x1, int y1, int x2, int y2, double threshold, int color, int seed) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (noise(x * 1.3 + seed, y * 1.7 + seed) > threshold) {
                    paint(x, y, color, 0.4 + 0.6 * noise(x + 7, y + seed));
                }
            }
        }
    }

    // Makes a rectangle slightly lighter or darker per cell, so flat areas look rough like stone.
    public void addNoise(int x1, int y1, int x2, int y2, double amount) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (x < 0 || y < 0 || x >= width || y >= height) {
                    continue;
                }
                double factor = 1 + (noise(x, y) - 0.5) * amount;
                red[y][x] *= factor;
                green[y][x] *= factor;
                blue[y][x] *= factor;
            }
        }
    }

    // Darkens the corners of the picture so attention goes to the middle.
    public void vignette(double strength) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = (x - width / 2.0) / (width / 2.0);
                double dy = (y - height / 2.0) / (height / 2.0);
                double factor = Math.max(0, 1 - strength * (dx * dx + dy * dy) / 2);
                red[y][x] *= factor;
                green[y][x] *= factor;
                blue[y][x] *= factor;
            }
        }
    }

    // Copies everything above waterY upside-down below it, dimmed, with a small ripple.
    // squash above 1 squeezes the reflection so more of the scene fits into the water.
    public void reflect(int waterY, double alpha, double squash) {
        for (int y = waterY; y < height; y++) {
            int sourceY = waterY - 1 - (int) Math.round((y - waterY) * squash);
            if (sourceY < 0) {
                break;
            }
            double fade = 1 - 0.6 * (y - waterY) / (double) (height - waterY);
            for (int x = 0; x < width; x++) {
                int sourceX = x + (int) Math.round(Math.sin(y * 1.3) * 1.2);
                if (sourceX < 0 || sourceX >= width) {
                    sourceX = x;
                }
                paint(x, y, getColor(sourceX, sourceY), alpha * fade);
            }
        }
    }
}
