package com.slinky.ui;

import java.io.InputStream;

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
     * A path that starts with {@code /} is resolved from the root of the classpath. Any other path is resolved
     * relative to the {@code com.slinky.ui} package, so {@code "icon.png"} opens
     * {@code com/slinky/ui/icon.png}. The caller owns the returned stream and must close it.
     *
     * @param path the resource path, either absolute (starting with {@code /}) or relative to this package
     *
     * @return an open stream of the resource, never null
     *
     * @throws NullPointerException      if {@code path} is null
     * @throws ResourceNotFoundException if the classpath contains no resource at {@code path}
     */
    public static InputStream getResource(String path) {
        var in = Resources.class.getResourceAsStream(normalise(path));
        if (in == null) throw ResourceNotFoundException.rnf(path);

        return in;
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    private static String normalise(String path) {
        return path; // TODO
    }

}
