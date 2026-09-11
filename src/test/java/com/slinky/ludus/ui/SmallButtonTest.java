package com.slinky.ludus.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link SmallButton}, pinning how the {@code radians} argument turns the icon.
 * <p>
 * Every expected icon comes from the {@link SmallButton.Symbol} image on the classpath, with each pixel moved to
 * where a clockwise quarter turn puts it. The icons are square, so a turned icon keeps its width and height.
 *
 * <p>
 * <b>TDD state.</b> Written before the rotation. Covered: every whole number of quarter turns from -1 to 5, given
 * in degrees and converted with {@link Math#toRadians(double)}; the same quarter turn through all four factory
 * methods; angles between quarter turns, NaN and infinity; and a null symbol.
 *
 * @author Claude Code
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
class SmallButtonTest {

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    @ParameterizedTest
    @CsvSource(textBlock = """
            # degrees, clockwise quarter turns
               0,      0
              90,      1
             180,      2
             270,      3
             360,      0
             450,      1
             -90,      3
            """)
    void testRedSquare_withValidArgs_ReturnsIconTurnedClockwise(int degrees, int quarterTurns) {
        var source = Resources.readImage(SmallButton.Symbol.LEFT_ARROW.getPath());

        var icon = readIcon(SmallButton.redSquare(SmallButton.Symbol.LEFT_ARROW, Math.toRadians(degrees)));

        assertAll(
                () -> assertEquals(source.getWidth(), icon.getWidth()),
                () -> assertEquals(source.getHeight(), icon.getHeight()),
                () -> assertArrayEquals(turnClockwise(source, quarterTurns), readPixels(icon))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    void testEveryFactory_withValidArgs_ReturnsIconTurnedClockwise(
            BiFunction<SmallButton.Symbol, Double, SmallButton> factory) {
        var source = Resources.readImage(SmallButton.Symbol.LEFT_ARROW.getPath());

        var icon = readIcon(factory.apply(SmallButton.Symbol.LEFT_ARROW, Math.PI / 2));

        assertArrayEquals(turnClockwise(source, 1), readPixels(icon));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, Math.PI / 4, -Math.PI / 3, Double.NaN, Double.POSITIVE_INFINITY})
    void testRedSquare_withInvalidArgs_ThrowsIllegalArgumentException(double radians) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> SmallButton.redSquare(SmallButton.Symbol.CROSS, radians));

        assertEquals("radians must be a multiple of Math.PI / 2, got " + radians, ex.getMessage());
    }

    @Test
    void testBlueRound_withNullArgs_ThrowsNullPointerException() {
        var ex = assertThrows(NullPointerException.class, () -> SmallButton.blueRound(null, 0));

        assertEquals("symbol must not be null", ex.getMessage());
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    private static Stream<Arguments> provideFactories() {
        return Stream.of(
                Arguments.of((BiFunction<SmallButton.Symbol, Double, SmallButton>) SmallButton::blueRound),
                Arguments.of((BiFunction<SmallButton.Symbol, Double, SmallButton>) SmallButton::blueSquare),
                Arguments.of((BiFunction<SmallButton.Symbol, Double, SmallButton>) SmallButton::redRound),
                Arguments.of((BiFunction<SmallButton.Symbol, Double, SmallButton>) SmallButton::redSquare)
        );
    }

    private static BufferedImage readIcon(SmallButton button) {
        return (BufferedImage) ((ImageIcon) button.getIcon()).getImage();
    }

    private static int[] readPixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    /**
     * Returns the pixels of a square image turned clockwise, row by row from the top-left. Each entry reads the
     * source pixel that a clockwise turn moves to that position.
     */
    private static int[] turnClockwise(BufferedImage source, int quarterTurns) {
        var size   = source.getWidth();
        var last   = size - 1;
        var pixels = new int[size * size];
        for (var y = 0; y < size; y++) {
            for (var x = 0; x < size; x++) {
                pixels[(y * size) + x] = switch (quarterTurns) {
                    case 0  -> source.getRGB(x, y);
                    case 1  -> source.getRGB(y, last - x);
                    case 2  -> source.getRGB(last - x, last - y);
                    case 3  -> source.getRGB(last - y, x);
                    default -> throw new IllegalArgumentException("quarterTurns must be 0 to 3, got " + quarterTurns);
                };
            }
        }
        return pixels;
    }

}
