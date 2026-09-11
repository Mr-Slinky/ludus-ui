package com.slinky.ui;

import com.slinky.ui.Resources.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link SliceRenderer}, pinning the size it measures, the order and position in which it paints pieces,
 * and the origin it reports.
 * <p>
 * A built grid image contains one piece per column and row, separated by {@code GAP} transparent pixels and
 * surrounded by a {@code MARGIN} of transparent pixels. Every pixel of a piece has the colour
 * {@code 0xFFccrr00}, where {@code cc} is the piece's source column and {@code rr} its source row, so a test reads
 * back which piece the renderer drew at any pixel.
 *
 * <p>
 * <b>TDD state.</b> Written after {@code SliceRenderer}, to pin its behaviour as it stands. Covered: the sizes and
 * origins of the panel and big button images; the repeat rule for 1, 2, 3 and 4 pieces, measured and painted;
 * pieces of uneven size painted side by side; painting that covers exactly the measured area; negative scales; a
 * null image, a grid that differs from the image, and a missing path; and a fresh {@link Point} per call.
 *
 * @author Claude Code
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
class SliceRendererTest {

    private static final int PIECE  = 4;
    private static final int GAP    = 3;
    private static final int MARGIN = 2;

    @ParameterizedTest
    @CsvSource(textBlock = """
            # path,                                                hScale, vScale, width, height
            /assets/ui-elements/wood-table/woodtable.png,               3,      1,   360,    252
            /assets/ui-elements/wood-table/woodtable.png,               0,      0,   168,    188
            /assets/ui-elements/papers/regularpaper.png,                0,      0,   104,     89
            /assets/ui-elements/papers/specialpaper.png,                3,      2,   302,    215
            /assets/ui-elements/buttons/bigbluebutton_regular.png,      1,      0,   154,     94
            /assets/ui-elements/buttons/bigbluebutton_pressed.png,      1,      0,   164,     85
            """)
    void testMeasure_withValidArgs_ReturnsSizeOfAssets(String path, int hScale, int vScale, int width,
            int height) {
        var renderer = SliceRenderer.load(path, 3, 3);

        var actual = renderer.measure(hScale, vScale);

        assertEquals(new Dimension(width, height), actual);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # piece sizes, scale, expected size
            4,             0,     4
            4,             5,     4
            4 6,           3,     10
            4 6 8,         0,     12
            4 6 8,         2,     24
            4 6 7 8,       2,     38
            """)
    void testMeasure_withValidArgs_RepeatsPiecesBetweenFirstAndLast(String pieceSizes, int scale, int expectedSize) {
        var sizes    = parseInts(pieceSizes);
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), sizes.length, sizes.length);

        var actual = renderer.measure(scale, scale);

        assertEquals(new Dimension(expectedSize, expectedSize), actual);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # pieces, scale, expected order
            1,        5,     0
            2,        2,     0 1
            3,        0,     0 2
            3,        3,     0 1 1 1 2
            4,        2,     0 1 2 1 2 3
            """)
    void testPaint_withValidArgs_DrawsPiecesInExpectedOrder(int pieces, int scale, String expectedOrder) {
        var sizes    = IntStream.generate(() -> PIECE).limit(pieces).toArray();
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), pieces, pieces);
        var size     = renderer.measure(scale, scale);
        var canvas   = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        var g        = canvas.createGraphics();

        renderer.paint(g, scale, scale);
        g.dispose();

        var expected = toList(parseInts(expectedOrder));
        var columns  = new ArrayList<Integer>();
        var rows     = new ArrayList<Integer>();
        for (var i = 0; i < expected.size(); i++) {
            columns.add(decodeColumn(canvas.getRGB((i * PIECE) + (PIECE / 2), PIECE / 2)));
            rows.add(decodeRow(canvas.getRGB(PIECE / 2, (i * PIECE) + (PIECE / 2))));
        }
        assertAll(
                () -> assertEquals(expected, columns),
                () -> assertEquals(expected, rows)
        );
    }

    @Test
    void testPaint_withEdgeCaseArgs_PlacesUnevenPiecesSideBySide() {
        var sizes    = new int[] {4, 6, 8};
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), 3, 3);
        var canvas   = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        var g        = canvas.createGraphics();

        renderer.paint(g, 1, 1);
        g.dispose();

        var edges    = new int[] {0, 3, 4, 9, 10, 17};
        var expected = List.of(0, 0, 1, 1, 2, 2);
        var columns  = Arrays.stream(edges).mapToObj(x -> decodeColumn(canvas.getRGB(x, 0))).toList();
        var rows     = Arrays.stream(edges).mapToObj(y -> decodeRow(canvas.getRGB(0, y))).toList();
        assertAll(
                () -> assertEquals(expected, columns),
                () -> assertEquals(expected, rows)
        );
    }

    @Test
    void testPaint_withValidArgs_CoversExactlyTheMeasuredArea() {
        var sizes    = new int[] {PIECE, PIECE, PIECE};
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), 3, 3);
        var size     = renderer.measure(1, 1);
        var canvas   = new BufferedImage(size.width + 1, size.height + 1, BufferedImage.TYPE_INT_ARGB);
        var g        = canvas.createGraphics();

        renderer.paint(g, 1, 1);
        g.dispose();

        assertAll(
                () -> assertEquals(255, readAlpha(canvas, 0, 0)),
                () -> assertEquals(255, readAlpha(canvas, size.width - 1, size.height - 1)),
                () -> assertEquals(0, readAlpha(canvas, size.width, 0)),
                () -> assertEquals(0, readAlpha(canvas, 0, size.height))
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            /assets/ui-elements/buttons/bigbluebutton_regular.png, 19, 17
            /assets/ui-elements/buttons/bigbluebutton_pressed.png, 14, 28
            /assets/ui-elements/wood-table/woodtable.png,          44, 43
            """)
    void testGetOrigin_withValidArgs_ReturnsPositionOfFirstPiece(String path, int x, int y) {
        var renderer = SliceRenderer.load(path, 3, 3);

        var actual = renderer.getOrigin();

        assertEquals(new Point(x, y), actual);
    }

    @Test
    void testGetOrigin_withValidArgs_ReturnsNewPointEachCall() {
        var sizes    = new int[] {PIECE, PIECE, PIECE};
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), 3, 3);
        renderer.getOrigin().setLocation(99, 99);

        var actual = renderer.getOrigin();

        assertEquals(new Point(MARGIN, MARGIN), actual);
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1"})
    void testMeasure_withInvalidArgs_ThrowsIllegalArgumentException(int hScale, int vScale) {
        var sizes    = new int[] {PIECE, PIECE, PIECE};
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), 3, 3);

        var ex = assertThrows(IllegalArgumentException.class, () -> renderer.measure(hScale, vScale));

        assertEquals("scale must be 0 or more, got -1", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1"})
    void testPaint_withInvalidArgs_ThrowsIllegalArgumentException(int hScale, int vScale) {
        var sizes    = new int[] {PIECE, PIECE, PIECE};
        var renderer = new SliceRenderer(buildGridImage(sizes, sizes), 3, 3);
        var g        = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();

        var ex = assertThrows(IllegalArgumentException.class, () -> renderer.paint(g, hScale, vScale));

        assertEquals("scale must be 0 or more, got -1", ex.getMessage());
    }

    @Test
    void testConstructor_withNullArgs_ThrowsNullPointerException() {
        var ex = assertThrows(NullPointerException.class, () -> new SliceRenderer(null, 3, 3));

        assertEquals("image must not be null", ex.getMessage());
    }

    @Test
    void testConstructor_withInvalidArgs_ThrowsIllegalArgumentException() {
        var sizes = new int[] {PIECE, PIECE, PIECE};
        var image = buildGridImage(sizes, sizes);

        var ex = assertThrows(IllegalArgumentException.class, () -> new SliceRenderer(image, 2, 3));

        assertEquals("Expected 2 sequences of rows with visible pixels but found 3", ex.getMessage());
    }

    @Test
    void testLoad_withInvalidArgs_ThrowsResourceNotFoundException() {
        var ex = assertThrows(ResourceNotFoundException.class, () -> SliceRenderer.load("/assets/missing.png", 3, 3));

        assertEquals("/assets/missing.png could not be located", ex.getMessage());
    }

    private static BufferedImage buildGridImage(int[] columnWidths, int[] rowHeights) {
        var width  = (2 * MARGIN) + IntStream.of(columnWidths).sum() + ((columnWidths.length - 1) * GAP);
        var height = (2 * MARGIN) + IntStream.of(rowHeights).sum() + ((rowHeights.length - 1) * GAP);
        var image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        var top = MARGIN;
        for (var row = 0; row < rowHeights.length; row++) {
            var left = MARGIN;
            for (var column = 0; column < columnWidths.length; column++) {
                fillPiece(image, left, top, columnWidths[column], rowHeights[row], encodePiece(column, row));
                left += columnWidths[column] + GAP;
            }
            top += rowHeights[row] + GAP;
        }

        return image;
    }

    private static void fillPiece(BufferedImage image, int left, int top, int width, int height, int argb) {
        for (var y = top; y < top + height; y++) {
            for (var x = left; x < left + width; x++) {
                image.setRGB(x, y, argb);
            }
        }
    }

    private static int encodePiece(int column, int row) {
        return 0xFF000000 | (column << 16) | (row << 8);
    }

    private static int decodeColumn(int argb) {
        return (argb >> 16) & 0xFF;
    }

    private static int decodeRow(int argb) {
        return (argb >> 8) & 0xFF;
    }

    private static int readAlpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }

    private static int[] parseInts(String text) {
        return Arrays.stream(text.trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
    }

    private static List<Integer> toList(int[] values) {
        return IntStream.of(values).boxed().toList();
    }

}
