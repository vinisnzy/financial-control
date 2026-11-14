package com.vinisnzy.financial_control_api.controllers.dashboard;

import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryShare;
import com.vinisnzy.financial_control_api.dtos.dashboard.InstallmentProjection;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthComparison;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthlySummaryResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Dashboard", description = "Financial summaries and reports")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @Operation(
            summary = "Monthly summary",
            description = "Returns the authenticated user's incomes, expenses and balance for the month, "
                    + "split into planned  and realized. "
                    + "Defaults to the current month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid month", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Month (yyyy-MM). Defaults to the current month", example = "2026-11")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @AuthenticationPrincipal User user
    ) {
        YearMonth target = month != null ? month : YearMonth.now();
        return ResponseEntity.ok(this.service.getMonthlySummary(target, user.getId()));
    }

    @Operation(
            summary = "Expenses by category",
            description = "Returns the total and percentage spent per expense category in the period, "
                    + "ordered from the highest total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses grouped successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing period", content = @Content)
    })
    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryShare>> getExpensesByCategory(
            @Parameter(description = "Period start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getExpensesByCategory(start, end, user.getId()));
    }

    @Operation(
            summary = "Incomes by category",
            description = "Returns the total and percentage received per income category in the period, "
                    + "ordered from the highest total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incomes grouped successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing period", content = @Content)
    })
    @GetMapping("/incomes-by-category")
    public ResponseEntity<List<CategoryShare>> getIncomesByCategory(
            @Parameter(description = "Period start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getIncomesByCategory(start, end, user.getId()));
    }

    @Operation(
            summary = "Monthly cash flow",
            description = "Returns income, expense and balance for each of the last N months, "
                    + "including the current one. Months without records come back as zero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparison calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid number of months", content = @Content)
    })
    @GetMapping("/monthly-cash-flow")
    public ResponseEntity<List<MonthComparison>> getMonthlyCashFlow(
            @Parameter(description = "Number of months to look back (1 to 24)")
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getMonthlyCashFlow(months, user.getId()));
    }

    @Operation(
            summary = "Installments projection",
            description = "Returns the amount already committed in unpaid installments for each of the next "
                    + "N months. Months without installments come back as zero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projection calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid number of months", content = @Content)
    })
    @GetMapping("/installments-projection")
    public ResponseEntity<List<InstallmentProjection>> getInstallmentsProjection(
            @Parameter(description = "Number of future months to project (1 to 24)")
            @RequestParam(defaultValue = "12") int months,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getInstallmentsProjection(months, user.getId()));
    }
}
