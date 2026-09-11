package com.slinky.ludus.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.util.Objects.requireNonNull;

/**
 * A {@link JButton} that draws itself from one of the small round or square button images, at the image's own
 * size.
 * <p>
 * Each button comes as a regular image and a pressed image on a canvas of the same size. The button shows the
 * pressed image while the user holds the mouse down over it, and the regular image otherwise.
 * <p>
 * A caller picks the icon drawn on the button from {@link Symbol}. The button draws the icon at its own size,
 * centred on the top of the button in the image it is showing, above the base that the art shows underneath.
 * The icon therefore sinks with the button when pressed.
 * <p>
 * Every factory method also takes an angle in radians, and the button turns the icon clockwise by that angle.
 * The angle must be a whole number of quarter turns, such as {@code Math.PI / 2} or {@code -Math.PI}, so that
 * every pixel of the icon lands on a whole pixel of the button. The button accepts the tiny rounding error in a
 * value such as {@code Math.toRadians(270)}.
 *
 * <pre>{@code
 * var close = SmallButton.redSquare(SmallButton.Symbol.CROSS, 0);    // preferred size 128 x 128
 * close.addActionListener(e -> dispose());
 *
 * var up = SmallButton.blueRound(SmallButton.Symbol.LEFT_ARROW, Math.PI / 2);    // the arrow points up
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
public class SmallButton extends JButton {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final String ROOT_DIR = "/assets/ui-elements/buttons/";

    // The bottom rows of every small button image show the base of the button, below the dark line that ends its
    // top. Both heights are measured from the art, and the icon centres on the top of the button above them.
    private static final int REGULAR_BASE_HEIGHT = 14;
    private static final int PRESSED_BASE_HEIGHT = 9;

    // How far below the centre of the button's top the icon is drawn. This is a decision made by eye, since the
    // exact centre looks slightly high.
    private static final int ICON_DROP = 3;

    private static final double QUARTER_TURN = Math.PI / 2;

    // How far an angle may stray from a whole number of quarter turns and still count as one. Rounding in a value
    // such as Math.toRadians(270) stays far below this.
    private static final double TOLERANCE = 1e-9;

    /**
     * Creates a blue round button showing the given icon, turned clockwise by {@code radians}.
     *
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @return a new button
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    public static SmallButton blueRound(Symbol symbol, double radians) {
        return new SmallButton(Colour.BLUE, Type.ROUND, symbol, radians);
    }

    /**
     * Creates a blue square button showing the given icon, turned clockwise by {@code radians}.
     *
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @return a new button
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    public static SmallButton blueSquare(Symbol symbol, double radians) {
        return new SmallButton(Colour.BLUE, Type.SQUARE, symbol, radians);
    }

    /**
     * Creates a red round button showing the given icon, turned clockwise by {@code radians}.
     *
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @return a new button
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    public static SmallButton redRound(Symbol symbol, double radians) {
        return new SmallButton(Colour.RED, Type.ROUND, symbol, radians);
    }

    /**
     * Creates a red square button showing the given icon, turned clockwise by {@code radians}.
     *
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @return a new button
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    public static SmallButton redSquare(Symbol symbol, double radians) {
        return new SmallButton(Colour.RED, Type.SQUARE, symbol, radians);
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    /**
     * The icons a small button can show, each defined by the name of its 64 by 64 image in
     * {@code /assets/ui-elements/icons/}.
     */
    public enum Symbol {

        /** A cog, from {@code cog.png}. */
        COG("cog"),

        /** A gold coin, from {@code coin.png}. */
        COIN("coin"),

        /** An information sign, from {@code info.png}. */
        INFO("info"),

        /** An arrow pointing left, from {@code left-arrow.png}. */
        LEFT_ARROW("left-arrow"),

        /** A log of wood, from {@code log.png}. */
        LOG("log"),

        /** A mallet, from {@code mallet.png}. */
        MALLET("mallet"),

        /** A cut of meat, from {@code meat.png}. */
        MEAT("meat"),

        /** A musical note, from {@code music.png}. */
        MUSIC("music"),

        /** A play triangle, from {@code play.png}. */
        PLAY("play"),

        /** A shield, from {@code shield.png}. */
        SHIELD("shield"),

        /** A sword, from {@code sword.png}. */
        SWORD("sword"),

        /** A cross, from {@code cross.png}. */
        CROSS("cross");

        private static final String ROOT_DIR = "/assets/ui-elements/icons/";

        private final String fileName;

        Symbol(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Returns the classpath path of this icon, such as {@code "/assets/ui-elements/icons/cog.png"} for
         * {@link #COG}.
         *
         * @return the absolute classpath path, starting with {@code /}
         */
        String getPath() {
            return ROOT_DIR + fileName + ".png";
        }
    }

    /**
     * The shapes a small button comes in, each defined by the part of its file names that follows the colour.
     */
    enum Type {

        /** Drawn from the {@code small*squarebutton} images. */
        SQUARE("square"),

        /** Drawn from the {@code small*roundbutton} images. */
        ROUND("round");

        private final String fileName;

        Type(String fileName) {
            this.fileName = fileName;
        }
    }

    /**
     * The colours a small button comes in, each defined by the part of its file names that follows "small".
     */
    enum Colour {

        /** Drawn from the {@code smallblue*button} images. */
        BLUE("blue"),

        /** Drawn from the {@code smallred*button} images. */
        RED("red");

        private final String fileName;

        Colour(String fileName) {
            this.fileName = fileName;
        }
    }

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final BufferedImage regularImage;
    private final BufferedImage pressedImage;
    private final Rectangle     regularTop;
    private final Rectangle     pressedTop;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a button in the given colour and shape, showing the given icon turned clockwise by {@code radians}.
     *
     * @param colour  the colour of the button
     * @param type    the shape of the button
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    SmallButton(Colour colour, Type type, Symbol symbol, double radians) {
        super(buildIcon(symbol, radians));
        this.regularImage = Resources.readImage(buildPath(colour, type, "regular"));
        this.pressedImage = Resources.readImage(buildPath(colour, type, "pressed"));

        this.regularTop   = findTop(regularImage, REGULAR_BASE_HEIGHT);
        this.pressedTop   = findTop(pressedImage, PRESSED_BASE_HEIGHT);

        // Switches off everything the look and feel draws apart from the icon and label, which paintComponent
        // replaces.
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Returns the size of the button images, 128 by 128 for every current colour and shape. Layout managers
     * call this to decide how much room the button gets.
     *
     * @return the preferred size, large enough for both the regular and the pressed image
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                Math.max(regularImage.getWidth(), pressedImage.getWidth()),
                Math.max(regularImage.getHeight(), pressedImage.getHeight())
        );
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a minimum size from the icon and label
     * alone, which would let a layout manager squeeze the button smaller than its image.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a maximum size from the icon and label
     * alone, which would let a layout manager such as {@link BoxLayout} clip the button's image.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Draws the regular or the pressed image whole from the top-left corner, then the icon and label centred on
     * the top of the button in that image.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        var showPressed = isShowingPressed();
        var top         = showPressed ? pressedTop : regularTop;

        g.drawImage(showPressed ? pressedImage : regularImage, 0, 0, null);

        // The look and feel centres the icon on the whole button, so moving the graphics by the gap between
        // the two centres puts the icon in the middle of the button's top instead.
        var content = g.create();
        try {
            content.translate(
                    top.x + (top.width / 2) - (getWidth() / 2),
                    top.y + (top.height / 2) - (getHeight() / 2) + ICON_DROP
            );
            super.paintComponent(content);
        } finally {
            content.dispose();
        }
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Reads the image of a symbol and returns it as an icon, turned clockwise by {@code radians}.
     *
     * @param symbol  the icon drawn on the button
     * @param radians the clockwise angle of the icon, a whole number of quarter turns
     *
     * @return a new icon
     *
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code radians} is not a multiple of {@code Math.PI / 2}
     */
    private static ImageIcon buildIcon(Symbol symbol, double radians) {
        requireNonNull(symbol, "symbol must not be null");
        var quarterTurns = countQuarterTurns(radians);

        return new ImageIcon(turnClockwise(Resources.readImage(symbol.getPath()), quarterTurns));
    }

    /**
     * Returns the number of clockwise quarter turns in an angle, from 0 to 3. A negative angle turns
     * anticlockwise, which equals the matching number of clockwise turns.
     *
     * <pre>{@code
     * Math.PI / 2          ->  1
     * Math.toRadians(270)  ->  3
     * 2 * Math.PI          ->  0
     * -Math.PI / 2         ->  3
     * Math.PI / 4          ->  throws IllegalArgumentException
     * }</pre>
     *
     * @param radians the angle, a whole number of quarter turns
     *
     * @return the number of clockwise quarter turns, from 0 to 3
     *
     * @throws IllegalArgumentException if {@code radians} is more than {@code TOLERANCE} away from a multiple of
     *                                  {@code Math.PI / 2}, or is NaN or infinite
     */
    private static int countQuarterTurns(double radians) {
        var quarterTurns = Math.round(radians / QUARTER_TURN);
        if (!Double.isFinite(radians) || Math.abs(radians - (quarterTurns * QUARTER_TURN)) > TOLERANCE) {
            throw new IllegalArgumentException("radians must be a multiple of Math.PI / 2, got %s".formatted(radians));
        }

        return Math.floorMod(quarterTurns, 4);
    }

    /**
     * Returns a copy of an image turned clockwise by the given number of quarter turns. Each pixel moves to a
     * whole pixel of the copy, so the copy contains exactly the pixels of the original. One or three turns swap
     * the width and the height.
     *
     * @param image        the image to turn
     * @param quarterTurns the number of clockwise quarter turns, from 0 to 3
     *
     * @return a new image of type {@link BufferedImage#TYPE_INT_ARGB}
     *
     * @throws IllegalArgumentException if {@code quarterTurns} is outside 0 to 3
     */
    private static BufferedImage turnClockwise(BufferedImage image, int quarterTurns) {
        var width    = image.getWidth();
        var height   = image.getHeight();
        var sideways = quarterTurns % 2 == 1;
        var turned   = new BufferedImage(
                sideways ? height : width,
                sideways ? width : height,
                BufferedImage.TYPE_INT_ARGB
        );

        // Copies the pixel at (x, y) to the place a clockwise turn moves it to. One turn sends the top-left pixel
        // to the top-right, two to the bottom-right, and three to the bottom-left.
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var argb = image.getRGB(x, y);
                switch (quarterTurns) {
                    case 0  -> turned.setRGB(x, y, argb);
                    case 1  -> turned.setRGB(height - 1 - y, x, argb);
                    case 2  -> turned.setRGB(width - 1 - x, height - 1 - y, argb);
                    case 3  -> turned.setRGB(y, width - 1 - x, argb);
                    default -> throw new IllegalArgumentException("quarterTurns must be 0 to 3, got %d".formatted(quarterTurns));
                }
            }
        }

        return turned;
    }

    /**
     * Returns the part of an image that shows the top of the button, which is the bounds of every visible pixel
     * without the base rows at the bottom. For the regular images this runs from row 17 to row 96.
     *
     * @param image      the regular or the pressed image
     * @param baseHeight the number of rows at the bottom of the visible pixels that show the base
     *
     * @return the position and size of the button's top, in image coordinates
     */
    private static Rectangle findTop(BufferedImage image, int baseHeight) {
        var visible = ImageSlicer.scan(image, 1, 1).getRegion(0, 0); // The bounds of every visible pixel.
        return new Rectangle(visible.x, visible.y, visible.width, visible.height - baseHeight);
    }

    /**
     * Returns the classpath path of one image, such as
     * {@code "/assets/ui-elements/buttons/smallredsquarebutton_pressed.png"}.
     *
     * @param colour the colour of the button
     * @param type   the shape of the button
     * @param state  either {@code "regular"} or {@code "pressed"}
     *
     * @return the absolute classpath path, starting with {@code /}
     */
    private static String buildPath(Colour colour, Type type, String state) {
        return "%ssmall%s%sbutton_%s.png".formatted(ROOT_DIR, colour.fileName, type.fileName, state);
    }

    /**
     * Returns true while the user holds the mouse down over the button, which is when Swing's own buttons draw
     * themselves pressed. Dragging off the button while holding the mouse down returns false.
     *
     * @return true while the button shows its pressed image
     */
    private boolean isShowingPressed() {
        var model = getModel();
        return model.isArmed() && model.isPressed();
    }

}
