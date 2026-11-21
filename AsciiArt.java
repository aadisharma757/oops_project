

            // All the pictures in the game. Each method builds a PixelArt by drawing simple shapes
// (rectangles, circles, triangles, lines) and adding light (glow). Nothing is loaded from files.
// Rendered by DotCanvas as glowing dots, like dot-matrix ASCII art.
public class AsciiArt {

    public static final int WIDE_W = 140;      // size of the wide pictures (title and rooms)
    public static final int ROOM_H = 56;
    public static final int TITLE_H = 40;
    public static final int PORTRAIT_W = 56;   // size of the character pictures
    public static final int PORTRAIT_H = 64;

    private static final int GOLD = 0xe8b830;
    private static final int GOLD_LIGHT = 0xffe27a;
    private static final int STEEL = 0x7c86a6;
    private static final int STEEL_DARK = 0x464e6a;
    private static final int STEEL_LIGHT = 0xb4bedc;
    private static final int STONE = 0x2e2c48;
    private static final int TREE = 0x03030a;

    // =====================================================================
    //  TITLE SCREEN: mountains, moon and a glowing dungeon entrance by a lake
    // =====================================================================
    public static PixelArt title() {
        PixelArt p = new PixelArt(WIDE_W, TITLE_H);
        int horizon = 26;

        p.fillGradient(0, horizon, 0x04050e, 0x5a2a4c);          // night sky, warmer at the horizon
        p.sprinkle(0, 0, WIDE_W - 1, 14, 0.978, 0xffffff, 3);    // stars
        p.addGlow(104, 9, 28, 0xff9a70, 0.7);                    // moon glow
        p.fillCircle(104, 9, 4.5, 0xfff1d6);                     // moon

        mountain(p, 118, 8, horizon, 30);
        mountain(p, 14, 9, horizon, 22);
        mountain(p, 82, 12, horizon, 21);
        mountain(p, 44, 3, horizon, 32);                         // the big one in front
        p.addGlow(104, horizon, 50, 0xff7a50, 0.45);             // warm light on the horizon

        // dungeon entrance at the foot of the big mountain
        p.fillRect(39, 19, 53, horizon, 0x120a10);
        p.fillCircle(46, 19, 7, 0x120a10);
        p.fillRect(41, 19, 51, horizon, 0xff9a30);
        p.fillCircle(46, 19, 5, 0xff9a30);
        p.fillRect(44, 20, 48, horizon, 0xfff0b0, 0.7);
        p.addGlow(46, 22, 14, 0xff8a30, 0.8);

        // lake with reflections
        p.fillGradient(horizon + 1, TITLE_H - 1, 0x141840, 0x080a1c);
        p.reflect(horizon + 1, 0.85, 1.9);
        p.sprinkle(0, horizon + 2, WIDE_W - 1, TITLE_H - 1, 0.95, 0xaab4ff, 11);

        // pine trees in front (dark silhouettes)
        pine(p, 7, 8, TITLE_H - 1, 10);
        pine(p, 21, 20, TITLE_H - 3, 6);
        pine(p, 133, 6, TITLE_H - 1, 11);
        pine(p, 120, 19, TITLE_H - 3, 6);

        p.vignette(0.5);
        return p;
    }

    // A mountain: a dark triangle whose top is lit peach by the moon.
    private static void mountain(PixelArt p, double apexX, double apexY, int baseY, double halfBase) {
        p.fillTriangle(apexX, apexY, apexX - halfBase, baseY, apexX + halfBase, baseY, 0x1c1c42);
        double height = baseY - apexY;
        double[] depths = {0.6, 0.47, 0.34, 0.21, 0.1};   // overlapping caps make a soft fade
        for (double depth : depths) {
            double half = halfBase * depth;
            p.fillTriangle(apexX, apexY, apexX - half, apexY + height * depth,
                    apexX + half, apexY + height * depth, 0xf4a283, 0.4);
        }
    }

    // A pine tree made of stacked triangles.
    private static void pine(PixelArt p, int x, int top, int bottom, int halfWidth) {
        int layers = 4;
        double step = (bottom - top) / (double) (layers + 1);
        for (int i = 0; i < layers; i++) {
            double y0 = top + i * step;
            double w = halfWidth * (i + 1) / (double) layers;
            p.fillTriangle(x, y0, x - w, y0 + step * 1.9, x + w, y0 + step * 1.9, TREE);
        }
        p.fillRect(x - 1, bottom - step, x + 1, bottom, TREE);
    }

    // =====================================================================
    //  ROOMS: a stone chamber with torches and a different door in each room
    // =====================================================================
    public static PixelArt room(int number) {
        PixelArt p = new PixelArt(WIDE_W, ROOM_H);
        int cx = 70;          // centre of the door
        int topY = 27;        // where the straight part of the door ends and the arch begins
        int bottomY = 50;     // floor line
        int halfW = 15;

        brickWall(p, 0, bottomY - 1);
        p.fillGradient(bottomY, ROOM_H - 1, 0x1a1828, 0x08070f);   // floor
        p.addNoise(0, bottomY, WIDE_W - 1, ROOM_H - 1, 0.5);

        // stone frame around the door
        p.fillRect(cx - halfW - 3, topY, cx + halfW + 3, bottomY, 0x55536f);
        p.fillCircle(cx, topY, halfW + 3, 0x55536f);
        p.fillRect(cx - halfW - 3, topY, cx - halfW - 2, bottomY, 0x7a789a, 0.7);   // lit edge

        int lightColor;
        int gemColor;
        if (number == 1) {
            woodDoor(p, cx, topY, bottomY, halfW);
            lightColor = 0xffa030;
            gemColor = 0xffc860;
        } else if (number == 2) {
            ironDoor(p, cx, topY, bottomY, halfW);
            lightColor = 0x4a8aff;
            gemColor = 0x60d0ff;
        } else {
            portalDoor(p, cx, topY, bottomY, halfW);
            lightColor = 0xb060ff;
            gemColor = 0xff7aff;
        }
        p.drawRing(cx, topY, halfW, 1.6, 0x000000, 0.45);            // shadow inside the arch

        // one gem above the door per room number
        for (int i = 0; i < number; i++) {
            gem(p, cx + (i - (number - 1) / 2.0) * 9, 5, gemColor);
        }

        torch(p, 38, 24);
        torch(p, 102, 24);
        p.addGlow(cx, bottomY + 3, 34, lightColor, 0.4);             // light spilling on the floor
        p.vignette(0.55);
        return p;
    }

    private static void brickWall(PixelArt p, int y1, int y2) {
        int brickH = 5;
        int brickW = 12;
        for (int y = y1; y <= y2; y++) {
            int row = (y - y1) / brickH;
            int offset = (row % 2) * (brickW / 2);
            for (int x = 0; x < WIDE_W; x++) {
                boolean mortar = (y - y1) % brickH == 0 || (x + offset) % brickW == 0;
                int brick = (x + offset) / brickW;
                if (mortar) {
                    p.paint(x, y, 0x0b0a16, 1);
                } else {
                    double variation = 0.7 + 0.6 * PixelArt.noise(brick, row);
                    p.paint(x, y, PixelArt.shade(STONE, variation), 1);
                }
            }
        }
        p.addNoise(0, y1, WIDE_W - 1, y2, 0.35);
    }

    private static void torch(PixelArt p, int x, int y) {
        p.fillRect(x - 1, y + 2, x + 1, y + 11, 0x4a3320);          // handle
        p.fillRect(x - 3, y + 1, x + 3, y + 3, 0x6e4e2c);           // cup
        p.fillTriangle(x - 3, y + 1, x + 3, y + 1, x, y - 10, 0xff6a14);   // outer flame
        p.fillTriangle(x - 2, y + 1, x + 2, y + 1, x, y - 6, 0xffb02e);    // middle flame
        p.fillTriangle(x - 1, y + 1, x + 1, y + 1, x, y - 2, 0xfff2b0);    // hot core
        p.addGlow(x, y - 3, 30, 0xff7a2a, 0.95);
    }

    private static void gem(PixelArt p, double x, double y, int color) {
        p.addGlow(x, y, 8, color, 0.7);
        p.fillTriangle(x - 3.5, y, x, y - 4, x + 3.5, y, color);
        p.fillTriangle(x - 3.5, y, x, y + 4, x + 3.5, y, PixelArt.shade(color, 0.7));
    }

    private static void fillDoor(PixelArt p, int cx, int topY, int bottomY, int halfW, int color) {
        p.fillRect(cx - halfW, topY, cx + halfW, bottomY, color);
        p.fillCircle(cx, topY, halfW, color);
    }

    // Room 1: old wooden door, amber light leaking through the keyhole and under the door.
    private static void woodDoor(PixelArt p, int cx, int topY, int bottomY, int halfW) {
        fillDoor(p, cx, topY, bottomY, halfW, 0x72421e);
        for (int x = cx - halfW + 5; x < cx + halfW; x += 5) {        // wooden planks
            double top = topY - Math.sqrt(halfW * halfW - (x - cx) * (x - cx));
            p.fillRect(x, Math.ceil(top), x, bottomY, 0x1a0c04, 0.9);
        }
        p.addNoise(cx - halfW, topY - halfW, cx + halfW, bottomY, 0.4);
        p.fillRect(cx - halfW, 31, cx + halfW, 32, 0x22222e);          // iron bands
        p.fillRect(cx - halfW, 43, cx + halfW, 44, 0x22222e);
        p.fillRect(cx - 3, 35, cx + 3, 41, 0x22222e);                  // lock plate
        p.fillCircle(cx, 37, 1.6, 0x050505);                           // keyhole
        p.fillTriangle(cx - 1.5, 41, cx + 1.5, 41, cx, 37, 0x050505);
        p.addGlow(cx, 37, 7, 0xffc070, 0.9);
        p.fillRect(cx - halfW, bottomY - 1, cx + halfW, bottomY, 0xffc060, 0.95);   // light under door
        p.addGlow(cx, bottomY, 22, 0xffa030, 0.45);
    }

    // Room 2: cold iron door with a big golden padlock.
    private static void ironDoor(PixelArt p, int cx, int topY, int bottomY, int halfW) {
        fillDoor(p, cx, topY, bottomY, halfW, 0x39414f);
        for (int x = cx - halfW + 6; x < cx + halfW; x += 6) {         // metal seams
            double top = topY - Math.sqrt(halfW * halfW - (x - cx) * (x - cx));
            p.fillRect(x, Math.ceil(top), x, bottomY, 0x181c26, 0.8);
        }
        for (int x = cx - halfW + 3; x < cx + halfW; x += 6) {         // rivets
            p.fillRect(x, 29, x, 29, 0x9aa4b8);
            p.fillRect(x, 47, x, 47, 0x9aa4b8);
        }
        p.addNoise(cx - halfW, topY - halfW, cx + halfW, bottomY, 0.3);
        p.drawRing(cx, 35, 4.5, 2, GOLD_LIGHT, 1);                     // padlock shackle
        p.fillRect(cx - 6, 37, cx + 6, 46, GOLD);                      // padlock body
        p.fillRect(cx - 6, 37, cx - 4, 46, 0xffffff, 0.3);
        p.fillCircle(cx, 41, 1.4, 0x050505);                           // keyhole
        p.fillRect(cx, 41, cx, 44, 0x050505);
        p.addGlow(cx, 41, 20, 0x4a8aff, 0.22);
        p.fillRect(cx - halfW, bottomY - 1, cx + halfW, bottomY, 0x80c0ff, 0.95);
        p.addGlow(cx, bottomY, 22, 0x4a8aff, 0.45);
    }

    // Room 3: no door at all, a swirling purple and gold portal.
    private static void portalDoor(PixelArt p, int cx, int topY, int bottomY, int halfW) {
        fillDoor(p, cx, topY, bottomY, halfW, 0x1c0c38);
        p.addGlow(cx, 36, 24, 0xc080ff, 1.3);
        for (int r = 4; r <= 12; r += 4) {                             // magic rings
            p.drawRing(cx, 36, r, 1.2, GOLD_LIGHT, 0.55);
        }
        p.sprinkle(cx - halfW, topY - 2, cx + halfW, bottomY - 1, 0.9, 0xffffff, 5);
        p.addGlow(cx, bottomY, 26, 0xb060ff, 0.8);
    }

    // =====================================================================
    //  CHARACTERS
    // =====================================================================
    public static PixelArt warrior() {
        PixelArt p = new PixelArt(PORTRAIT_W, PORTRAIT_H);
        p.fillGradient(0, PORTRAIT_H - 1, 0x0c0818, 0x2a0f18);
        p.addGlow(28, 42, 38, 0xff5a1a, 0.7);                            // fire behind the hero
        p.sprinkle(0, 0, PORTRAIT_W - 1, 50, 0.975, 0xff9a40, 8);        // embers

        p.fillTriangle(28, 24, 3, 64, 53, 64, 0x7a1226);                 // red cape
        p.fillTriangle(28, 24, 53, 64, 28, 64, 0x000000, 0.3);

        p.fillRect(20, 50, 26, 63, STEEL_DARK);                          // legs
        p.fillRect(30, 50, 36, 63, STEEL_DARK);
        p.fillRect(19, 62, 27, 63, 0x2a2e40);                            // boots
        p.fillRect(29, 62, 37, 63, 0x2a2e40);

        p.fillRect(17, 30, 39, 50, STEEL);                               // chest plate
        p.fillRect(17, 30, 23, 50, 0xffffff, 0.2);                       // light from the left
        p.fillRect(33, 30, 39, 50, 0x000000, 0.3);                       // shadow on the right
        p.fillTriangle(28, 33, 24, 39, 32, 39, GOLD);                    // golden emblem
        p.fillTriangle(24, 39, 32, 39, 28, 45, GOLD);
        p.fillRect(17, 47, 39, 49, 0x6a4a20);                            // belt
        p.fillRect(26, 46, 30, 50, GOLD);

        p.fillRect(9, 33, 13, 48, STEEL);                                // arms
        p.fillRect(43, 33, 47, 47, STEEL_DARK);
        p.fillEllipse(15, 31, 6.5, 4.5, STEEL_LIGHT);                    // shoulder plates
        p.fillEllipse(41, 31, 6.5, 4.5, STEEL);

        p.fillRect(25, 26, 31, 30, STEEL_DARK);                          // neck
        p.fillEllipse(28, 19, 8, 9.5, STEEL);                            // helmet
        p.fillEllipse(24, 16, 3.5, 6, 0xffffff, 0.25);                   // shine
        p.fillRect(27, 10, 29, 20, STEEL_LIGHT, 0.7);                    // crest line
        p.fillRect(21, 18, 35, 20, 0x05050a);                            // visor slit
        p.fillRect(24, 19, 25, 19, 0xffd060);                            // glowing eyes
        p.fillRect(31, 19, 32, 19, 0xffd060);
        p.addGlow(24.5, 19, 5, 0xffb030, 0.8);
        p.addGlow(31.5, 19, 5, 0xffb030, 0.8);
        p.fillEllipse(28, 8, 2.5, 5, 0xd8203a);                          // red plume
        p.drawLine(29, 7, 38, 13, 2.5, 0xd8203a);

        p.fillCircle(9, 44, 8, 0x5c6a8a);                                // shield
        p.drawRing(9, 44, 7.5, 1.5, GOLD, 1);
        p.fillRect(8, 38, 10, 50, GOLD);
        p.fillRect(3, 43, 15, 45, GOLD);

        p.addGlow(48, 26, 18, 0xa0c0ff, 0.4);                            // sword
        p.drawLine(48, 7, 48, 46, 2.4, 0xe8eefc);
        p.fillTriangle(46.8, 7, 49.2, 7, 48, 2, 0xe8eefc);
        p.fillRect(44, 46, 52, 47, GOLD);
        p.fillRect(47, 48, 49, 53, 0x5a3418);
        p.fillCircle(48, 54, 1.6, GOLD);
        p.fillRect(46, 44, 50, 50, STEEL_DARK, 0.9);                     // hand

        p.addGlow(8, 30, 16, 0xff8a40, 0.25);                            // warm rim light
        p.vignette(0.4);
        return p;
    }

    public static PixelArt thief() {
        PixelArt p = new PixelArt(PORTRAIT_W, PORTRAIT_H);
        p.fillGradient(0, PORTRAIT_H - 1, 0x0a1232, 0x1f4070);
        p.sprinkle(0, 0, PORTRAIT_W - 1, 30, 0.97, 0xffffff, 4);          // stars
        p.addGlow(45, 10, 26, 0x7aa8ff, 0.8);                            // moon
        p.fillCircle(45, 10, 5, 0xe8f0ff);

        p.fillRect(0, 57, PORTRAIT_W - 1, 63, 0x05070d);                  // rooftop
        p.fillRect(3, 48, 10, 57, 0x05070d);                              // chimney

        p.fillTriangle(28, 20, 6, 61, 50, 61, 0x2a6a5c);                  // cloak
        p.fillEllipse(28, 32, 13, 6, 0x2a6a5c);                           // shoulders
        p.fillTriangle(28, 20, 50, 61, 28, 61, 0x000000, 0.4);            // shadow side
        p.drawLine(20, 34, 12, 58, 1.4, 0x5acab0, 0.7);                   // cloth folds
        p.drawLine(25, 38, 21, 59, 1.4, 0x5acab0, 0.5);

        p.drawLine(18, 34, 12, 48, 5, 0x1f5248);                          // left arm
        p.drawLine(37, 33, 44, 41, 4.5, 0x1f5248);                        // right arm
        p.fillCircle(46, 41, 2.4, 0xc29a78);                              // hand

        p.fillTriangle(28, 7, 17, 25, 39, 25, 0x245a50);                  // hood
        p.fillEllipse(28, 19, 10.5, 10.5, 0x245a50);
        p.drawRing(28, 19, 10, 1.3, 0x7ae0c8, 0.55);                      // moonlit hood edge        p.fillEllipse(28, 21, 6, 6.8, 0x020306);                          // dark face
        p.fillRect(24, 20, 26, 21, 0xfff0a0);                             // glowing eyes
        p.fillRect(30, 20, 32, 21, 0xfff0a0);
        p.addGlow(25, 20.5, 4, 0xffe060, 0.6);
        p.addGlow(31, 20.5, 4, 0xffe060, 0.6);
        p.fillEllipse(28, 25.5, 5, 2.2, 0x6a1a28);                        // mask over the mouth
        p.fillEllipse(28, 29.5, 10, 2.6, 0xc23a4c);                       // red scarf around the neck
        p.fillEllipse(28, 28.8, 8, 0.9, 0xffffff, 0.2);

        p.addGlow(50, 24, 12, 0xaad0ff, 0.7);                             // dagger
        p.drawLine(46.5, 40, 50, 22, 2, 0xeef4ff);
        p.fillTriangle(49, 22, 51, 22, 50, 15, 0xeef4ff);
        p.drawLine(43, 42, 49, 39, 2, GOLD);

        p.fillCircle(20, 49, 3.4, 0x8a5a2a);                              // coin pouch
        p.addGlow(20, 47, 9, GOLD, 0.7);
        p.fillRect(20, 58, 25, 60, 0x080a10);                             // boots

        p.addGlow(44, 14, 44, 0x7aa8ff, 0.25);                            // moonlight on everything
        p.vignette(0.4);
        return p;
    }

    // =====================================================================
    //  ENDINGS
    // =====================================================================
    public static PixelArt victory() {
        PixelArt p = new PixelArt(PORTRAIT_W, PORTRAIT_H);
        p.fillGradient(0, PORTRAIT_H - 1, 0x0c0618, 0x2a1a05);
        p.addGlow(28, 30, 36, 0xffc040, 0.85);
        for (int i = 0; i < 12; i++) {                                    // light rays
            double angle = Math.toRadians(i * 30 + 10);
            p.drawLine(28, 30, 28 + Math.cos(angle) * 46, 30 + Math.sin(angle) * 46, 1.6, GOLD_LIGHT, 0.2);
        }

        p.drawRing(13, 19, 5, 2, GOLD, 1);                                // handles
        p.drawRing(43, 19, 5, 2, GOLD, 1);
        p.fillRect(16, 12, 40, 22, GOLD);                                 // cup
        p.fillEllipse(28, 22, 12.5, 11, GOLD);
        p.fillRect(15, 11, 41, 13, GOLD_LIGHT);                           // rim
        p.fillRect(18, 14, 21, 25, 0xffffff, 0.35);                       // shine
        p.fillRect(35, 14, 40, 26, 0x000000, 0.2);                        // shade
        p.fillTriangle(28, 14, 25, 20, 31, 20, 0xfff3b0);                 // star on the cup
        p.fillTriangle(28, 24, 25, 18, 31, 18, 0xfff3b0);
        p.fillRect(26, 32, 30, 42, GOLD);                                 // stem
        p.fillRect(20, 42, 36, 45, GOLD_LIGHT);                           // base
        p.fillRect(17, 45, 39, 50, 0xb88a1c);
        p.fillRect(24, 46, 32, 48, 0x5a3418);                             // name plate

        sparkle(p, 10, 8, 3);
        sparkle(p, 47, 12, 4);
        sparkle(p, 6, 36, 2);
        sparkle(p, 50, 40, 3);
        sparkle(p, 28, 4, 2);
        confetti(p, 0, 0, PORTRAIT_W - 1, PORTRAIT_H - 1);
        p.vignette(0.4);
        return p;
    }

    public static PixelArt defeat() {
        PixelArt p = new PixelArt(PORTRAIT_W, PORTRAIT_H);
        p.fillGradient(0, PORTRAIT_H - 1, 0x060306, 0x2a0808);
        p.addGlow(28, 34, 36, 0xff2a1a, 0.55);
        p.sprinkle(0, 0, PORTRAIT_W - 1, PORTRAIT_H - 1, 0.97, 0xff5a30, 6);   // embers

        p.drawLine(11, 47, 45, 62, 3.2, 0xb8b09c);                        // crossed bones
        p.drawLine(45, 47, 11, 62, 3.2, 0xb8b09c);
        p.fillCircle(10, 46, 2.4, 0xb8b09c);
        p.fillCircle(46, 46, 2.4, 0xb8b09c);
        p.fillCircle(10, 63, 2.4, 0xb8b09c);
        p.fillCircle(46, 63, 2.4, 0xb8b09c);

        p.fillEllipse(28, 25, 15.5, 16, 0xd8d0bc);                        // skull
        p.fillEllipse(28, 38, 10, 8, 0xc9c1ac);                           // jaw
        p.fillEllipse(17, 22, 5, 9, 0xffffff, 0.15);                      // shine
        p.fillEllipse(21, 27, 4.8, 5.6, 0x050203);                        // eye sockets
        p.fillEllipse(35, 27, 4.8, 5.6, 0x050203);
        p.addGlow(21, 27, 8, 0xff3020, 0.95);
        p.addGlow(35, 27, 8, 0xff3020, 0.95);
        p.fillEllipse(21, 27, 2, 2.4, 0xff6a40);                          // glowing pupils
        p.fillEllipse(35, 27, 2, 2.4, 0xff6a40);
        p.fillTriangle(28, 31, 25.5, 37, 30.5, 37, 0x050203);             // nose
        p.fillRect(21, 41, 35, 45, 0xcfc7b0);                             // teeth
        for (int x = 22; x <= 34; x += 3) {
            p.fillRect(x, 41, x, 45, 0x1a1210, 0.85);
        }
        p.fillRect(21, 43, 35, 43, 0x1a1210, 0.6);
        p.drawLine(29, 10, 26, 17, 1, 0x1a1210, 0.9);                     // crack
        p.drawLine(26, 17, 29, 21, 1, 0x1a1210, 0.9);
        p.vignette(0.45);
        return p;
    }

    private static void sparkle(PixelArt p, double x, double y, int size) {
        p.addGlow(x, y, size * 2.2, 0xffe9a0, 0.8);
        p.drawLine(x - size, y, x + size, y, 1, 0xffffff, 0.9);
        p.drawLine(x, y - size, x, y + size, 1, 0xffffff, 0.9);
    }

    private static void confetti(PixelArt p, int x1, int y1, int x2, int y2) {
        int[] colors = {0xff4a6a, 0x4adfff, 0xffe04a, 0x7aff7a, 0xc07aff};
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (PixelArt.noise(x * 2.1 + 5, y * 1.3 + 9) > 0.975) {
                    int index = (int) (PixelArt.noise(x, y) * colors.length);
                    p.paint(x, y, colors[index], 1);
                }
            }
        }
    }

    // =====================================================================
    //  TEXT FRAME: draws a box of + - | characters around lines of text
    // =====================================================================
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
}
