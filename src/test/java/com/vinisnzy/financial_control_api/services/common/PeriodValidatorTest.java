package com.vinisnzy.financial_control_api.services.common;

import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodValidatorTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    @Test
    void hasPeriod_bothNull_shouldReturnFalse() {
        assertThat(PeriodValidator.hasPeriod(null, null)).isFalse();
    }

    @Test
    void hasPeriod_validPeriod_shouldReturnTrue() {
        assertThat(PeriodValidator.hasPeriod(START, END)).isTrue();
    }

    @Test
    void hasPeriod_sameDay_shouldReturnTrue() {
        assertThat(PeriodValidator.hasPeriod(START, START)).isTrue();
    }

    @Test
    void hasPeriod_onlyStart_shouldThrow() {
        assertThatThrownBy(() -> PeriodValidator.hasPeriod(START, null))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_PERIOD);
    }

    @Test
    void hasPeriod_onlyEnd_shouldThrow() {
        assertThatThrownBy(() -> PeriodValidator.hasPeriod(null, END))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_PERIOD);
    }

    @Test
    void hasPeriod_startAfterEnd_shouldThrow() {
        assertThatThrownBy(() -> PeriodValidator.hasPeriod(END, START))
                .isInstanceOf(BusinessException.class)
                .hasMessage("'start' must not be after 'end'.");
    }
}
