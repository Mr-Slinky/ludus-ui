package com.slinky.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The nine source regions of a 9-slice image, stored as three column spans and three row spans.
 * <p>
 * The image must contain nine pieces arranged in three rows and three columns, with a fully transparent gap
 * between each pair of neighbouring rows and each pair of neighbouring columns. A column span starts at the
 * leftmost visible pixel among the three pieces in that column and ends at the rightmost, and a row span does
 * the same from top to bottom. Every region in one column therefore has the same x and width, and every region
 * in one row has the same y and height, even where one piece is smaller than its neighbours.
 * <p>
 * A caller obtains a {@code ImageSlicer} from {@link #scan(BufferedImage)} and reads each region from
 * {@link #getRegion(int, int)}. Rows and columns are numbered 0 to 2 from the top-left, so
 * {@code getRegion(1, 1)} returns the centre region.
 *
 * <pre>{@code
 * BufferedImage woodTable = ...
 *
 * var slice   = ImageSlicer.scan(woodTable);
 * var topLeft = slice.getRegion(0, 0);    // x=44, y=43, width=84, height=85
 * var centre  = slice.getRegion(1, 1);     // x=192, y=192, width=64, height=64
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
    private static final int PIECES_PER_AXIS = 3;

    private static final int REGION_COUNT = 9;

    /**
     * Scans an image for the three column spans and three row spans of its nine pieces.
     * <p>
     * The method reads the alpha value of every pixel once, and counts a pixel as visible where its alpha is
     * above zero. A visible pixel marks both its pixel column and its pixel row as used.
     * {@link #findSpans(boolean[], String)} then finds the three column spans among the used columns, and the
     * three row spans among the used rows. A single faint pixel in a gap therefore marks that gap as used and
     * joins the pieces on either side of it, so {@code findSpans} finds only two spans along that axis and the
     * method throws.
     * <p>
     * In the 5 by 5 example below, {@code #} marks a visible pixel.
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
     * @param image the 9-slice image to scan
     *
     * @return the nine regions of {@code image}
     *
     * @throws NullPointerException     if {@code image} is null
     * @throws IllegalArgumentException if the image does not contain exactly three column spans and three row
     *                                  spans
     */
    static ImageSlicer scan(BufferedImage image) {
        requireNonNull(image, "image must not be null");

        var usedColumns = new boolean[image.getWidth()];
        var usedRows    = new boolean[image.getHeight()];
        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                // getRGB returns the pixel as 0xAARRGGBB, so shifting right by 24 leaves only the alpha, 0
                // to 255.
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    usedColumns[x] = true;
                    usedRows[y]    = true;
                }
            }
        }

        return new ImageSlicer(findSpans(usedColumns, "columns"), findSpans(usedRows, "rows"));
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
     * Returns the number of regions in a scanned image, which is always 9 (three rows of three columns).
     *
     * @return 9
     */
    public int getRegionCount() {
        return REGION_COUNT;
    }

    /**
     * Returns the source region at the given row and column, as a new {@link Rectangle} that the caller is free
     * to modify.
     *
     * @param row    the row index, from 0 at the top to 2 at the bottom
     * @param column the column index, from 0 at the left to 2 at the right
     *
     * @return the region's position and size within the scanned image
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code column} is outside 0 to 2
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
     * Returns one {@link Span} for each unbroken sequence of {@code true} entries in {@code used}.
     * <p>
     * Each entry in {@code used} represents one column of pixels (or one row of pixels), and is {@code true}
     * where that column contains a visible pixel. The {@code false} entries between sequences are the fully
     * transparent gaps that separate the nine pieces. Each {@code Span} starts at the first index in its
     * sequence, and its length equals the number of entries in the sequence.
     * <p>
     * In the first example below, the last sequence reaches the end of the array.
     *
     * <pre>{@code
     * used = F F T T F F T F T T  ->  returns [Span(2, 2), Span(6, 1), Span(8, 2)]
     * used = F T T F F            ->  throws IllegalArgumentException (1 sequence found)
     * }</pre>
     *
     * @param used one entry per column of pixels (or row of pixels), {@code true} where it contains a visible
     *             pixel
     * @param axis either {@code "columns"} or {@code "rows"}, for the exception message
     *
     * @return the three spans in index order, as an unmodifiable list
     *
     * @throws IllegalArgumentException if {@code used} does not contain exactly three sequences of {@code true}
     *                                  entries
     */
    private static List<Span> findSpans(boolean[] used, String axis) {
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

        if (spans.size() != PIECES_PER_AXIS) {
            throw new IllegalArgumentException(
                    "Expected %d sequences of %s with visible pixels but found %d".formatted(
                            PIECES_PER_AXIS, axis, spans.size()
                    )
            );
        }

        return List.copyOf(spans);
    }

}
