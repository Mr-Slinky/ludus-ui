package com.slinky.ui;

import javax.swing.*;
import java.awt.*;

import static java.util.Objects.requireNonNull;

/**
 * A {@link JPanel} that draws a ribbon or a sword, stretched sideways by repeating its middle piece.
 * <p>
 * Each image is one row of three pieces: a left end, a middle and a right end. The panel draws each end once and
 * repeats the middle piece {@code hScale} times between them, so each call to {@link #increaseWidth()} adds the
 * width of one middle piece to the panel. The height stays that of the image.
 * <p>
 * A caller creates a panel from {@link #sword(Colour, int)}, {@link #big(Colour, int)},
 * {@link #smallForked(Colour, int)} or {@link #smallPointed(Colour, int)} and adds it to a container. The panel
 * reports the room it needs through {@link #getPreferredSize()}. Whatever is behind the panel shows through the
 * see-through parts of the image.
 *
 * <pre>{@code
 * var title = Ribbon.big(Ribbon.Colour.RED, 2);    // preferred size 323 x 103
 * frame.add(title);
 * frame.pack();
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
public class Ribbon extends JPanel {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final int ROWS    = 1; // Every ribbon and sword image is one row of three pieces.
    private static final int COLUMNS = 3;

    /**
     * Creates a sword, with the handle on the left and the point on the right. The shortest sword,
     * {@code sword(colour, 0)}, is 197 by 128 pixels.
     *
     * @param colour          the colour of the handle
     * @param horizontalScale the number of middle pieces, 0 or more
     *
     * @return a new panel
     *
     * @throws NullPointerException     if {@code colour} is null
     * @throws IllegalArgumentException if {@code horizontalScale} is negative
     */
    public static Ribbon sword(Colour colour, int horizontalScale) {
        return new Ribbon(Style.SWORD, colour, horizontalScale);
    }

    /**
     * Creates a big ribbon, with folded ends that fan out behind it. The shortest big ribbon,
     * {@code big(colour, 0)}, is 195 by 103 pixels.
     *
     * @param colour          the colour of the ribbon
     * @param horizontalScale the number of middle pieces, 0 or more
     *
     * @return a new panel
     *
     * @throws NullPointerException     if {@code colour} is null
     * @throws IllegalArgumentException if {@code horizontalScale} is negative
     */
    public static Ribbon big(Colour colour, int horizontalScale) {
        return new Ribbon(Style.BIG, colour, horizontalScale);
    }

    /**
     * Creates a small ribbon with forked ends, like a swallow's tail. The shortest one,
     * {@code smallForked(colour, 0)}, is 124 by 60 pixels.
     *
     * @param colour          the colour of the ribbon
     * @param horizontalScale the number of middle pieces, 0 or more
     *
     * @return a new panel
     *
     * @throws NullPointerException     if {@code colour} is null
     * @throws IllegalArgumentException if {@code horizontalScale} is negative
     */
    public static Ribbon smallForked(Colour colour, int horizontalScale) {
        return new Ribbon(Style.SMALL_FORKED, colour, horizontalScale);
    }

    /**
     * Creates a small ribbon with pointed ends. The shortest one, {@code smallPointed(colour, 0)}, is 122 by 54
     * pixels.
     *
     * @param colour          the colour of the ribbon
     * @param horizontalScale the number of middle pieces, 0 or more
     *
     * @return a new panel
     *
     * @throws NullPointerException     if {@code colour} is null
     * @throws IllegalArgumentException if {@code horizontalScale} is negative
     */
    public static Ribbon smallPointed(Colour colour, int horizontalScale) {
        return new Ribbon(Style.SMALL_POINTED, colour, horizontalScale);
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    /**
     * The colours every ribbon and sword comes in, each defined by the colour word in its file names.
     */
    public enum Colour {
        BLUE("blue"),
        RED("red"),
        YELLOW("yellow"),
        PURPLE("purple"),
        BLACK("black");

        private final String fileName;

        Colour(String fileName) {
            this.fileName = fileName;
        }
    }

    /**
     * The four kinds of image a panel can draw, each defined by the pattern of its file names, where {@code %s}
     * stands for the colour.
     */
    enum Style {

        /** Drawn from {@code swords/<colour>.png}. */
        SWORD("swords/%s.png"),

        /** Drawn from {@code ribbons/<colour>-big.png}. */
        BIG("ribbons/%s-big.png"),

        /** Drawn from {@code ribbons/<colour>-small-forked.png}. */
        SMALL_FORKED("ribbons/%s-small-forked.png"),

        /** Drawn from {@code ribbons/<colour>-small-pointed.png}. */
        SMALL_POINTED("ribbons/%s-small-pointed.png");

        private static final String ROOT_DIR = "/assets/ui-elements/";

        private final String pathPattern;

        Style(String pathPattern) {
            this.pathPattern = pathPattern;
        }

        /**
         * Returns the classpath path of this style in the given colour, such as
         * {@code "/assets/ui-elements/ribbons/red-big.png"} for {@link #BIG} in {@link Colour#RED}.
         *
         * @param colour the colour of the ribbon or sword
         *
         * @return the absolute classpath path, starting with {@code /}
         */
        String getPath(Colour colour) {
            return ROOT_DIR + pathPattern.formatted(colour.fileName);
        }
    }

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final SliceRenderer renderer;

    private int hScale;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a panel in the given style and colour.
     *
     * @param style  the kind of ribbon or sword
     * @param colour the colour of the ribbon or sword
     * @param hScale the number of middle pieces, 0 or more
     *
     * @throws NullPointerException     if {@code colour} is null
     * @throws IllegalArgumentException if {@code hScale} is negative
     */
    Ribbon(Style style, Colour colour, int hScale) {
        super(true); // double buffered = true
        requireNonNull(colour, "colour must not be null");
        if (hScale < 0) {
            throw new IllegalArgumentException("hScale must be 0 or more, got %d".formatted(hScale));
        }

        this.renderer = SliceRenderer.load(style.getPath(colour), ROWS, COLUMNS);
        this.hScale   = hScale;

        // Tells Swing to draw whatever is behind the panel first, so it shows through the see-through parts of
        // the image.
        setOpaque(false);
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Adds one middle piece, which widens the panel by 64 pixels for every current ribbon and sword.
     * <p>
     * The panel asks its container to lay it out again and redraws itself. The caller calls {@code pack()} on
     * the enclosing window to grow the window to fit.
     */
    public void increaseWidth() {
        hScale += 1;
        refreshSize();
    }

    /**
     * Returns the size at which the panel draws every piece at full size. Layout managers call this to decide
     * how much room the panel gets.
     * <p>
     * The width is the left end's width, plus {@code hScale} times the middle piece's width, plus the right end's
     * width. For {@code big(colour, 2)} that gives 98 + (2 * 64) + 97 = 323 wide, and the height is 103.
     *
     * @return the preferred size, worked out on every call from the current {@code hScale}
     */
    @Override
    public Dimension getPreferredSize() {
        return renderer.measure(hScale, 0);
    }

    /**
     * Draws every piece at full size from the top-left corner: the left end, {@code hScale} copies of the middle
     * piece, and the right end. Where a layout manager gives the panel more room than {@link #getPreferredSize()},
     * whatever is behind the panel shows in the extra area to the right and below.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint(g, hScale, 0);
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Makes Swing lay the panel out again and redraw it, after {@code hScale} changes.
     */
    private void refreshSize() {
        revalidate(); // Tells the layout manager to call getPreferredSize() again and redo the layout.
        repaint();    // Asks Swing to call paintComponent() again soon.
    }

}
