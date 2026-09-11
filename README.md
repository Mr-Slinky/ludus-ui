# Ludus UI

Ludus UI is a Java library of Swing components that draw themselves from pixel-art images. A caller
adds one to a container like any other `JPanel` or `JButton`, and the component paints the art
itself in place of the look and feel's drawing.

## Components

The library exports four classes from `com.slinky.ludus.ui`. A caller creates each one through a static
factory method.

| Class         | Extends   | Factory methods                                    | What it draws                                          |
|---------------|-----------|----------------------------------------------------|--------------------------------------------------------|
| `Surface`     | `JPanel`  | `wood`, `regularPaper`, `specialPaper`, `banner`   | A wooden table, a sheet of paper or a parchment banner |
| `Ribbon`      | `JPanel`  | `sword`, `big`, `smallForked`, `smallPointed`      | A sword or a ribbon, in a colour from `Ribbon.Colour`  |
| `BigButton`   | `JButton` | `blue`, `red`                                      | A button with a text label                             |
| `SmallButton` | `JButton` | `blueRound`, `blueSquare`, `redRound`, `redSquare` | A button with an icon from `SmallButton.Symbol`        |

Both button classes show a pressed image while the user holds the mouse down over them, so the
button sinks as it is clicked. `BigButton` draws its label in Pixelify Sans Bold at 24 points, and a
caller replaces the font on one button with `setFont(Font)`.

The `SmallButton` factory methods also take an angle in radians, and the button turns its icon
clockwise by that angle. The angle must be a whole number of quarter turns, such as `Math.PI / 2`,
so that the pixel art stays sharp.

## Stretching

`Surface`, `Ribbon` and `BigButton` grow by repeating the middle of their image. Each image contains
its pieces in a grid, with a fully transparent gap between neighbouring rows and columns. A table,
paper, banner or big button image has three rows of three pieces: four corners, four edges and a
centre. A ribbon or sword image has one row of three pieces: a left end, a middle and a right end.

The component draws the corners and ends once. Between them it repeats the middle column a number of
times the caller chooses, which the factory method takes as `horizontalScale`. The `Surface` and
`BigButton` factories also take a `verticalScale` for the middle row. A scale of 0 draws the corners
and ends alone.

The calls below create one component of each kind.

```java
var table = Surface.wood(3, 1);                                  // 3 middle columns, 1 middle row
var title = Ribbon.big(Ribbon.Colour.RED, 2);                    // 2 middle pieces
var play  = BigButton.blue("Play", 1, 0);                        // 1 middle column, no middle row
var close = SmallButton.redSquare(SmallButton.Symbol.CROSS, 0);  // always the image's own size
```

A `Surface` or a `Ribbon` also grows after creation. The caller calls `increaseWidth()`, or
`increaseHeight()` on a `Surface`, then calls `pack()` on the window to make room for the new size.

Every component returns the size of its art from `getPreferredSize()`, and the layout manager uses
that to decide how much room to give it. The Javadoc of each factory method gives the smallest size
in pixels.

## Using the library

A developer installs the jar into the local Maven repository, where any other project on the same
machine can find it.

```bash
mvn clean install
```

The consuming project then declares the dependency in its `pom.xml`.

```xml
<dependency>
    <groupId>com.slinky.ludus</groupId>
    <artifactId>ludus-ui</artifactId>
    <version>1.0.0</version>
</dependency>
```

The code below opens a window containing a wooden table, a big button and a small button.

```java
SwingUtilities.invokeLater(() -> {
    var frame = new JFrame("Ludus UI");
    frame.setLayout(new FlowLayout());

    frame.add(Surface.wood(3, 1));
    frame.add(BigButton.blue("Play", 1, 0));
    frame.add(SmallButton.redSquare(SmallButton.Symbol.CROSS, 0));

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
});
```

## Environment

The project compiles for Java 25 and builds with Maven. The library depends on Swing alone, which
ships inside the JDK, and the test suite runs on JUnit 6.1.3.

| Command                         | Purpose                         |
|---------------------------------|---------------------------------|
| `mvn clean install -DskipTests` | Rebuild after a change          |
| `mvn clean install`             | Full build, with the test suite |
| `mvn test`                      | The test suite alone            |

## The art

`src/main/resources/assets` contains the images and the font, and the build packs both into the jar.
`ui-elements/` stores the images the components draw, along with character avatars, cursors and
bar images.

The images come from [Tiny Swords](https://pixelfrog-assets.itch.io/tiny-swords) by Pixel Frog.
Pixel Frog's terms allow the pack in personal and commercial projects and allow the images to be
modified. The same terms forbid redistributing, reselling or repackaging the images, even once
modified.

`fonts/pixelify-sans/` stores Pixelify Sans in four weights. The font is copyright 2021 The
Pixelify Sans Project Authors and is licensed under the SIL Open Font License 1.1. `OFL.txt`, in the
same folder as the font files, contains the full licence.
