package com.slinky.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A {@link JButton} that draws itself from one of the small round or square button images, at the image's own
 * size.
 * <p>
 * Each button comes as a regular image and a pressed image on a canvas of the same size. The button shows the
 * pressed image while the user holds the mouse down over it, and the regular image otherwise. It centres its
 * icon or label on the visible part of the image it is showing, so the icon sinks with the button when
 * pressed.
 *
 * <pre>{@code
 * var close = SmallButton.redSquare();    // preferred size 128 x 128
 * close.setIcon(new ImageIcon(cross));
 * close.addActionListener(e -> dispose());
 * }</pre>
 *
 * @author Kheagen Haskins
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
public class SmallButton extends JButton {

    // ========================================================================================== \\
    //                                           Static                                           \\
    // ========================================================================================== \\
    private static final String ROOT_DIR = "/assets/ui-elements/buttons/";

    /**
     * Creates a blue round button.
     *
     * @return a new button, with no label or icon
     */
    public static SmallButton blueRound() {
        return new SmallButton(Colour.BLUE, Type.ROUND);
    }

    /**
     * Creates a blue square button.
     *
     * @return a new button, with no label or icon
     */
    public static SmallButton blueSquare() {
        return new SmallButton(Colour.BLUE, Type.SQUARE);
    }

    /**
     * Creates a red round button.
     *
     * @return a new button, with no label or icon
     */
    public static SmallButton redRound() {
        return new SmallButton(Colour.RED, Type.ROUND);
    }

    /**
     * Creates a red square button.
     *
     * @return a new button, with no label or icon
     */
    public static SmallButton redSquare() {
        return new SmallButton(Colour.RED, Type.SQUARE);
    }

    // ========================================================================================== \\
    //                                           Nested                                           \\
    // ========================================================================================== \\
    /**
     * The shapes a small button comes in, each defined by the part of its file names that follows the colour.
     */
    enum Type {

        /** Drawn from the {@code small*squarebutton} images. */
        SQUARE("square"),

        /** Drawn from the {@code small*roundbutton} images. */
        ROUND("round");

        private final String fileName;

        Type(String fileName) {
            this.fileName = fileName;
        }
    }

    /**
     * The colours a small button comes in, each defined by the part of its file names that follows "small".
     */
    enum Colour {

        /** Drawn from the {@code smallblue*button} images. */
        BLUE("blue"),

        /** Drawn from the {@code smallred*button} images. */
        RED("red");

        private final String fileName;

        Colour(String fileName) {
            this.fileName = fileName;
        }
    }

    // ========================================================================================== \\
    //                                           Fields                                           \\
    // ========================================================================================== \\
    private final BufferedImage regularImage;
    private final BufferedImage pressedImage;
    private final Rectangle     regularFace;
    private final Rectangle     pressedFace;

    // ========================================================================================== \\
    //                                       Constructor(s)                                       \\
    // ========================================================================================== \\
    /**
     * Creates a button in the given colour and shape, with no label or icon.
     *
     * @param colour the colour of the button
     * @param type   the shape of the button
     */
    SmallButton(Colour colour, Type type) {
        this.regularImage = Resources.readImage(buildPath(colour, type, "regular"));
        this.pressedImage = Resources.readImage(buildPath(colour, type, "pressed"));

        // A 1 by 1 scan returns the bounds of every visible pixel, which is the button face the icon centres
        // on.
        this.regularFace = ImageSlicer.scan(regularImage, 1, 1).getRegion(0, 0);
        this.pressedFace = ImageSlicer.scan(pressedImage, 1, 1).getRegion(0, 0);

        // Switches off everything the look and feel draws apart from the icon and label, which paintComponent
        // replaces.
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    // ========================================================================================== \\
    //                                        API Methods                                         \\
    // ========================================================================================== \\
    /**
     * Returns the size of the button images, 128 by 128 for every current colour and shape. Layout managers
     * call this to decide how much room the button gets.
     *
     * @return the preferred size, large enough for both the regular and the pressed image
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                Math.max(regularImage.getWidth(), pressedImage.getWidth()),
                Math.max(regularImage.getHeight(), pressedImage.getHeight())
        );
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a minimum size from the icon and label
     * alone, which would let a layout manager squeeze the button smaller than its image.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns {@link #getPreferredSize()}. The look and feel works out a maximum size from the icon and label
     * alone, which would let a layout manager such as {@link BoxLayout} clip the button's image.
     *
     * @return the preferred size
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Draws the regular or the pressed image whole from the top-left corner, then the icon and label centred on
     * the visible part of that image.
     *
     * @param g the graphics context that Swing passes in for this paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        var showPressed = isShowingPressed();
        var face        = showPressed ? pressedFace : regularFace;

        g.drawImage(showPressed ? pressedImage : regularImage, 0, 0, null);

        // The look and feel centres the icon on the whole button, so moving the graphics by the gap between
        // the two centres puts the icon in the middle of the visible face instead.
        var content = g.create();
        try {
            content.translate(
                    face.x + (face.width / 2) - (getWidth() / 2),
                    face.y + (face.height / 2) - (getHeight() / 2)
            );
            super.paintComponent(content);
        } finally {
            content.dispose();
        }
    }

    // ========================================================================================== \\
    //                                       Helper Methods                                       \\
    // ========================================================================================== \\
    /**
     * Returns the classpath path of one image, such as
     * {@code "/assets/ui-elements/buttons/smallredsquarebutton_pressed.png"}.
     *
     * @param colour the colour of the button
     * @param type   the shape of the button
     * @param state  either {@code "regular"} or {@code "pressed"}
     *
     * @return the absolute classpath path, starting with {@code /}
     */
    private static String buildPath(Colour colour, Type type, String state) {
        return "%ssmall%s%sbutton_%s.png".formatted(ROOT_DIR, colour.fileName, type.fileName, state);
    }

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

}
