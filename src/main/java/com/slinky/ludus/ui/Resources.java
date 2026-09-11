package com.slinky.ludus.ui;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Opens files that are bundled with the application on the classpath.
 * <p>
 * A caller passes a path to {@link #getResource(String)} and reads from the stream it returns.
 *
 * <pre>{@code
 * try (var in = Resources.getResource("/assets/ui-elements/papers/regularpaper.png")) {
 *     var paper = ImageIO.read(in);
 * }
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
final class Resources {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final Pattern REPEATED_SLASHES = Pattern.compile("/{2,}");

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\

    /**
     * Thrown when the classpath contains no resource at a requested path.
     */
    public static class ResourceNotFoundException extends RuntimeException {

        /**
         * Creates an exception whose message states that {@code path} could not be located. For example,
         * {@code rnf("/assets/missing.png")} has the message {@code "/assets/missing.png could not be located"}.
         *
         * @param path the resource path that could not be located
         *
         * @return a new exception for {@code path}
         */
        static ResourceNotFoundException rnf(String path) {
            return new ResourceNotFoundException(path + " could not be located");
        }

        /**
         * Creates an exception with the given message.
         *
         * @param message the detail message
         */
        public ResourceNotFoundException(String message) {
            super(message);
        }

    }

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    private Resources() { }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\

    /**
     * Opens the classpath resource at the given path and returns it as a stream.
     * <p>
     * Every path is resolved from the root of the classpath, whether or not it starts with {@code /}.
     * Backslashes count as forward slashes, and repeated slashes count as one, so
     * {@code "assets\\ui-elements\\papers\\regularpaper.png"} and
     * {@code "//assets//ui-elements/papers/regularpaper.png"} both open
     * {@code /assets/ui-elements/papers/regularpaper.png}. The caller owns the returned stream and must close it.
     *
     * @param path the resource path, with or without a leading slash
     *
     * @return an open stream of the resource, never null
     *
     * @throws NullPointerException      if {@code path} is null
     * @throws IllegalArgumentException  if {@code path} is empty or contains only whitespace
     * @throws ResourceNotFoundException if the classpath contains no resource at {@code path}
     */
    public static InputStream getResource(String path) {
        var in = Resources.class.getResourceAsStream(normalise(path));
        if (in == null) throw ResourceNotFoundException.rnf(path);

        return in;
    }

    /**
     * Reads the image at the given path, which {@link #getResource(String)} resolves.
     *
     * @param path the resource path, with or without a leading slash
     *
     * @return the decoded image, or null where no installed {@link ImageIO} reader supports the file's format
     *
     * @throws NullPointerException      if {@code path} is null
     * @throws IllegalArgumentException  if {@code path} is empty or contains only whitespace
     * @throws ResourceNotFoundException if the classpath contains no resource at {@code path}
     * @throws UncheckedIOException      if reading the resource fails
     */
    public static BufferedImage readImage(String path) {
        try (var in = getResource(path)) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read image at " + path, ex);
        }
    }

    /**
     * Reads the TrueType font at the given path, which {@link #getResource(String)} resolves.
     * <p>
     * The returned font is 1 point in size. A caller derives the size it draws at, for example with
     * {@code readFont(path).deriveFont(24f)}. The font works from inside a jar, because the method reads it as a
     * stream rather than as a file on disk.
     *
     * @param path the resource path, with or without a leading slash
     *
     * @return the font, at a size of 1 point
     *
     * @throws NullPointerException      if {@code path} is null
     * @throws IllegalArgumentException  if {@code path} is empty or contains only whitespace, or if the resource is
     *                                   not a TrueType font
     * @throws ResourceNotFoundException if the classpath contains no resource at {@code path}
     * @throws UncheckedIOException      if reading the resource fails
     */
    public static Font readFont(String path) {
        try (var in = getResource(path)) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException ex) {
            throw new IllegalArgumentException(path + " is not a TrueType font", ex);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read font at " + path, ex);
        }
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Rewrites a resource path into the absolute form that {@link Class#getResourceAsStream(String)} resolves from
     * the root of the classpath. The method turns backslashes into forward slashes, collapses repeated slashes into
     * one, and adds a leading slash where the path has none.
     *
     * <pre>{@code
     * "/assets/papers/a.png"    ->  "/assets/papers/a.png"
     * "assets/papers/a.png"     ->  "/assets/papers/a.png"
     * "assets\\papers\\a.png"   ->  "/assets/papers/a.png"
     * "//assets//papers/a.png"  ->  "/assets/papers/a.png"
     * }</pre>
     *
     * @param path the resource path as the caller wrote it
     *
     * @return the path with forward slashes only, no repeated slashes, and one leading slash
     *
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is empty or contains only whitespace
     */
    private static String normalise(String path) {
        requireNonNull(path, "path must not be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }

        var forwardSlashes = path.replace('\\', '/');
        var singleSlashes  = REPEATED_SLASHES.matcher(forwardSlashes).replaceAll("/");

        return singleSlashes.startsWith("/") ? singleSlashes : "/" + singleSlashes;
    }

}
