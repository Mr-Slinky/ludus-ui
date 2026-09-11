package com.slinky.ludus.ui;

import com.slinky.ludus.ui.Surface.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link ImageSlicer}, pinning the regions it finds in the three 9-slice images on the classpath and
 * in small grid images that each test builds.
 * <p>
 * A built grid image contains {@code PIECE} by {@code PIECE} pixel pieces, separated by {@code GAP} transparent
 * pixels and surrounded by a {@code MARGIN} of transparent pixels, so every expected region follows from those
 * three constants.
 *
 * <p>
 * <b>TDD state.</b> Written after {@code ImageSlicer}, to pin its behaviour as it stands. Covered: the regions of
 * the wood, regular paper, special paper and banner images; grids of 1 by 1, 2 by 4, 3 by 3 and 5 by 2; the three counts;
 * pieces of uneven size in one column; pieces touching the image edges; a stray pixel touching a piece, clear of
 * both pieces, and fully transparent; a grid size that differs from the image; row and column counts below 1; a
 * null image; a fully transparent image; out-of-range region indices; and a fresh {@link Rectangle} per call.
 *
 * @author Claude Code
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
class ImageSlicerTest {

    private static final int PIECE  = 4;
    private static final int GAP    = 3;
    private static final int MARGIN = 2;

    @ParameterizedTest
    @CsvSource(textBlock = """
            # image,       x0, w0,  x1, w1,  x2, w2,  y0, h0,  y1, h1,  y2,  h2
            WOOD,          44, 84, 192, 64, 320, 84,  43, 85, 192, 64, 320, 103
            REGULAR_PAPER, 12, 52, 128, 64, 256, 52,  20, 44, 128, 64, 256,  45
            SPECIAL_PAPER,  9, 55, 128, 64, 256, 55,  20, 44, 128, 64, 256,  43
            BANNER,        28,100, 192, 64, 320, 84,  60, 68, 192, 64, 320, 111
            """)
    void testScan_withValidArgs_ReturnsRegionsMeasuredFromAssets(Type type,
            int x0, int w0, int x1, int w1, int x2, int w2,
            int y0, int h0, int y1, int h1, int y2, int h2) throws IOException {
        var image    = readAsset(type);
        var xs       = new int[] {x0, x1, x2};
        var widths   = new int[] {w0, w1, w2};
        var ys       = new int[] {y0, y1, y2};
        var heights  = new int[] {h0, h1, h2};
        var expected = new ArrayList<Rectangle>();
        for (var row = 0; row < 3; row++) {
            for (var column = 0; column < 3; column++) {
                expected.add(new Rectangle(xs[column], ys[row], widths[column], heights[row]));
            }
        }

        var actual = collectRegions(ImageSlicer.scan(image, 3, 3));

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "2, 4", "3, 3", "5, 2"})
    void testGetRegion_withValidArgs_ReturnsPiecePositions(int rows, int columns) {
        var image    = buildGridImage(rows, columns);
        var expected = new ArrayList<Rectangle>();
        for (var row = 0; row < rows; row++) {
            for (var column = 0; column < columns; column++) {
                expected.add(new Rectangle(derivePieceStart(column), derivePieceStart(row), PIECE, PIECE));
            }
        }

        var actual = collectRegions(ImageSlicer.scan(image, rows, columns));

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "2, 4", "3, 3", "5, 2"})
    void testGetRegionCount_withValidArgs_ReturnsRowsTimesColumns(int rows, int columns) {
        var image = buildGridImage(rows, columns);

        var slicer = ImageSlicer.scan(image, rows, columns);

        assertAll(
                () -> assertEquals(rows, slicer.getRowCount()),
                () -> assertEquals(columns, slicer.getColumnCount()),
                () -> assertEquals(rows * columns, slicer.getRegionCount())
        );
    }

    @Test
    void testScan_withEdgeCaseArgs_GivesEveryRegionInAColumnTheSameWidth() {
        var image = buildImage(16, 16,
                new Rectangle(2, 2, 4, 4), new Rectangle(9, 2, 4, 4),
                new Rectangle(1, 9, 3, 4), new Rectangle(9, 9, 4, 4));

        var slicer = ImageSlicer.scan(image, 2, 2);

        assertAll(
                () -> assertEquals(new Rectangle(1, 2, 5, 4), slicer.getRegion(0, 0)),
                () -> assertEquals(new Rectangle(1, 9, 5, 4), slicer.getRegion(1, 0))
        );
    }

    @Test
    void testScan_withEdgeCaseArgs_ReturnsRegionsTouchingImageEdges() {
        var image = buildImage(10, 10,
                new Rectangle(0, 0, 4, 4), new Rectangle(6, 0, 4, 4),
                new Rectangle(0, 6, 4, 4), new Rectangle(6, 6, 4, 4));
        var expected = List.of(
                new Rectangle(0, 0, 4, 4), new Rectangle(6, 0, 4, 4),
                new Rectangle(0, 6, 4, 4), new Rectangle(6, 6, 4, 4));

        var actual = collectRegions(ImageSlicer.scan(image, 2, 2));

        assertEquals(expected, actual);
    }

    @Test
    void testScan_withEdgeCaseArgs_WidensColumnForStrayPixelTouchingPiece() {
        var image = buildGridImage(3, 3);
        image.setRGB(MARGIN + PIECE, MARGIN, 0x01000000);

        var slicer = ImageSlicer.scan(image, 3, 3);

        assertAll(
                () -> assertEquals(new Rectangle(MARGIN, MARGIN, PIECE + 1, PIECE), slicer.getRegion(0, 0)),
                () -> assertEquals(new Rectangle(MARGIN, derivePieceStart(2), PIECE + 1, PIECE), slicer.getRegion(2, 0))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0x01000000, 0x80000000, 0xFF000000})
    void testScan_withEdgeCaseArgs_ThrowsIllegalArgumentExceptionForStrayPixelClearOfPieces(int argb) {
        var image = buildGridImage(3, 3);
        image.setRGB(MARGIN + PIECE + 1, MARGIN, argb);

        var ex = assertThrows(IllegalArgumentException.class, () -> ImageSlicer.scan(image, 3, 3));

        assertEquals("Expected 3 sequences of columns with visible pixels but found 4", ex.getMessage());
    }

    @Test
    void testScan_withEdgeCaseArgs_IgnoresColouredPixelWithZeroAlpha() {
        var image = buildGridImage(3, 3);
        image.setRGB(MARGIN + PIECE + 1, MARGIN, 0x00FF0000);

        var actual = collectRegions(ImageSlicer.scan(image, 3, 3));

        assertEquals(collectRegions(ImageSlicer.scan(buildGridImage(3, 3), 3, 3)), actual);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2, 3, Expected 2 sequences of rows with visible pixels but found 3
            4, 3, Expected 4 sequences of rows with visible pixels but found 3
            3, 2, Expected 2 sequences of columns with visible pixels but found 3
            3, 4, Expected 4 sequences of columns with visible pixels but found 3
            """)
    void testScan_withInvalidArgs_ThrowsIllegalArgumentExceptionWithFoundCount(int rows, int columns,
            String expectedMessage) {
        var image = buildGridImage(3, 3);

        var ex = assertThrows(IllegalArgumentException.class, () -> ImageSlicer.scan(image, rows, columns));

        assertEquals(expectedMessage, ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
             0,  3, 'rows and columns must be 1 or more, got 0 and 3'
             3,  0, 'rows and columns must be 1 or more, got 3 and 0'
            -1,  3, 'rows and columns must be 1 or more, got -1 and 3'
             3, -1, 'rows and columns must be 1 or more, got 3 and -1'
            """)
    void testScan_withInvalidArgs_ThrowsIllegalArgumentException(int rows, int columns, String expectedMessage) {
        var image = buildGridImage(3, 3);

        var ex = assertThrows(IllegalArgumentException.class, () -> ImageSlicer.scan(image, rows, columns));

        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testScan_withNullArgs_ThrowsNullPointerException() {
        var ex = assertThrows(NullPointerException.class, () -> ImageSlicer.scan(null, 3, 3));

        assertEquals("image must not be null", ex.getMessage());
    }

    @Test
    void testScan_withEmptyArgs_ThrowsIllegalArgumentException() {
        var image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        var ex = assertThrows(IllegalArgumentException.class, () -> ImageSlicer.scan(image, 3, 3));

        assertEquals("Expected 3 sequences of columns with visible pixels but found 0", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1", "3, 0", "0, 3"})
    void testGetRegion_withInvalidArgs_ThrowsIndexOutOfBoundsException(int row, int column) {
        var slicer = ImageSlicer.scan(buildGridImage(3, 3), 3, 3);

        assertThrows(IndexOutOfBoundsException.class, () -> slicer.getRegion(row, column));
    }

    @Test
    void testGetRegion_withValidArgs_ReturnsNewRectangleEachCall() {
        var slicer = ImageSlicer.scan(buildGridImage(3, 3), 3, 3);
        slicer.getRegion(0, 0).setBounds(99, 99, 99, 99);

        var actual = slicer.getRegion(0, 0);

        assertEquals(new Rectangle(MARGIN, MARGIN, PIECE, PIECE), actual);
    }

    private static BufferedImage readAsset(Type type) throws IOException {
        try (var in = Resources.getResource(type.getPath())) {
            return ImageIO.read(in);
        }
    }

    private static List<Rectangle> collectRegions(ImageSlicer slicer) {
        var regions = new ArrayList<Rectangle>();
        for (var row = 0; row < slicer.getRowCount(); row++) {
            for (var column = 0; column < slicer.getColumnCount(); column++) {
                regions.add(slicer.getRegion(row, column));
            }
        }
        return regions;
    }

    private static BufferedImage buildGridImage(int rows, int columns) {
        var pieces = new ArrayList<Rectangle>();
        for (var row = 0; row < rows; row++) {
            for (var column = 0; column < columns; column++) {
                pieces.add(new Rectangle(derivePieceStart(column), derivePieceStart(row), PIECE, PIECE));
            }
        }

        var width  = (2 * MARGIN) + (columns * PIECE) + ((columns - 1) * GAP);
        var height = (2 * MARGIN) + (rows * PIECE) + ((rows - 1) * GAP);
        return buildImage(width, height, pieces.toArray(Rectangle[]::new));
    }

    private static BufferedImage buildImage(int width, int height, Rectangle... pieces) {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g     = image.createGraphics();
        g.setColor(Color.RED);
        for (var piece : pieces) {
            g.fill(piece);
        }
        g.dispose();

        return image;
    }

    private static int derivePieceStart(int index) {
        return MARGIN + (index * (PIECE + GAP));
    }

}
