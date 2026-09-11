package com.slinky.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A {@link JPanel} that draws itself from a 9-slice image, such as a wooden table or a sheet of paper.
 * <p>
 * The image contains nine pieces, made up of four corners, four edges and a centre. The panel draws each corner
 * once. Between the corners it repeats the middle column of pieces {@code hScale} times and the middle row of
 * pieces {@code vScale} times, so each call to {@link #increaseWidth()} or {@link #increaseHeight()} adds the
 * width or height of one middle piece to the panel.
 * <p>
 * A caller creates a panel from {@link #wood(int, int)}, {@link #regularPaper(int, int)},
 * {@link #specialPaper(int, int)} or {@link #banner(int, int)} and adds it to a container. The panel reports
 * the room it needs through {@link #getPreferredSize()}. Whatever is behind the panel shows through the
 * see-through parts of the image.
 *
 * <pre>{@code
 * var table = Surface.wood(3, 1);    // preferred size 360 x 252
 * frame.add(table);
 * frame.pack();
 *
 * table.increaseWidth();            // preferred size 424 x 252
 * frame.pack();                     // grows the window to fit
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
public class Surface extends JPanel {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final int GRID_SIZE = 3; // Every panel image is three rows of three pieces.

    /**
     * Creates a panel drawn from the wooden table image. The smallest panel, {@code wood(0, 0)}, is 168 by 188
     * pixels and draws only the four corners.
     *
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new panel
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static Surface wood(int horizontalScale, int verticalScale) {
        return new Surface(horizontalScale, verticalScale, Type.WOOD.getPath());
    }

    /**
     * Creates a panel drawn from the regular paper image. The smallest panel, {@code regularPaper(0, 0)}, is
     * 104 by 89 pixels and draws only the four corners.
     *
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new panel
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static Surface regularPaper(int horizontalScale, int verticalScale) {
        return new Surface(horizontalScale, verticalScale, Type.REGULAR_PAPER.getPath());
    }

    /**
     * Creates a panel drawn from the special paper image. The smallest panel, {@code specialPaper(0, 0)}, is
     * 110 by 87 pixels and draws only the four corners.
     *
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new panel
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static Surface specialPaper(int horizontalScale, int verticalScale) {
        return new Surface(horizontalScale, verticalScale, Type.SPECIAL_PAPER.getPath());
    }

    /**
     * Creates a panel drawn from the banner image. The smallest panel, {@code banner(0, 0)}, is 184 by 179
     * pixels and draws only the four corners.
     *
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new panel
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static Surface banner(int horizontalScale, int verticalScale) {
        return new Surface(horizontalScale, verticalScale, Type.BANNER.getPath());
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    /**
     * The 9-slice images a panel can draw, each defined by its path on the classpath.
     */
    public enum Type {

        /** Cream paper with thin dark edges. */
        REGULAR_PAPER("papers/regularpaper.png"),

        /** Dark slate paper with gold scrolls in the corners. */
        SPECIAL_PAPER("papers/specialpaper.png"),

        /** A wooden table top with a wooden frame and metal corner caps. */
        WOOD("wood-table/woodtable.png"),

        /** A parchment scroll whose bottom corners roll up. */
        BANNER("banners/banner.png");

        private static final String ROOT_DIR = "/assets/ui-elements/";

        private final String path;

        Type(String path) {
            this.path = ROOT_DIR + path;
        }

        /**
         * Returns the classpath path of this image, such as
         * {@code "/assets/ui-elements/wood-table/woodtable.png"} for {@link #WOOD}.
         *
         * @return the absolute classpath path, starting with {@code /}
         */
        public String getPath() {
            return path;
        }
    }

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final SliceRenderer renderer;

    private int hScale;
    private int vScale;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a panel from the 9-slice image at the given classpath path.
     *
     * @param hScale  the number of middle columns, 0 or more
     * @param vScale  the number of middle rows, 0 or more
     * @param srcPath the classpath path of the image, for example from {@link Type#getPath()}
     *
     * @throws Resources.ResourceNotFoundException if the classpath contains no file at {@code srcPath}
     * @throws RuntimeException                    if reading the file fails
     * @throws IllegalArgumentException            if {@code hScale} or {@code vScale} is negative, or if the
     *                                             image does not contain nine pieces in three rows and three
     *                                             columns with fully transparent gaps between them
     */
    Surface(int hScale, int vScale, String srcPath) {
        this(hScale, vScale, SliceRenderer.load(srcPath, GRID_SIZE, GRID_SIZE));
    }

    /**
     * Creates a panel from an image that is already loaded.
     *
     * @param hScale the number of middle columns, 0 or more
     * @param vScale the number of middle rows, 0 or more
     * @param src    the 9-slice image
     *
     * @throws NullPointerException     if {@code src} is null
     * @throws IllegalArgumentException if {@code hScale} or {@code vScale} is negative, or if {@code src} does
     *                                  not contain nine pieces in three rows and three columns with fully
     *                                  transparent gaps between them
     */
    Surface(int hScale, int vScale, BufferedImage src) {
        this(hScale, vScale, new SliceRenderer(src, GRID_SIZE, GRID_SIZE));
    }

    private Surface(int hScale, int vScale, SliceRenderer renderer) {
        super(true); // double buffered = true
        if (hScale < 0 || vScale < 0) {
            throw new IllegalArgumentException(
                    "hScale and vScale must be 0 or more, got %d and %d".formatted(hScale, vScale)
            );
        }

        this.renderer = renderer;
        this.hScale   = hScale;
        this.vScale   = vScale;

        // Tells Swing to draw whatever is behind the panel first, so it shows through the see-through parts of
        // the image.
        setOpaque(false);
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Adds one middle column, which widens the panel by the width of one middle piece.
     * <p>
     * The panel asks its container to lay it out again and redraws itself. The caller calls {@code pack()} on
     * the enclosing window to grow the window to fit.
     */
    public void increaseWidth() {
        hScale += 1;
        refreshSize();
    }

    /**
     * Adds one middle row, which makes the panel taller by the height of one middle piece.
     * <p>
     * The panel asks its container to lay it out again and redraws itself. The caller calls {@code pack()} on
     * the enclosing window to grow the window to fit.
     */
    public void increaseHeight() {
        vScale += 1;
        refreshSize();
    }

    /**
     * Returns the size at which the panel draws every piece at full size. Layout managers call this to decide
     * how much room the panel gets.
     * <p>
     * The width is the left column's width, plus {@code hScale} times the middle column's width, plus the right
     * column's width. The height adds up the rows in the same way, with {@code vScale} middle rows. For
     * {@code wood(3, 1)} that gives 84 + (3 * 64) + 84 = 360 wide and 85 + 64 + 103 = 252 high.
     *
     * @return the preferred size, worked out on every call from the current {@code hScale} and {@code vScale}
     */
    @Override
    public Dimension getPreferredSize() {
        return renderer.measure(hScale, vScale);
    }

    /**
     * Draws every piece at full size from the top-left corner.
     * <p>
     * Each row of the panel contains the left piece, {@code hScale} copies of the middle piece, and the right
     * piece. The panel contains the top row, {@code vScale} copies of the middle row, and the bottom row. Where
     * a layout manager gives the panel more room than {@link #getPreferredSize()}, whatever is behind the panel
     * shows in the extra area to the right and below.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint(g, hScale, vScale);
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Makes Swing lay the panel out again and redraw it, after {@code hScale} or {@code vScale} changes.
     */
    private void refreshSize() {
        revalidate(); // Tells the layout manager to call getPreferredSize() again and redo the layout.
        repaint();    // Asks Swing to call paintComponent() again soon.
    }

}
