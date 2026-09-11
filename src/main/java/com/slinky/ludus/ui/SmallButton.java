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
 *
 * <pre>{@code
 * var close = SmallButton.redSquare(SmallButton.Symbol.CROSS);    // preferred size 128 x 128
 * close.addActionListener(e -> dispose());
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

    /**
     * Creates a blue round button showing the given icon.
     *
     * @param symbol the icon drawn on the button
     *
     * @return a new button
     *
     * @throws NullPointerException if {@code symbol} is null
     */
    public static SmallButton blueRound(Symbol symbol, double radians) {
        return new SmallButton(Colour.BLUE, Type.ROUND, symbol);
    }

    /**
     * Creates a blue square button showing the given icon.
     *
     * @param symbol the icon drawn on the button
     *
     * @return a new button
     *
     * @throws NullPointerException if {@code symbol} is null
     */
    public static SmallButton blueSquare(Symbol symbol, double radians) {
        return new SmallButton(Colour.BLUE, Type.SQUARE, symbol);
    }

    /**
     * Creates a red round button showing the given icon.
     *
     * @param symbol the icon drawn on the button
     *
     * @return a new button
     *
     * @throws NullPointerException if {@code symbol} is null
     */
    public static SmallButton redRound(Symbol symbol, double radians) {
        return new SmallButton(Colour.RED, Type.ROUND, symbol);
    }

    /**
     * Creates a red square button showing the given icon.
     *
     * @param symbol the icon drawn on the button
     *
     * @return a new button
     *
     * @throws NullPointerException if {@code symbol} is null
     */
    public static SmallButton redSquare(Symbol symbol, double radians) {
        return new SmallButton(Colour.RED, Type.SQUARE, symbol);
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
     * Creates a button in the given colour and shape, showing the given icon.
     *
     * @param colour the colour of the button
     * @param type   the shape of the button
     * @param symbol the icon drawn on the button
     *
     * @throws NullPointerException if {@code symbol} is null
     */
    SmallButton(Colour colour, Type type, Symbol symbol) {
        super(new ImageIcon(Resources.readImage(requireNonNull(symbol, "symbol must not be null").getPath())));
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
