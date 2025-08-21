package com.example.githubmonitoring.builder;

public class SVGBadge {

    public static String buildBadge(String label, String value, String color, String theme) {
        return builder().label(label).value(value).color(color).theme(theme).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String textEl(int x, int y, String content, String fill) {
        return "<text x=\"" + x + "\" y=\"" + y + "\" fill=\"" + fill + "\" font-family=\"Verdana,DejaVu Sans,sans-serif\" font-size=\"12\">" + content + "</text>\n";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static final class Builder {
        private String label;
        private String value;
        private String color = "#4f46e5";
        private String theme = "light";
        private Integer height;
        private Integer width;
        private Integer paddingX;
        private String title;

        private static int approxTextWidth(String s) {
            if (s == null || s.isEmpty()) return 0;
            int base = s.length() * 6;
            int bonus = (int) s.chars().filter(ch -> Character.isUpperCase(ch) || Character.isDigit(ch)).count();
            return base + bonus;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder paddingX(int paddingX) {
            this.paddingX = paddingX;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public String build() {
            String safeLabel = escape(label);
            String safeValue = escape(value);
            boolean dark = "dark".equalsIgnoreCase(theme);

            String bg = dark ? "#0f172a" : "#f3f4f6";
            String text = dark ? "#e5e7eb" : "#111827";

            int px = paddingX != null ? paddingX : 8;
            int h = height != null ? Math.max(12, height) : 24;

            int labelWidth = approxTextWidth(safeLabel) + 2 * px;
            int valueWidth = approxTextWidth(safeValue) + 2 * px;
            int autoWidth = labelWidth + valueWidth;
            int w = width != null ? Math.max(labelWidth + 10, width) : autoWidth;

            int labelSegment = labelWidth;
            int valueSegment = Math.max(10, w - labelSegment);

            String ttl = (title != null && !title.isBlank()) ? title : (safeLabel + ": " + safeValue);

            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                    .append(w)
                    .append("\" height=\"")
                    .append(h)
                    .append("\" role=\"img\" aria-label=\"")
                    .append(ttl)
                    .append("\">\n");

            svg.append("<title>").append(ttl).append("</title>\n");

            svg.append("<rect rx=\"4\" width=\"").append(labelSegment)
                    .append("\" height=\"").append(h)
                    .append("\" fill=\"").append(bg).append("\"/>\n");
            svg.append("<rect rx=\"4\" x=\"").append(labelSegment)
                    .append("\" width=\"").append(valueSegment)
                    .append("\" height=\"").append(h)
                    .append("\" fill=\"").append(color).append("\"/>\n");

            int textY = Math.max(1, h - 8);
            svg.append(textEl(px, textY, safeLabel, text));
            svg.append(textEl(labelSegment + px, textY, safeValue, "#ffffff"));

            svg.append("</svg>");
            return svg.toString();
        }
    }
}
