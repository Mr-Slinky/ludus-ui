package com.slinky.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link Ribbon}, pinning the size of every style in every colour and how the width grows.
 * <p>
 * Every expected size comes from the ribbon and sword images: the widths of the left and right pieces, plus 64
 * pixels for each middle piece, by the height of the one row of pieces.
 *
 * <p>
 * <b>TDD state.</b> Written before {@code Ribbon}. Covered: the preferred size of all four styles in all five
 * colours; the extra width from {@link Ribbon#increaseWidth()}; a negative scale; and a null colour.
 *
 * @author Claude Code
 * @version 1.0.0
 *         <p>
 *         Last modified: 2026-09-11
 * @since 1.0.0
 */
class RibbonTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            # style,        width at scale 0, height
            SWORD,          197,              128
            BIG,            195,              103
            SMALL_FORKED,   124,               60
            SMALL_POINTED,  122,               54
            """)
    void testGetPreferredSize_withValidArgs_ReturnsSizeForEveryColour(Ribbon.Style style, int widthAtZero,
            int height) {
        var colours = Ribbon.Colour.values();

        var actual = Arrays.stream(colours).map(colour -> new Ribbon(style, colour, 2).getPreferredSize()).toList();

        assertEquals(Collections.nCopies(colours.length, new Dimension(widthAtZero + 128, height)), actual);
    }

    @Test
    void testIncreaseWidth_withValidArgs_AddsOneMiddlePiece() {
        var ribbon = Ribbon.big(Ribbon.Colour.RED, 0);

        ribbon.increaseWidth();

        assertEquals(new Dimension(195 + 64, 103), ribbon.getPreferredSize());
    }

    @Test
    void testSword_withInvalidArgs_ThrowsIllegalArgumentException() {
        var ex = assertThrows(IllegalArgumentException.class, () -> Ribbon.sword(Ribbon.Colour.BLUE, -1));

        assertEquals("hScale must be 0 or more, got -1", ex.getMessage());
    }

    @Test
    void testBig_withNullArgs_ThrowsNullPointerException() {
        var ex = assertThrows(NullPointerException.class, () -> Ribbon.big(null, 0));

        assertEquals("colour must not be null", ex.getMessage());
    }

}
