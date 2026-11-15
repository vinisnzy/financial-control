package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.services.income.IncomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncomeController.class)
class IncomeSortWhitelistControllerTest extends ControllerTest {

    @MockitoBean
    IncomeService service;

    @BeforeEach
    void stubService() {
        when(service.getAll(isNull(), isNull(), eq(authUser.getId()), any()))
                .thenReturn(new PageResponse<IncomeResponse>(List.of(), 0, 20, 0L, 0, true, true));
    }

    @Test
    void getAll_sortByAllowedField_shouldPassSortToService() throws Exception {
        mvc.perform(get("/incomes").param("sort", "expectedAt,desc").with(user(authUser)))
                .andExpect(status().isOk());

        assertThat(capturePageable().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "expectedAt"));
    }

    @Test
    void getAll_withoutSort_shouldApplyDefaultCreatedAtDesc() throws Exception {
        mvc.perform(get("/incomes").with(user(authUser)))
                .andExpect(status().isOk());

        assertThat(capturePageable().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void getAll_sortByNotWhitelistedField_shouldReturn400ListingAllowedFields() throws Exception {
        mvc.perform(get("/incomes").param("sort", "bankAccount").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value(
                        "Invalid sort property 'bankAccount'. Allowed: name, amount, expectedAt, receivedAt, createdAt."));

        verify(service, never()).getAll(any(), any(), any(), any());
    }

    @Test
    void sortFields_shouldAllBeIncomeProperties() {
        for (IncomeSortField field : IncomeSortField.values()) {
            assertThat(hasField(Income.class, field.property()))
                    .as("Income.%s", field.property()).isTrue();
        }
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAll(isNull(), isNull(), eq(authUser.getId()), captor.capture());
        return captor.getValue();
    }

    private static boolean hasField(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
