package cz.uhk.fim.ppro.drevenka.dto;

import java.math.BigDecimal;

public class CategoryReportItem {
    private String categoryName;
    private int itemsSold;
    private BigDecimal revenue;
    private BigDecimal margin;
    private double revenueSharePercentage;

    public CategoryReportItem(String categoryName, int itemsSold, BigDecimal revenue, BigDecimal margin, double revenueSharePercentage) {
        this.categoryName = categoryName;
        this.itemsSold = itemsSold;
        this.revenue = revenue;
        this.margin = margin;
        this.revenueSharePercentage = revenueSharePercentage;
    }

    public String getCategoryName() { return categoryName; }
    public int getItemsSold() { return itemsSold; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getMargin() { return margin; }
    public double getRevenueSharePercentage() { return revenueSharePercentage; }
}
