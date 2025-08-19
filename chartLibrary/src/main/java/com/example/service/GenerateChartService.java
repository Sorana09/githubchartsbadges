package com.example.service;

import com.example.domain.ChartInformation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import java.awt.*;

import static com.example.domain.ChartType.*;

public  class GenerateChartService {

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
                        true,
                        false
                );

                CategoryPlot barPlot = chart.getCategoryPlot();
                barPlot.setBackgroundPaint(Color.WHITE);
                barPlot.setDomainGridlinesVisible(true);
                barPlot.setRangeGridlinePaint(new Color(200, 200, 200));
                barPlot.setOutlineVisible(false);

                BarRenderer barRenderer = (BarRenderer) barPlot.getRenderer();
                barRenderer.setSeriesPaint(0, Color.BLACK);
                barRenderer.setShadowVisible(false);
                barRenderer.setMaximumBarWidth(0.1);
                barRenderer.setItemMargin(0.02);
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
                piePlot.setBackgroundPaint(Color.WHITE);
                piePlot.setOutlineVisible(false);
                piePlot.setShadowPaint(null);
                piePlot.setLabelBackgroundPaint(null);
                piePlot.setLabelOutlinePaint(null);
                piePlot.setLabelShadowPaint(null);
                piePlot.setLabelPaint(Color.DARK_GRAY);

                if (info.getPieDataset() != null && info.getPieDataset().getItemCount() > 0) {
                    piePlot.setSectionPaint(info.getPieDataset().getKey(0), Color.BLACK);
                    for (int i = 1; i < info.getPieDataset().getItemCount(); i++) {
                        piePlot.setSectionPaint(info.getPieDataset().getKey(i),
                                new Color(50 + i * 30, 50 + i * 30, 50 + i * 30));
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
                linePlot.setBackgroundPaint(Color.WHITE);
                linePlot.setRangeGridlinePaint(new Color(200, 200, 200));
                linePlot.setDomainGridlinePaint(new Color(200, 200, 200));
                linePlot.setOutlineVisible(false);

                LineAndShapeRenderer lineRenderer = (LineAndShapeRenderer) linePlot.getRenderer();
                lineRenderer.setSeriesPaint(0, Color.BLACK);
                lineRenderer.setDrawOutlines(true);
                lineRenderer.setSeriesStroke(0, new BasicStroke(2f));
                lineRenderer.setDefaultShapesFilled(true);
                break;

            default:
                throw new IllegalArgumentException("Unsupported chart type: " + info.getChartType());
        }

        return chart;
    }


}