package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

@Data
@Builder
@AllArgsConstructor
public class ChartInformation {
    String title;
    ChartType chartType;
    CategoryDataset categoryDataset;
    PieDataset pieDataset;
    String categoryAxisLabel;
    String valueAxisLabel;
}
