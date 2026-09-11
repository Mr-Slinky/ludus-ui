package com.slinky.ludus.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A {@link JButton} that draws itself from one of the big button images, stretched by repeating its middle pieces.
 * <p>
 * Each colour comes as a regular image and a pressed image, both 3 by 3 grids of pieces. The button shows the
 * pressed image while the user holds the mouse down over it, and the regular image otherwise. It repeats the middle
 * column of pieces {@code hScale} times and the middle row {@code vScale} times, so the caller chooses how wide and
 * tall the button is.
 * <p>
 * The pressed image sits lower and slightly wider than the regular one on the canvas the two share. The button
 * draws each image at its place on that canvas, so the button sinks when pressed. It centres the label on the image
 * it is showing, so the label sinks with it.
 * <p>
 * The label is drawn in Pixelify Sans Bold at 24 points, a font bundled with the library. A caller replaces it on
 * one button with {@link #setFont(Font)}.
 *
 * <pre>{@code
 * var play = BigButton.blue("Play", 1, 0);    // preferred size 164 x 96
 * play.addActionListener(e -> startGame());
 * panel.add(play);
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
public class BigButton extends JButton {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final int GRID_SIZE = 3; // Every big button image is three rows of three pieces.

    // Loaded once and shared by every big button. A caller can still replace it on one button with setFont.
    private static final Font LABEL_FONT = Resources.readFont("/assets/fonts/pixelify-sans/PixelifySans-Bold.ttf")
                                                    .deriveFont(24f);

    /**
     * Creates a blue button. The smallest button, {@code blue(text, 0, 0)}, is 100 by 96 pixels and draws only the
     * four corners.
     *
     * @param text            the label drawn on the button
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new button
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static BigButton blue(String text, int horizontalScale, int verticalScale) {
        return new BigButton(Colour.BLUE, text, horizontalScale, verticalScale);
    }

    /**
     * Creates a red button. The smallest button, {@code red(text, 0, 0)}, is 100 by 96 pixels and draws only the
     * four corners.
     *
     * @param text            the label drawn on the button
     * @param horizontalScale the number of middle columns, 0 or more
     * @param verticalScale   the number of middle rows, 0 or more
     *
     * @return a new button
     *
     * @throws IllegalArgumentException if either scale is negative
     */
    public static BigButton red(String text, int horizontalScale, int verticalScale) {
        return new BigButton(Colour.RED, text, horizontalScale, verticalScale);
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    /**
     * The colours a big button comes in, each defined by the start of its two file names.
     */
    enum Colour {

        /** Drawn from {@code bigbluebutton_regular.png} and {@code bigbluebutton_pressed.png}. */
        BLUE("bigbluebutton"),

        /** Drawn from {@code bigredbutton_regular.png} and {@code bigredbutton_pressed.png}. */
        RED("bigredbutton");

        private static final String ROOT_DIR = "/assets/ui-elements/buttons/";

        private final String baseName;

        Colour(String baseName) {
            this.baseName = baseName;
        }

        /**
         * Returns the classpath path of the regular image, such as
         * {@code "/assets/ui-elements/buttons/bigbluebutton_regular.png"} for {@link #BLUE}.
         *
         * @return the absolute classpath path, starting with {@code /}
         */
        String getRegularPath() {
            return ROOT_DIR + baseName + "_regular.png";
        }

        /**
         * Returns the classpath path of the pressed image, such as
         * {@code "/assets/ui-elements/buttons/bigbluebutton_pressed.png"} for {@link #BLUE}.
         *
         * @return the absolute classpath path, starting with {@code /}
         */
        String getPressedPath() {
            return ROOT_DIR + baseName + "_pressed.png";
        }
    }

    /**
     * How far right and down the button draws one of its images, so that both images line up.
     */
    private record Offset(int x, int y) {}

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final SliceRenderer regular;
    private final SliceRenderer pressed;
    private final Offset        regularOffset;
    private final Offset        pressedOffset;
    private final int           hScale;
    private final int           vScale;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a button in the given colour.
     *
     * @param colour the colour of the button
     * @param text   the label drawn on the button
     * @param hScale the number of middle columns, 0 or more
     * @param vScale the number of middle rows, 0 or more
     *
     * @throws IllegalArgumentException if {@code hScale} or {@code vScale} is negative
     */
    BigButton(Colour colour, String text, int hScale, int vScale) {
        super(text);
        if (hScale < 0 || vScale < 0) {
            throw new IllegalArgumentException(
                    "hScale and vScale must be 0 or more, got %d and %d".formatted(hScale, vScale)
            );
        }

        this.regular = SliceRenderer.load(colour.getRegularPath(), GRID_SIZE, GRID_SIZE);
        this.pressed = SliceRenderer.load(colour.getPressedPath(), GRID_SIZE, GRID_SIZE);
        this.hScale  = hScale;
        this.vScale  = vScale;

        // Each image is shifted by how far its first piece sits from the leftmost and topmost of the two, which
        // puts both images back where they are on their shared canvas.
        var regularOrigin = regular.getOrigin();
        var pressedOrigin = pressed.getOrigin();
        var left          = Math.min(regularOrigin.x, pressedOrigin.x);
        var top           = Math.min(regularOrigin.y, pressedOrigin.y);
        this.regularOffset = new Offset(regularOrigin.x - left, regularOrigin.y - top);
        this.pressedOffset = new Offset(pressedOrigin.x - left, pressedOrigin.y - top);

        setFont(LABEL_FONT);

        // Switches off everything the look and feel draws apart from the label, which paintComponent replaces.
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Returns the size that fits both the regular and the pressed image at full size. Layout managers call this to
     * decide how much room the button gets. For {@code blue("Play", 1, 0)} that gives 164 by 96.
     *
     * @return the preferred size, worked out on every call
     */
    @Override
    public Dimension getPreferredSize() {
        var regularArea = findDrawnArea(regular, regularOffset);
        var pressedArea = findDrawnArea(pressed, pressedOffset);

        return new Dimension(
                Math.max(regularArea.x + regularArea.width, pressedArea.x + pressedArea.width),
                Math.max(regularArea.y + regularArea.height, pressedArea.y + pressedArea.height)
        );
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a minimum size from the label alone, which
     * would let a layout manager squeeze the button smaller than its images.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a maximum size from the label alone, which
     * would let a layout manager such as {@link BoxLayout} clip the button's images.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Draws the regular or the pressed image at its offset, then the label centred on that image.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        var showPressed = isShowingPressed();
        var renderer    = showPressed ? pressed : regular;
        var offset      = showPressed ? pressedOffset : regularOffset;
        var area        = findDrawnArea(renderer, offset);

        var image = g.create();
        try {
            image.translate(offset.x(), offset.y());
            renderer.paint(image, hScale, vScale);
        } finally {
            image.dispose();
        }

        // The look and feel centres the label on the whole button, so moving the graphics by the gap between the
        // two centres puts the label in the middle of the image instead.
        var label = g.create();
        try {
            label.translate(
                    area.x + (area.width / 2) - (getWidth() / 2),
                    area.y + (area.height / 2) - (getHeight() / 2)
            );
            super.paintComponent(label);
        } finally {
            label.dispose();
        }
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Returns true while the user holds the mouse down over the button, which is when Swing's own buttons draw
     * themselves pressed. Dragging off the button while holding the mouse down returns false.
     *
     * @return true while the button shows its pressed image
     */
    private boolean isShowingPressed() {
        var model = getModel();
        return model.isArmed() && model.isPressed();
    }

    /**
     * Returns the area of the button that an image covers when drawn at the given offset.
     *
     * @param renderer the regular or the pressed image
     * @param offset   where the button draws that image
     *
     * @return the position and size of the drawn image, in button coordinates
     */
    private Rectangle findDrawnArea(SliceRenderer renderer, Offset offset) {
        var size = renderer.measure(hScale, vScale);
        return new Rectangle(offset.x(), offset.y(), size.width, size.height);
    }

}
