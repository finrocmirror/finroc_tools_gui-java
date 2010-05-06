/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2007-2010 Max Reichardt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.finroc.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.finroc.gui.GUIPanel;
import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedConvexSVG;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.commons.fastdraw.SVG;
import org.finroc.gui.themes.Themes;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;

/**
 * @author max
 *
 */
public class LCD extends Widget {

    /** UID */
    private static final long serialVersionUID = -8860194633749745370L;

    /** input */
    public WidgetInput.Numeric input;

    /** colors */
    private Color lcdBackground = Themes.getCurTheme().lcdBackground();
    private Color lcdEnabled = Themes.getCurTheme().lcdEnabled();
    private Color lcdDisabled = Themes.getCurTheme().lcdDisabled();

    /** number format */
    private enum FormatOptions { Default, FractionDigits, ToString, JavaDecimalFormat, CxxDecimalFormat };
    private FormatOptions formatting = FormatOptions.Default;
    private String format = "2";
    private boolean useScientificFormatOutOfRange = true;
    private static final Pattern CXX_PATTERN = Pattern.compile("%([0-9]+).([0-9]+)f");

    /** warnings */
    private Color lcdWarningBackground;
    private Color lcdWarningEnabled;
    private Color lcdWarningDisabled;
    private enum WarnOptions { Off, LargerThan, SmallerThan };
    private WarnOptions warnings = WarnOptions.Off;
    private double warningThreshold = 0;

    //private

    /** for block rendering */
    private static final String[] allBlocks = { "NW", "N", "NE", "C", "SW", "S", "SE" };
    private static Map<String, List<String>> blocksForDigits;

    @Override
    protected WidgetUI createWidgetUI() {
        return new LCDUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    @Override
    public void restore(GUIPanel parent) {
        super.restore(parent);
        if (lcdWarningBackground == null) {
            lcdWarningBackground = createWarningColor(lcdBackground);
            lcdWarningEnabled = createWarningColor(lcdEnabled);
            lcdWarningDisabled = createWarningColor(lcdDisabled);
        }
        if (formatting == null) {
            formatting = FormatOptions.Default;
        }
    }

    private Color createWarningColor(Color c) {
        int all = Math.max(Math.max(c.getRed(), c.getBlue()), c.getGreen());
        return new Color(all, 0, 0);
    }

    private class LCDUI extends WidgetUI implements CCPortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -5577318403814215832L;

        private static final double SPACING = 0.25;
        private static final int SVGHEIGHT = 540;
        private static final int SVGWIDTH = 300;

        //private transient SVGPanel svg;
        private BufferedConvexSVG svgComma;
        private Map<String, BufferedConvexSVG> blocksOn = new HashMap<String, BufferedConvexSVG>(),
        blocksOff = new HashMap<String, BufferedConvexSVG>();
        //private Map<String, SVGPanel> digits;
        //private Number oldNumber;

        private DecimalFormat primaryFormat = new DecimalFormat("######0.00");
        private DecimalFormat fallbackFormat = new DecimalFormat("0.000E0");

        private LCDUI() {
            super(RenderMode.Cached);
            if (blocksForDigits == null) {
                blocksForDigits = new HashMap<String, List<String>>();
                blocksForDigits.put("0", Arrays.asList(new String[] {"NW", "N", "NE", "SW", "S", "SE"}));
                blocksForDigits.put("1", Arrays.asList(new String[] {"NE", "SE"}));
                blocksForDigits.put("2", Arrays.asList(new String[] {"N", "NE", "C", "SW", "S"}));
                blocksForDigits.put("3", Arrays.asList(new String[] {"N", "NE", "C", "S", "SE"}));
                blocksForDigits.put("4", Arrays.asList(new String[] {"NW", "C", "NE", "SE"}));
                blocksForDigits.put("5", Arrays.asList(new String[] {"NW", "N", "C", "S", "SE"}));
                blocksForDigits.put("6", Arrays.asList(new String[] {"NW", "N", "C", "SW", "S", "SE"}));
                blocksForDigits.put("7", Arrays.asList(new String[] {"N", "NE", "SE"}));
                blocksForDigits.put("8", Arrays.asList(new String[] {"NW", "N", "NE", "C", "SW", "S", "SE"}));
                blocksForDigits.put("9", Arrays.asList(new String[] {"NW", "N", "NE", "C", "S", "SE"}));
                blocksForDigits.put("-", Arrays.asList(new String[] {"C"}));
                blocksForDigits.put("E", Arrays.asList(new String[] {"NW", "N", "SW", "S", "C"}));
                blocksForDigits.put(" ", Arrays.asList(new String[] {}));
                blocksForDigits.put("N", Arrays.asList(new String[] {"SW", "C", "SE"}));
                blocksForDigits.put("n", Arrays.asList(new String[] {"SW", "C", "SE"}));
                blocksForDigits.put("A", Arrays.asList(new String[] {"NW", "N", "NE", "C", "SW", "SE"}));
                blocksForDigits.put("a", Arrays.asList(new String[] {"NW", "N", "NE", "C", "SW", "SE"}));
                blocksForDigits.put("I", Arrays.asList(new String[] {"NE", "SE"}));
                blocksForDigits.put("i", Arrays.asList(new String[] {"NE", "SE"}));
                blocksForDigits.put("F", Arrays.asList(new String[] {"SW", "NW", "C", "N"}));
                blocksForDigits.put("f", Arrays.asList(new String[] {"SW", "NW", "C", "N"}));
            }

            input.addChangeListener(this);
            try {
                SVG tempCommaOn = SVG.createInstance(LCD.class.getResource("LCDComma.svg"), true);
                SVG tempDigitOn = SVG.createInstance(LCD.class.getResource("LCD.svg"), true);
                SVG tempDigitOff = SVG.createInstance(LCD.class.getResource("LCD.svg"), true);
                svgComma = new BufferedConvexSVG(tempCommaOn, "comma", lcdBackground);
                for (String s : allBlocks) {
                    blocksOn.put(s, new BufferedConvexSVG(tempDigitOn, s, lcdBackground));
                    blocksOff.put(s, new BufferedConvexSVG(tempDigitOff, s, lcdBackground));
                }
                updateColors(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {

            primaryFormat.setMaximumIntegerDigits(50);
            primaryFormat.setMinimumIntegerDigits(1);
            fallbackFormat.setMinimumIntegerDigits(1);

            // update number formatting
            switch (formatting) {
            case Default:
                primaryFormat.applyPattern("######0.00");
                break;

            case FractionDigits:
                int digits = 2;
                try {
                    digits = Integer.parseInt(format);
                } catch (Exception e) {
                    format = "2";
                }
                primaryFormat.setMaximumFractionDigits(digits);
                primaryFormat.setMinimumFractionDigits(digits);
                break;

            case CxxDecimalFormat:
                Matcher m = CXX_PATTERN.matcher(format);
                if (!m.matches()) {
                    format = "%6.2f";
                    widgetPropertiesChanged();
                } else {
                    try {
                        primaryFormat.setMaximumIntegerDigits(Integer.parseInt(m.group(1)));
                        int n = Integer.parseInt(m.group(2));
                        primaryFormat.setMaximumFractionDigits(n);
                        primaryFormat.setMinimumFractionDigits(n);
                    } catch (Exception e) {
                        format = "%6.2f";
                        widgetPropertiesChanged();
                    }
                }
                break;

            case JavaDecimalFormat:
                try {
                    primaryFormat.applyPattern(format);
                } catch (Exception e) {
                    format = "#######0.00";
                    widgetPropertiesChanged();
                }
                break;
            }

        }

        private void updateColors(boolean warn) throws Exception {
            Color background = warn ? lcdWarningBackground : lcdBackground;
            Color enabled = warn ? lcdWarningEnabled : lcdEnabled;
            Color disabled = warn ? lcdWarningDisabled : lcdDisabled;

            svgComma.setBackground(background);
            svgComma.setColor(enabled);
            for (BufferedConvexSVG svg : blocksOn.values()) {
                svg.setBackground(background);
                svg.setColor(enabled);
            }
            for (BufferedConvexSVG svg : blocksOff.values()) {
                svg.setBackground(background);
                svg.setColor(disabled);
            }
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
            //if (oldNumber != null) {
            //System.out.println(oldNumber.toString() + " " + value.toString() + " " + value.equals(oldNumber));
            //}
            //oldNumber = new Number(value.)
            setChanged();
            repaint();
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) {

            if (renderSize.height < 2 || renderSize.width < 2) {
                cache.fill(0);
                return;
            }

            // Get value
            CoreNumber cn = input.getAutoLocked();
            releaseAllLocks();

            // Optimal size
            int blockWidth = (SVGWIDTH * renderSize.height / SVGHEIGHT);
            int optimalLength = (int)((renderSize.width + blockWidth * SPACING) / (blockWidth * (1 + SPACING)));

            // Format number
            String s = formatNumber(cn, optimalLength);

            // Determine pos of comma
            int commaPos = s.indexOf(".");
            s = s.replace(".", "");
            if (commaPos == -1) {
                commaPos = s.length();
            }

            // calculate block size
            if (s.length() > optimalLength) {
                blockWidth = (int)(renderSize.width / ((s.length() - 1) * (1 + SPACING) + 1));
            }
            int blockHeight = SVGHEIGHT * blockWidth / SVGWIDTH;

            // prepend whitespace
            int fillSpace = optimalLength - s.length();
            if (fillSpace > 0) {
                StringBuilder sb = new StringBuilder(fillSpace);
                for (int i = 0; i < fillSpace; i++) {
                    sb.append(" ");
                }
                s = sb.toString() + s;
                commaPos += fillSpace;
            }

            boolean warn = ((warnings == WarnOptions.LargerThan && cn.doubleValue() > warningThreshold) || (warnings == WarnOptions.SmallerThan && cn.doubleValue() < warningThreshold));
            Color background = warn ? lcdWarningBackground : lcdBackground;
            cache.drawFilledRectangle(new Rectangle(renderSize), background.getRGB());
            try {
                updateColors(warn);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Rectangle firstBlock = new Rectangle(renderSize.width - blockWidth, (renderSize.height - blockHeight) / 2, blockWidth, blockHeight);
            for (int i = s.length() - 1; i >= 0; i--) {
                renderBlock(cache, firstBlock, s.substring(i, i + 1));
                firstBlock.x -= firstBlock.width * (1 + SPACING);
                if (i == commaPos) {
                    firstBlock.x += firstBlock.width * (1 + SPACING) / 2 + 1;
                    renderComma(cache, firstBlock);
                    firstBlock.x -= firstBlock.width * (1 + SPACING) / 2 + 1;
                }
            }
            //cache.save(new File("cache.png"));
        }

        private String formatNumber(CoreNumber num, int optimalLength) {

            //super.paintComponent(g);
            //input.setValue(-123567890);

            // catch nan and infinity
            if (num.isFloatingPoint()) {
                double d = num.doubleValue();
                if (Double.isNaN(d)) {
                    return "nan";
                } else if (Double.isInfinite(d)) {
                    return "inf";
                }
            }

            switch (formatting) {
            case Default:
                if (!num.isFloatingPoint()) {
                    return num.toString();
                }
                break;

            case ToString:
                if (!num.isFloatingPoint()) {
                    return num.toString();
                } else {
                    String s = num.toString();
                    if (s.length() <= optimalLength + 1) {
                        return s;
                    }

                    // original code
                    String suffix = "";
                    if (s.contains("E")) {
                        suffix = s.substring(s.indexOf("E"));
                        s = s.substring(0, s.indexOf("E"));
                    }

                    // Determine pos of comma
                    int commaPos = s.indexOf(".");
                    s = s.replace(".", "");
                    if (commaPos == -1) {
                        commaPos = s.length();
                    }

                    if (s.length() + suffix.length() > optimalLength) { // notfalls Kommastellen weglassen
                        s = s.substring(0, Math.max(commaPos, optimalLength - suffix.length()));
                    }
                    // Nullen hinterm Komma abschneiden
                    while (s.length() - commaPos > 1 && s.substring(s.length() - 1).equals("0")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    s += suffix;

                    return s;
                }
            }

            return primaryFormat(num.doubleValue(), optimalLength);
        }

        private String primaryFormat(double val, int optimalLength) {
            String x = primaryFormat.format(val).replace(',', '.');
            int c = x.contains(".") ? 1 : 0;
            if (useScientificFormatOutOfRange) {
                if (x.length() > optimalLength + c && optimalLength >= 5) {
                    x = fallbackFormat.format(val).replace(',', '.');
                    if (x.length() != optimalLength + 1) {
                        int diff = (optimalLength + 1) - x.length();
                        int frac = Math.max(1, fallbackFormat.getMinimumFractionDigits() + diff);
                        fallbackFormat.setMinimumFractionDigits(frac);
                        fallbackFormat.setMaximumFractionDigits(frac);
                        x = fallbackFormat.format(val).replace(',', '.');
                    }
                }
            }
            return x;
        }

        public void renderBlock(BufferedImageRGB cache, Rectangle pos, String digit) {

            List<String> blocks = blocksForDigits.get(digit);
            if (blocks == null) {
                System.out.println("warning in LCD Widget: Cannot render digit " + digit);
            }
            for (String s : allBlocks) {
                if (blocks.contains(s)) {
                    blocksOn.get(s).blitTo(cache, pos);
                } else {
                    blocksOff.get(s).blitTo(cache, pos);
                }
            }
        }

        public void renderComma(BufferedImageRGB cache, Rectangle pos) {

            svgComma.blitTo(cache, pos);
        }
    }

}
