package com.slinky.ui;

import com.slinky.ui.data.Resources;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/**
 * A {@link JPanel} that draws itself from a 9-slice image, such as a wooden table or a sheet of paper.
 * <p>
 * The image contains nine pieces, made up of four corners, four edges and a centre. The panel draws each corner
 * once. Between the corners it repeats the middle column of pieces {@code hScale} times and the middle row of
 * pieces {@code vScale} times, so each call to {@link #increaseWidth()} or {@link #increaseHeight()} adds the
 * width or height of one middle piece to the panel.
 * <p>
 * A caller creates a panel from {@link #wood(int, int)} or {@link #paper(int, int)} and adds it to a container.
 * The panel reports the room it needs through {@link #getPreferredSize()}, and paints a black background behind
 * the see-through parts of the image.
 *
 * <pre>{@code
 * var table = LPanel.wood(3, 1);    // preferred size 360 x 252
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
public class LPanel extends JPanel {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
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
    public static LPanel wood(int horizontalScale, int verticalScale) {
        return new LPanel(horizontalScale, verticalScale, Type.WOOD.getPath());
    }

    /**
     * Creates a panel drawn from the regular paper image. The smallest panel, {@code paper(0, 0)}, is 104 by 89
     * pixels and draws only the four corners.
     *
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new panel
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static LPanel paper(int horizontalScale, int verticalScale) {
        return new LPanel(horizontalScale, verticalScale, Type.REGULAR_PAPER.getPath());
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
        WOOD("wood-table/woodtable.png");

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
    private final BufferedImage src;
    private final ImageSlicer   slicer;

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
    LPanel(int hScale, int vScale, String srcPath) {
        this(hScale, vScale, loadImage(srcPath));
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
    LPanel(int hScale, int vScale, BufferedImage src) {
        super(true);
        if (hScale < 0 || vScale < 0) {
            throw new IllegalArgumentException(
                    "hScale and vScale must be 0 or more, got %d and %d".formatted(hScale, vScale)
            );
        }

        this.src    = src;
        this.slicer = ImageSlicer.scan(src);
        this.hScale = hScale;
        this.vScale = vScale;

        // A JPanel is opaque by default, so super.paintComponent() fills the whole panel with this colour
        // before
        // paintComponent() draws the pieces on top.
        setBackground(Color.BLACK);
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
        var width = slicer.getRegion(0, 0).width +
                    (hScale * slicer.getRegion(0, 1).width) +
                    slicer.getRegion(0, 2).width;

        var height = slicer.getRegion(0, 0).height +
                     (vScale * slicer.getRegion(1, 0).height) +
                     slicer.getRegion(2, 0).height;

        return new Dimension(width, height);
    }

    /**
     * Fills the panel with its background colour, then draws every piece at full size from the top-left
     * corner.
     * <p>
     * Each row of the panel contains the left piece, {@code hScale} copies of the middle piece, and the right
     * piece. The panel contains the top row, {@code vScale} copies of the middle row, and the bottom row. Where
     * a layout manager gives the panel more room than {@link #getPreferredSize()}, the extra area to the right
     * and below stays the background colour.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var y = 0;
        for (var row : buildPieceOrder(vScale)) {
            var x = 0;
            for (var column : buildPieceOrder(hScale)) {
                var piece = slicer.getRegion(row, column);
                drawPiece(g, piece, x, y);
                x += piece.width;
            }
            y += slicer.getRegion(row, 0).height;
        }
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Reads the image at the given classpath path.
     *
     * @param path the classpath path of the image
     *
     * @return the decoded image, or null where no installed {@link ImageIO} reader supports the file's format
     *
     * @throws Resources.ResourceNotFoundException if the classpath contains no file at {@code path}
     * @throws RuntimeException                    if reading the file fails
     */
    private static BufferedImage loadImage(String path) {
        var in = Resources.getResource(path);
        try (in) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load image at" + path, ex);
        }
    }

    /**
     * Makes Swing lay the panel out again and redraw it, after {@code hScale} or {@code vScale} changes.
     */
    private void refreshSize() {
        revalidate(); // Tells the layout manager to call getPreferredSize() again and redo the layout.
        repaint();    // Asks Swing to call paintComponent() again soon.
    }

    /**
     * Returns the source column (or source row) of each piece across the panel, from left to right (or top to
     * bottom).
     * <p>
     * The first entry is always 0 and the last is always 2, with {@code middleCount} entries of 1 between
     * them.
     *
     * <pre>{@code
     * buildPieceOrder(0)  ->  [0, 2]
     * buildPieceOrder(3)  ->  [0, 1, 1, 1, 2]
     * }</pre>
     *
     * @param middleCount the number of middle pieces, which is {@code hScale} or {@code vScale}
     *
     * @return the source column or source row for each piece, in drawing order
     */
    private static int[] buildPieceOrder(int middleCount) {
        var order = new int[middleCount + 2];
        Arrays.fill(order, 1, order.length - 1, 1);
        order[order.length - 1] = 2;

        return order;
    }

    /**
     * Copies one piece of {@code src} onto the panel at full size, with its top-left corner at ({@code x},
     * {@code y}).
     * <p>
     * The {@code drawImage} overload used here takes two pairs of corners, each given as top-left then
     * bottom-right. The first pair places the piece on the panel, and the second pair selects the part of
     * {@code src} to copy. The last argument, an {@code ImageObserver}, only matters for images that are still
     * loading. A {@code BufferedImage} is always fully loaded, so the method passes null.
     *
     * @param g     the graphics context that {@code paintComponent} received
     * @param piece the position and size of the piece within {@code src}
     * @param x     the panel x at which the left edge of the piece is drawn
     * @param y     the panel y at which the top edge of the piece is drawn
     */
    private void drawPiece(Graphics g, Rectangle piece, int x, int y) {
        g.drawImage(
                src,
                x, y, x + piece.width, y + piece.height,
                piece.x, piece.y, piece.x + piece.width, piece.y + piece.height,
                null
        );
    }

}
