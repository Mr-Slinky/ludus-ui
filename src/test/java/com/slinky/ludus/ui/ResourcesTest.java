package com.slinky.ludus.ui;

import com.slinky.ludus.ui.Resources.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link Resources}, pinning how {@link Resources#getResource(String)} resolves a path and how it
 * rejects a path it cannot open.
 * <p>
 * Every accepted spelling of a path is checked by reading the bytes it opens and comparing them with the bytes of
 * {@code REGULAR_PAPER}, read straight from the classpath.
 *
 * <p>
 * <b>TDD state.</b> Written before {@code normalise}, {@code readImage} and {@code readFont} were implemented.
 * Covered: a leading slash, no leading slash, backslashes, mixed slashes and repeated slashes; a path with no
 * resource behind it; a null path; empty and blank paths; reading an image, with and without a resource behind the
 * path; and reading each of the four Pixelify Sans weights, a missing font, and a file that is not a font.
 *
 * @author Claude Code
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
class ResourcesTest {

    private static final String REGULAR_PAPER = "/assets/ui-elements/papers/regularpaper.png";

    @ParameterizedTest
    @ValueSource(strings = {
            "/assets/ui-elements/papers/regularpaper.png",
            "assets/ui-elements/papers/regularpaper.png",
            "assets\\ui-elements\\papers\\regularpaper.png",
            "\\assets\\ui-elements\\papers\\regularpaper.png",
            "//assets//ui-elements///papers/regularpaper.png",
            "assets/ui-elements\\papers//regularpaper.png"
    })
    void testGetResource_withValidArgs_OpensResourceFromClasspathRoot(String path) throws IOException {
        var expected = readBytes(REGULAR_PAPER);

        byte[] actual;
        try (var in = Resources.getResource(path)) {
            actual = in.readAllBytes();
        }

        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetResource_withInvalidArgs_ThrowsResourceNotFoundException() {
        var ex = assertThrows(ResourceNotFoundException.class, () -> Resources.getResource("/assets/missing.png"));

        assertEquals("/assets/missing.png could not be located", ex.getMessage());
    }

    @Test
    void testGetResource_withNullArgs_ThrowsNullPointerException() {
        var ex = assertThrows(NullPointerException.class, () -> Resources.getResource(null));

        assertEquals("path must not be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void testGetResource_withEmptyArgs_ThrowsIllegalArgumentException(String path) {
        var ex = assertThrows(IllegalArgumentException.class, () -> Resources.getResource(path));

        assertEquals("path must not be blank", ex.getMessage());
    }

    @Test
    void testReadImage_withValidArgs_ReturnsDecodedImage() {
        var image = Resources.readImage(REGULAR_PAPER);

        assertAll(
                () -> assertEquals(320, image.getWidth()),
                () -> assertEquals(320, image.getHeight())
        );
    }

    @Test
    void testReadImage_withInvalidArgs_ThrowsResourceNotFoundException() {
        var ex = assertThrows(ResourceNotFoundException.class, () -> Resources.readImage("/assets/missing.png"));

        assertEquals("/assets/missing.png could not be located", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "PixelifySans-Regular.ttf,  Pixelify Sans Regular",
            "PixelifySans-Medium.ttf,   Pixelify Sans Medium",
            "PixelifySans-SemiBold.ttf, Pixelify Sans SemiBold",
            "PixelifySans-Bold.ttf,     Pixelify Sans Bold"
    })
    void testReadFont_withValidArgs_ReturnsFontAtSizeOne(String fileName, String expectedFontName) {
        var font = Resources.readFont("/assets/fonts/pixelify-sans/" + fileName);

        assertAll(
                () -> assertEquals(expectedFontName, font.getFontName()),
                () -> assertEquals(1f, font.getSize2D())
        );
    }

    @Test
    void testReadFont_withInvalidArgs_ThrowsResourceNotFoundException() {
        var ex = assertThrows(ResourceNotFoundException.class, () -> Resources.readFont("/assets/missing.ttf"));

        assertEquals("/assets/missing.ttf could not be located", ex.getMessage());
    }

    @Test
    void testReadFont_withEdgeCaseArgs_ThrowsIllegalArgumentExceptionForFileThatIsNotAFont() {
        var ex = assertThrows(IllegalArgumentException.class, () -> Resources.readFont(REGULAR_PAPER));

        assertEquals(REGULAR_PAPER + " is not a TrueType font", ex.getMessage());
    }

    private static byte[] readBytes(String absolutePath) throws IOException {
        try (var in = ResourcesTest.class.getResourceAsStream(absolutePath)) {
            return in.readAllBytes();
        }
    }

}
