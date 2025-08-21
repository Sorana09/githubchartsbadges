package com.example.service;

import com.example.domain.ChartInformation;
import com.example.domain.Theme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public  class GenerateChartService {

    private static boolean isDark(ChartInformation info) {
        Theme theme = info.getTheme();
        return theme != null && theme == Theme.DARK;
    }

    public byte[] generateChart(ChartInformation info) {
       try {
           BufferedImage chartImage = this.buildChart(info).createBufferedImage(800, 400);
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ImageIO.write(chartImage, "png", baos);
           return baos.toByteArray();
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
   }

    public JFreeChart buildChart(ChartInformation info) {
        JFreeChart chart;

        switch (info.getChartType()) {
            case BAR:
                chart = ChartFactory.createBarChart(
                        info.getTitle(),
                        info.getCategoryAxisLabel(),
                        info.getValueAxisLabel(),
                        info.getCategoryDataset(),
                        PlotOrientation.VERTICAL,
                        false,
                        false,
                        false
                );

                CategoryPlot barPlot = chart.getCategoryPlot();
                if (isDark(info)) {
                    barPlot.setBackgroundPaint(new Color(0x1e1e1e));
                } else {
                    barPlot.setBackgroundPaint(Color.WHITE);
                }
                barPlot.setDomainGridlinesVisible(false);
                barPlot.setOutlineVisible(false);

                BarRenderer barRenderer = (BarRenderer) barPlot.getRenderer();
                barRenderer.setShadowVisible(false);
                barRenderer.setBarPainter(new StandardBarPainter());
                barRenderer.setGradientPaintTransformer(null);
                barRenderer.setDrawBarOutline(false);
                barRenderer.setMaximumBarWidth(0.1);
                barRenderer.setItemMargin(0.02);

                Color[] series = isDark(info)
                        ? new Color[]{new Color(0x7aa2f7), new Color(0xf7768e), new Color(0x9ece6a), new Color(0xe0af68)}
                        : new Color[]{new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c), new Color(0xff7f0e)};
                for (int s = 0; s < 8; s++) {
                    barRenderer.setSeriesPaint(s, series[s % series.length]);
                }
                break;

            case PIE:
                chart = ChartFactory.createPieChart(
                        info.getTitle(),
                        info.getPieDataset(),
                        false,
                        true,
                        false
                );

                PiePlot piePlot = (PiePlot) chart.getPlot();
                if (isDark(info)) {
                    piePlot.setBackgroundPaint(new Color(0x1e1e1e));
                    piePlot.setLabelPaint(new Color(0xdddddd));
                } else {
                    piePlot.setBackgroundPaint(Color.WHITE);
                    piePlot.setLabelPaint(Color.DARK_GRAY);
                }
                piePlot.setOutlineVisible(false);
                piePlot.setShadowPaint(null);
                piePlot.setLabelBackgroundPaint(null);
                piePlot.setLabelOutlinePaint(null);
                piePlot.setLabelShadowPaint(null);

                if (info.getPieDataset() != null && info.getPieDataset().getItemCount() > 0) {
                    Color[] colors = isDark(info)
                            ? new Color[]{new Color(0x7aa2f7), new Color(0xf7768e), new Color(0x9ece6a), new Color(0xe0af68), new Color(0xbb9af7)}
                            : new Color[]{new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c), new Color(0xff7f0e), new Color(0x9467bd)};
                    for (int i = 0; i < info.getPieDataset().getItemCount(); i++) {
                        piePlot.setSectionPaint(info.getPieDataset().getKey(i), colors[i % colors.length]);
                    }
                }
                break;

            case LINE:
                chart = ChartFactory.createLineChart(
                        info.getTitle(),
                        info.getCategoryAxisLabel(),
                        info.getValueAxisLabel(),
                        info.getCategoryDataset(),
                        PlotOrientation.VERTICAL,
                        false,
                        true,
                        false
                );

                CategoryPlot linePlot = chart.getCategoryPlot();
                if (isDark(info)) {
                    linePlot.setBackgroundPaint(new Color(0x1e1e1e));
                    linePlot.setRangeGridlinePaint(new Color(0x3a3a3a));
                    linePlot.setDomainGridlinePaint(new Color(0x3a3a3a));
                } else {
                    linePlot.setBackgroundPaint(Color.WHITE);
                    linePlot.setRangeGridlinePaint(new Color(200, 200, 200));
                    linePlot.setDomainGridlinePaint(new Color(200, 200, 200));
                }
                linePlot.setOutlineVisible(false);
                // Ensure no plot-level drop shadow is applied (JFreeChart 1.5+)
                try { linePlot.setShadowGenerator(null); } catch (NoSuchMethodError ignored) { }

                LineAndShapeRenderer lineRenderer = (LineAndShapeRenderer) linePlot.getRenderer();
                lineRenderer.setDrawOutlines(true);
                lineRenderer.setSeriesStroke(0, new BasicStroke(2f));
                lineRenderer.setDefaultShapesFilled(true);
                lineRenderer.setSeriesPaint(0, isDark(info) ? new Color(0x7aa2f7) : new Color(0x1f77b4));
                for (int s = 1; s < 8; s++) {
                    lineRenderer.setSeriesPaint(s, isDark(info) ? new Color(0x9ece6a) : new Color(0x2ca02c));
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported chart type: " + info.getChartType());
        }

        return chart;
    }


}