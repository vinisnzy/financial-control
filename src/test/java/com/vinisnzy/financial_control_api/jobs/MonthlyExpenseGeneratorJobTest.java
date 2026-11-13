package com.vinisnzy.financial_control_api.jobs;

import com.vinisnzy.financial_control_api.services.expense.FixedExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyExpenseGeneratorJobTest {

    @Mock
    FixedExpenseService fixedExpenseService;

    @InjectMocks
    MonthlyExpenseGeneratorJob job;

    @Test
    void generate_shouldCallServiceWithCurrentMonthInSaoPaulo() {
        when(fixedExpenseService.generateForMonth(any())).thenReturn(2);

        job.generate("scheduled");

        verify(fixedExpenseService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void generate_whenServiceFails_shouldNotPropagate() {
        when(fixedExpenseService.generateForMonth(any())).thenThrow(new RuntimeException("boom"));

        assertThatCode(() -> job.generate("scheduled")).doesNotThrowAnyException();
    }

    @Test
    void catchUp_shouldGenerateForCurrentMonth() {
        job.catchUp();

        verify(fixedExpenseService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void run_shouldGenerateForCurrentMonth() {
        job.run();

        verify(fixedExpenseService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void catchUpAndRun_shouldEachCallTheService() {
        job.catchUp();
        job.run();

        verify(fixedExpenseService, times(2)).generateForMonth(any());
    }
}
