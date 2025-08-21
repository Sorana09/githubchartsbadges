package com.example.githubmonitoring.builder;

public class SVGBadge {

    public static String buildBadge(String label, String value) {
        return buildBadge(label, value, "#4f46e5", "light");
    }

    public static String buildBadge(String label, String value, String color, String theme) {
        String safeLabel = escape(label);
        String safeValue = escape(value);
        boolean dark = "dark".equalsIgnoreCase(theme);

        String bg = dark ? "#0f172a" : "#f3f4f6"; // slate-900 vs gray-100
        String text = dark ? "#e5e7eb" : "#111827"; // gray-200 vs gray-900

        int paddingX = 8;
        int height = 24;
        int labelWidth = 6 * safeLabel.length() + 2 * paddingX; // approx 6px per char
        int valueWidth = 6 * safeValue.length() + 2 * paddingX;
        int width = labelWidth + valueWidth;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
           .append(width)
           .append("\" height=\"")
           .append(height)
           .append("\" role=\"img\" aria-label=\"")
           .append(safeLabel).append(": ").append(safeValue)
           .append("\">\n");

        // background rects
        svg.append("<rect rx=\"4\" width=\"").append(labelWidth)
           .append("\" height=\"").append(height)
           .append("\" fill=\"").append(bg).append("\"/>\n");
        svg.append("<rect rx=\"4\" x=\"").append(labelWidth)
           .append("\" width=\"").append(valueWidth)
           .append("\" height=\"").append(height)
           .append("\" fill=\"").append(color).append("\"/>\n");

        // texts
        int textY = 16;
        svg.append(textEl(paddingX, textY, safeLabel, text));
        svg.append(textEl(labelWidth + paddingX, textY, safeValue, "#ffffff"));

        svg.append("</svg>");
        return svg.toString();
    }

    private static String textEl(int x, int y, String content, String fill) {
        return "<text x=\"" + x + "\" y=\"" + y + "\" fill=\"" + fill + "\" font-family=\"Verdana,DejaVu Sans,sans-serif\" font-size=\"12\">" + content + "</text>\n";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
