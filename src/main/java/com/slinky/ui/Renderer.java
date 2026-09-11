package com.slinky.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.IntStream;

/**
 * Draws an image whose pieces are laid out in a grid, stretched by repeating the pieces between its edges.
 * <p>
 * A {@code Renderer} stores an image together with the regions that {@link ImageSlicer} found in it.
 * <p>
 * {@link #measure(int, int)} and {@link #paint(Graphics, int, int)} each take a horizontal scale, {@code hScale},
 * and a vertical scale, {@code vScale}. The renderer draws the first column of pieces once, then the columns
 * between the first and the last, in order, {@code hScale} times over, then the last column once. Rows follow the
 * same rule with {@code vScale}. The examples below list the source column drawn at each position across the
 * component, from left to right.
 *
 * <pre>{@code
 * 3 columns, hScale 2  ->  0, 1, 1, 2
 * 4 columns, hScale 2  ->  0, 1, 2, 1, 2, 3
 * 2 columns, hScale 2  ->  0, 1
 * }</pre>
 * <p>
 * For an image with 1 or 2 columns, the renderer draws each column once, whatever {@code hScale} is.
 * <p>
 * A Swing component creates a {@code Renderer} once, returns {@link #measure(int, int)} from
 * {@code getPreferredSize()}, and calls {@link #paint(Graphics, int, int)} from {@code paintComponent(Graphics)}.
 *
 * <pre>{@code
 * var renderer = Renderer.load("/assets/ui-elements/wood-table/woodtable.png", 3, 3);
 *
 * renderer.measure(3, 1);     // 360 x 252
 * renderer.paint(g, 3, 1);    // draws the table at 360 x 252, starting at (0, 0)
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
final class Renderer {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    /**
     * Reads the image at the given classpath path and creates a {@code Renderer} for it.
     *
     * @param path    the classpath path of the image, with or without a leading slash
     * @param rows    the number of rows of pieces the image contains, 1 or more
     * @param columns the number of columns of pieces the image contains, 1 or more
     *
     * @return a new renderer for the image
     *
     * @throws Resources.ResourceNotFoundException if the classpath contains no file at {@code path}
     * @throws UncheckedIOException                if reading the file fails
     * @throws NullPointerException                if no installed {@link ImageIO} reader supports the file's
     *                                             format
     * @throws IllegalArgumentException            if {@code rows} or {@code columns} is less than 1, or if the
     *                                             image does not contain that grid of pieces
     */
    static Renderer load(String path, int rows, int columns) {
        return new Renderer(readImage(path), rows, columns);
    }

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final BufferedImage image;
    private final ImageSlicer   slicer;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a renderer for an image that is already loaded.
     *
     * @param image   the image to draw
     * @param rows    the number of rows of pieces the image contains, 1 or more
     * @param columns the number of columns of pieces the image contains, 1 or more
     *
     * @throws NullPointerException     if {@code image} is null
     * @throws IllegalArgumentException if {@code rows} or {@code columns} is less than 1, or if the image does
     *                                  not contain that grid of pieces
     */
    Renderer(BufferedImage image, int rows, int columns) {
        this.slicer = ImageSlicer.scan(image, rows, columns);
        this.image  = image;
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Returns the size at which {@link #paint(Graphics, int, int)} draws every piece at full size, for the same
     * two scales.
     * <p>
     * The width adds up the width of every column in drawing order, and the height adds up the height of every
     * row in drawing order.
     *
     * @param hScale the number of times the columns between the first and the last repeat, 0 or more
     * @param vScale the number of times the rows between the first and the last repeat, 0 or more
     *
     * @return the drawn width and height, in pixels
     *
     * @throws IllegalArgumentException if {@code hScale} or {@code vScale} is negative
     */
    Dimension measure(int hScale, int vScale) {
        var width = 0;
        for (var column : buildPieceOrder(slicer.getColumnCount(), hScale)) {
            width += slicer.getRegion(0, column).width;
        }

        var height = 0;
        for (var row : buildPieceOrder(slicer.getRowCount(), vScale)) {
            height += slicer.getRegion(row, 0).height;
        }

        return new Dimension(width, height);
    }

    /**
     * Draws every piece at full size, with the top-left corner of the first piece at (0, 0) of {@code g}.
     *
     * @param g      the graphics context to draw on, usually the one Swing passes to {@code paintComponent}
     * @param hScale the number of times the columns between the first and the last repeat, 0 or more
     * @param vScale the number of times the rows between the first and the last repeat, 0 or more
     *
     * @throws IllegalArgumentException if {@code hScale} or {@code vScale} is negative
     */
    void paint(Graphics g, int hScale, int vScale) {
        var columns = buildPieceOrder(slicer.getColumnCount(), hScale);

        var y = 0;
        for (var row : buildPieceOrder(slicer.getRowCount(), vScale)) {
            var x = 0;
            for (var column : columns) {
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
     * @param path the classpath path of the image, with or without a leading slash
     *
     * @return the decoded image, or null where no installed {@link ImageIO} reader supports the file's format
     *
     * @throws Resources.ResourceNotFoundException if the classpath contains no file at {@code path}
     * @throws UncheckedIOException                if reading the file fails
     */
    private static BufferedImage readImage(String path) {
        try (var in = Resources.getResource(path)) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read image at " + path, ex);
        }
    }

    /**
     * Returns the source column (or source row) of each piece along one axis, in drawing order.
     * <p>
     * The first entry is always 0 and the last is always {@code count - 1}. Between them, the indices 1 to
     * {@code count - 2} repeat as a block {@code scale} times. An axis with fewer than 3 pieces has no block
     * between its first and last piece, so the result lists each index once.
     *
     * <pre>{@code
     * buildPieceOrder(3, 0)  ->  [0, 2]
     * buildPieceOrder(3, 3)  ->  [0, 1, 1, 1, 2]
     * buildPieceOrder(4, 2)  ->  [0, 1, 2, 1, 2, 3]
     * buildPieceOrder(1, 5)  ->  [0]
     * }</pre>
     *
     * @param count the number of pieces along the axis, 1 or more
     * @param scale the number of times the pieces between the first and the last repeat, 0 or more
     *
     * @return the source column or source row for each piece, in drawing order
     *
     * @throws IllegalArgumentException if {@code scale} is negative
     */
    private static int[] buildPieceOrder(int count, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be 0 or more, got %d".formatted(scale));
        }

        if (count < 3) {
            return IntStream.range(0, count).toArray();
        }

        var middle = count - 2;
        var order  = new int[2 + (middle * scale)];
        for (var i = 0; i < middle * scale; i++) {
            order[1 + i] = 1 + (i % middle); // Walks through the middle block, then starts it again.
        }
        order[order.length - 1] = count - 1;

        return order;
    }

    /**
     * Copies one piece of the image onto {@code g} at full size, with its top-left corner at ({@code x},
     * {@code y}).
     * <p>
     * The {@code drawImage} overload used here takes two pairs of corners, each given as top-left then
     * bottom-right. The first pair places the piece on {@code g}, and the second pair selects the part of the
     * image to copy. The last argument, an {@code ImageObserver}, only matters for images that are still loading.
     * A {@code BufferedImage} is always fully loaded, so the method passes null.
     *
     * @param g     the graphics context to draw on
     * @param piece the position and size of the piece within the image
     * @param x     the x on {@code g} at which the left edge of the piece is drawn
     * @param y     the y on {@code g} at which the top edge of the piece is drawn
     */
    private void drawPiece(Graphics g, Rectangle piece, int x, int y) {
        g.drawImage(
                image,
                x, y, x + piece.width, y + piece.height,
                piece.x, piece.y, piece.x + piece.width, piece.y + piece.height,
                null
        );
    }

}
