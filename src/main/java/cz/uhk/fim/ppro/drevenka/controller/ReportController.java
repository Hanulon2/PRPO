package cz.uhk.fim.ppro.drevenka.controller;

import cz.uhk.fim.ppro.drevenka.dto.CategoryReportItem;
import cz.uhk.fim.ppro.drevenka.service.ReportingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping
    public String index(Model model) {
        YearMonth currentMonth = YearMonth.now();
        List<CategoryReportItem> report = reportingService.getMonthlyCategoryReport(currentMonth);

        model.addAttribute("report", report);
        model.addAttribute("monthName", currentMonth.getMonth().name() + " " + currentMonth.getYear());
        return "reports/index";
    }
}
