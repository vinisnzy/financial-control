package com.vinisnzy.financial_control_api.jobs;

import com.vinisnzy.financial_control_api.services.income.FixedIncomeService;
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
class MonthlyIncomeGeneratorJobTest {

    @Mock
    FixedIncomeService fixedIncomeService;

    @InjectMocks
    MonthlyIncomeGeneratorJob job;

    @Test
    void generate_shouldCallServiceWithCurrentMonthInSaoPaulo() {
        when(fixedIncomeService.generateForMonth(any())).thenReturn(2);

        job.generate("scheduled");

        verify(fixedIncomeService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void generate_whenServiceFails_shouldNotPropagate() {
        when(fixedIncomeService.generateForMonth(any())).thenThrow(new RuntimeException("boom"));

        assertThatCode(() -> job.generate("scheduled")).doesNotThrowAnyException();
    }

    @Test
    void catchUp_shouldGenerateForCurrentMonth() {
        job.catchUp();

        verify(fixedIncomeService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void run_shouldGenerateForCurrentMonth() {
        job.run();

        verify(fixedIncomeService).generateForMonth(YearMonth.now(ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void catchUpAndRun_shouldEachCallTheService() {
        job.catchUp();
        job.run();

        verify(fixedIncomeService, times(2)).generateForMonth(any());
    }
}
