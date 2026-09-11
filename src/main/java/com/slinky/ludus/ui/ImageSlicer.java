package com.slinky.ludus.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The source regions of an image whose pieces are laid out in a grid, stored as column spans and row spans.
 * <p>
 * The image must contain its pieces in rows and columns, with a fully transparent gap between each pair of
 * neighbouring rows and each pair of neighbouring columns. The caller states how many rows and columns of
 * pieces to expect. A column span starts at the leftmost visible pixel among the pieces in that column and ends
 * at the rightmost, and a row span does the same from top to bottom. Every region in one column therefore has
 * the same x and width, and every region in one row has the same y and height, even where one piece is smaller
 * than its neighbours.
 * <p>
 * A caller obtains an {@code ImageSlicer} from {@link #scan(BufferedImage, int, int)} and reads each region
 * from {@link #getRegion(int, int)}. Rows and columns are numbered from 0 at the top-left, so in a 3 by 3 grid
 * {@code getRegion(1, 1)} returns the centre region.
 *
 * <pre>{@code
 * BufferedImage woodTable = ...
 *
 * var slice   = ImageSlicer.scan(woodTable, 3, 3);
 * var topLeft = slice.getRegion(0, 0);    // x=44, y=43, width=84, height=85
 * var centre  = slice.getRegion(1, 1);    // x=192, y=192, width=64, height=64
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
final class ImageSlicer {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    /**
     * Scans an image for the column spans and row spans of a grid of pieces with the given number of rows and
     * columns.
     * <p>
     * The method reads the alpha value of every pixel once, and counts a pixel as visible where its alpha is
     * above zero. A visible pixel marks both the column of pixels and the row of pixels that contain it as
     * used. {@link #findSpans(boolean[], int, String)} then finds {@code columns} column spans among the used
     * columns of pixels, and {@code rows} row spans among the used rows of pixels.
     * <p>
     * A single faint pixel in a gap therefore changes the result. Where the pixel touches a piece, it widens
     * the span of that piece by one pixel. Where the pixel stands clear of the pieces on both sides, it forms a
     * span of its own, so {@code findSpans} finds one span more than expected along that axis and the method
     * throws.
     * <p>
     * In the 5 by 5 example below, {@code #} marks a visible pixel, and the caller passes 3 rows and 3
     * columns.
     *
     * <pre>{@code
     *          x = 0 1 2 3 4
     *   y = 0      # . # . #
     *   y = 1      . . . . .
     *   y = 2      # . # . .
     *   y = 3      . . . . .
     *   y = 4      # . . . #
     *
     *   usedColumns = T F T F T  ->  columns [Span(0, 1), Span(2, 1), Span(4, 1)]
     *   usedRows    = T F T F T  ->  rows    [Span(0, 1), Span(2, 1), Span(4, 1)]
     * }</pre>
     *
     * @param image   the image to scan
     * @param rows    the number of rows of pieces the image must contain, 1 or more
     * @param columns the number of columns of pieces the image must contain, 1 or more
     *
     * @return the regions of {@code image}, {@code rows} high and {@code columns} wide
     *
     * @throws NullPointerException     if {@code image} is null
     * @throws IllegalArgumentException if {@code rows} or {@code columns} is less than 1, or if the image does
     *                                  not contain exactly {@code rows} row spans and {@code columns} column
     *                                  spans
     */
    static ImageSlicer scan(BufferedImage image, int rows, int columns) {
        requireNonNull(image, "image must not be null");
        if (rows < 1 || columns < 1) {
            throw new IllegalArgumentException(
                    "rows and columns must be 1 or more, got %d and %d".formatted(rows, columns)
            );
        }

        var usedColumns = new boolean[image.getWidth()];
        var usedRows    = new boolean[image.getHeight()];
        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                // getRGB returns the pixel as 0xAARRGGBB, so shifting right by 24 leaves only the alpha,
                // 0 to 255.
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    usedColumns[x] = true;
                    usedRows[y]    = true;
                }
            }
        }

        return new ImageSlicer(
                findSpans(usedColumns, columns, "columns"),
                findSpans(usedRows, rows, "rows")
        );
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    private record Span(int start, int length) {}

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final List<Span> columns;
    private final List<Span> rows;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    private ImageSlicer(List<Span> columns, List<Span> rows) {
        this.columns = columns;
        this.rows    = rows;
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Returns the number of rows of pieces in the scanned image, as passed to
     * {@link #scan(BufferedImage, int, int)}.
     *
     * @return the number of rows, 1 or more
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns the number of columns of pieces in the scanned image, as passed to
     * {@link #scan(BufferedImage, int, int)}.
     *
     * @return the number of columns, 1 or more
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Returns the number of regions in the scanned image, which is {@link #getRowCount()} times
     * {@link #getColumnCount()}. A 3 by 3 grid has 9 regions.
     *
     * @return the number of regions, 1 or more
     */
    public int getRegionCount() {
        return rows.size() * columns.size();
    }

    /**
     * Returns the source region at the given row and column, as a new {@link Rectangle} that the caller is free
     * to modify.
     *
     * @param row    the row index, from 0 at the top to {@code getRowCount() - 1} at the bottom
     * @param column the column index, from 0 at the left to {@code getColumnCount() - 1} at the right
     *
     * @return the region's position and size within the scanned image
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code column} is outside the grid
     */
    Rectangle getRegion(int row, int column) {
        var rowSpan    = rows.get(row);
        var columnSpan = columns.get(column);

        return new Rectangle(
                columnSpan.start(),
                rowSpan.start(),
                columnSpan.length(),
                rowSpan.length()
        );
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Returns one {@link Span} for each unbroken sequence of {@code true} entries in {@code used}, and checks
     * that the number of sequences matches {@code expected}.
     * <p>
     * Each entry in {@code used} represents one column of pixels (or one row of pixels), and is {@code true}
     * where that column contains a visible pixel. The {@code false} entries between sequences are the fully
     * transparent gaps that separate the pieces. Each {@code Span} starts at the first index in its sequence,
     * and its length equals the number of entries in the sequence.
     * <p>
     * In the examples below, the last sequence reaches the end of the array. The second call expects one
     * sequence fewer than the array contains.
     *
     * <pre>{@code
     * used = F F T T F F T F T T, expected = 3  ->  returns [Span(2, 2), Span(6, 1), Span(8, 2)]
     * used = F F T T F F T F T T, expected = 2  ->  throws IllegalArgumentException (3 sequences found)
     * }</pre>
     *
     * @param used     one entry per column of pixels (or row of pixels), {@code true} where it contains a
     *                 visible pixel
     * @param expected the number of sequences {@code used} must contain
     * @param axis     either {@code "columns"} or {@code "rows"}, for the exception message
     *
     * @return {@code expected} spans in index order, as an unmodifiable list
     *
     * @throws IllegalArgumentException if {@code used} does not contain exactly {@code expected} sequences of
     *                                  {@code true} entries
     */
    private static List<Span> findSpans(boolean[] used, int expected, String axis) {
        var spans = new ArrayList<Span>();
        var start = -1; // First index of the sequence in progress, or -1 while the loop is in a gap.

        // The loop takes one step past the end of used, and treats that extra step as a false entry. A sequence
        // that reaches the last index is therefore closed in the same way as every other sequence.
        for (var i = 0; i <= used.length; i++) {
            var inside = i < used.length && used[i];
            if (inside && start < 0) {
                // A true entry with no sequence in progress starts a new sequence.
                start = i;
            } else if (!inside && start >= 0) {
                // A false entry with a sequence in progress ends that sequence.
                // The sequence covered indices start to i - 1, so its length is i - start.
                spans.add(new Span(start, i - start));
                start = -1;
            }
        }

        if (spans.size() != expected) {
            throw new IllegalArgumentException(
                    "Expected %d sequences of %s with visible pixels but found %d".formatted(
                            expected, axis, spans.size()
                    )
            );
        }

        return List.copyOf(spans);
    }

}
