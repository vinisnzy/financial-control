package com.vinisnzy.financial_control_api.controllers.dashboard;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryShare;
import com.vinisnzy.financial_control_api.dtos.dashboard.InstallmentProjection;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthComparison;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthlySummaryResponse;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.services.dashboard.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest extends ControllerTest {

    @MockitoBean
    DashboardService service;

    @Test
    void getMonthlySummary_shouldReturn200AndPassMonthAndUserId() throws Exception {
        var month = YearMonth.of(2026, 3);
        var response = new MonthlySummaryResponse(month,
                amount("5000.00"), amount("3000.00"), amount("4200.00"),
                amount("1000.00"), amount("800.00"), amount("2000.00"));
        when(service.getMonthlySummary(month, authUser.getId())).thenReturn(response);

        mvc.perform(get("/dashboard/summary").param("month", "2026-03").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-03"))
                .andExpect(jsonPath("$.plannedIncome").value(5000.0))
                .andExpect(jsonPath("$.realizedBalance").value(2000.0));
    }

    @Test
    void getMonthlySummary_withoutMonth_shouldDefaultToCurrentMonth() throws Exception {
        var current = YearMonth.now();
        when(service.getMonthlySummary(current, authUser.getId())).thenReturn(
                new MonthlySummaryResponse(
                        current,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));

        mvc.perform(get("/dashboard/summary").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(current.toString()));

        verify(service).getMonthlySummary(current, authUser.getId());
    }

    @Test
    void getMonthlySummary_withInvalidMonth_shouldReturn400() throws Exception {
        mvc.perform(get("/dashboard/summary").param("month", "2026-13").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getExpensesByCategory_shouldReturn200WithList() throws Exception {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        var categoryId = UUID.randomUUID();
        when(service.getExpensesByCategory(start, end, authUser.getId()))
                .thenReturn(List.of(new CategoryShare(categoryId, "Rent", amount("900.00"), amount("75.00"))));

        mvc.perform(get("/dashboard/expenses-by-category")
                        .param("start", "2026-03-01").param("end", "2026-03-31").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].categoryName").value("Rent"))
                .andExpect(jsonPath("$[0].percentage").value(75.0));
    }

    @Test
    void getIncomesByCategory_shouldReturn200WithList() throws Exception {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        when(service.getIncomesByCategory(start, end, authUser.getId()))
                .thenReturn(List.of(new CategoryShare(UUID.randomUUID(), "Salary", amount("900.00"), amount("90.00"))));

        mvc.perform(get("/dashboard/incomes-by-category")
                        .param("start", "2026-03-01").param("end", "2026-03-31").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Salary"));
    }

    @Test
    void getByCategory_withoutRequiredParams_shouldReturn400() throws Exception {
        mvc.perform(get("/dashboard/expenses-by-category").with(user(authUser)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/dashboard/incomes-by-category").param("start", "2026-03-01").with(user(authUser)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getByCategory_withInvalidDate_shouldReturn400() throws Exception {
        mvc.perform(get("/dashboard/expenses-by-category")
                        .param("start", "01/03/2026").param("end", "2026-03-31").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByCategory_whenServiceRejectsPeriod_shouldReturn400WithInvalidPeriod() throws Exception {
        var start = LocalDate.of(2026, 4, 1);
        var end = LocalDate.of(2026, 3, 1);
        when(service.getExpensesByCategory(start, end, authUser.getId()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PERIOD, "'start' must not be after 'end'."));

        mvc.perform(get("/dashboard/expenses-by-category")
                        .param("start", "2026-04-01").param("end", "2026-03-01").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void getMonthlyCashFlow_shouldReturn200AndPassMonths() throws Exception {
        var month = YearMonth.of(2026, 3);
        when(service.getMonthlyCashFlow(3, authUser.getId())).thenReturn(List.of(
                new MonthComparison(month, amount("1000.00"), amount("400.00"), amount("600.00"))));

        mvc.perform(get("/dashboard/monthly-cash-flow").param("months", "3").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-03"))
                .andExpect(jsonPath("$[0].balance").value(600.0));
    }

    @Test
    void getMonthlyCashFlow_withoutMonths_shouldDefaultToSix() throws Exception {
        when(service.getMonthlyCashFlow(6, authUser.getId())).thenReturn(List.of());

        mvc.perform(get("/dashboard/monthly-cash-flow").with(user(authUser)))
                .andExpect(status().isOk());

        verify(service).getMonthlyCashFlow(6, authUser.getId());
    }

    @Test
    void getMonthlyCashFlow_withNonNumericMonths_shouldReturn400() throws Exception {
        mvc.perform(get("/dashboard/monthly-cash-flow").param("months", "abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getMonthlyCashFlow_whenServiceRejectsMonths_shouldReturn400WithInvalidPeriod() throws Exception {
        when(service.getMonthlyCashFlow(25, authUser.getId()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PERIOD, "'months' must be between 1 and 24."));

        mvc.perform(get("/dashboard/monthly-cash-flow").param("months", "25").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void getInstallmentsProjection_shouldReturn200AndPassMonths() throws Exception {
        var month = YearMonth.of(2026, 4);
        when(service.getInstallmentsProjection(3, authUser.getId()))
                .thenReturn(List.of(new InstallmentProjection(month, amount("100.00"))));

        mvc.perform(get("/dashboard/installments-projection").param("months", "3").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-04"))
                .andExpect(jsonPath("$[0].committed").value(100.0));
    }

    @Test
    void getInstallmentsProjection_withoutMonths_shouldDefaultToTwelve() throws Exception {
        when(service.getInstallmentsProjection(12, authUser.getId())).thenReturn(List.of());

        mvc.perform(get("/dashboard/installments-projection").with(user(authUser)))
                .andExpect(status().isOk());

        verify(service).getInstallmentsProjection(12, authUser.getId());
    }

    @Test
    void dashboard_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/dashboard/summary")).andExpect(status().isUnauthorized());
        mvc.perform(get("/dashboard/monthly-cash-flow")).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
