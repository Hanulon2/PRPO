package cz.uhk.fim.ppro.drevenka.service;

import cz.uhk.fim.ppro.drevenka.domain.Category;
import cz.uhk.fim.ppro.drevenka.domain.CustomerOrder;
import cz.uhk.fim.ppro.drevenka.domain.OrderItem;
import cz.uhk.fim.ppro.drevenka.domain.OrderStatus;
import cz.uhk.fim.ppro.drevenka.dto.CategoryReportItem;
import cz.uhk.fim.ppro.drevenka.repository.CategoryRepository;
import cz.uhk.fim.ppro.drevenka.repository.CustomerOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final CategoryRepository categoryRepository;
    private final CustomerOrderRepository orderRepository;

    public ReportingService(CategoryRepository categoryRepository, CustomerOrderRepository orderRepository) {
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    public List<CategoryReportItem> getMonthlyCategoryReport(YearMonth month) {
        List<Category> categories = categoryRepository.findAll();
        List<CustomerOrder> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        Map<String, Integer> countMap = new HashMap<>();
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        Map<String, BigDecimal> marginMap = new HashMap<>();

        for (Category c : categories) {
            countMap.put(c.getName(), 0);
            revenueMap.put(c.getName(), BigDecimal.ZERO);
            marginMap.put(c.getName(), BigDecimal.ZERO);
        }

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (CustomerOrder o : shippedOrders) {
            for (OrderItem item : o.getItems()) {
                BigDecimal itemRevenue = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal itemCost = item.getProduct().getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal itemMargin = itemRevenue.subtract(itemCost);

                grandTotal = grandTotal.add(itemRevenue);

                for (Category cat : item.getProduct().getCategories()) {
                    String catName = cat.getName();
                    countMap.put(catName, countMap.getOrDefault(catName, 0) + item.getQuantity());
                    revenueMap.put(catName, revenueMap.getOrDefault(catName, BigDecimal.ZERO).add(itemRevenue));
                    marginMap.put(catName, marginMap.getOrDefault(catName, BigDecimal.ZERO).add(itemMargin));
                }
            }
        }

        List<CategoryReportItem> result = new ArrayList<>();
        for (Category c : categories) {
            String name = c.getName();
            BigDecimal rev = revenueMap.getOrDefault(name, BigDecimal.ZERO);
            BigDecimal mar = marginMap.getOrDefault(name, BigDecimal.ZERO);
            int count = countMap.getOrDefault(name, 0);

            double share = 0.0;
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                share = rev.divide(grandTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            }

            result.add(new CategoryReportItem(name, count, rev, mar, Math.round(share * 10.0) / 10.0));
        }

        result.sort(Comparator.comparing(CategoryReportItem::getRevenue).reversed());
        return result;
    }
}
