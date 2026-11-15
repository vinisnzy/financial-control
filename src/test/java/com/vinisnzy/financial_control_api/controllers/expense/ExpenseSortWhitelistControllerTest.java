package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.services.expense.ExpenseService;
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

@WebMvcTest(ExpenseController.class)
class ExpenseSortWhitelistControllerTest extends ControllerTest {

    @MockitoBean
    ExpenseService service;

    @BeforeEach
    void stubService() {
        when(service.getAll(isNull(), isNull(), eq(authUser.getId()), any()))
                .thenReturn(new PageResponse<ExpenseResponse>(List.of(), 0, 20, 0L, 0, true, true));
    }

    @Test
    void getAll_sortByAllowedField_shouldPassSortToService() throws Exception {
        mvc.perform(get("/expenses").param("sort", "amount,asc").with(user(authUser)))
                .andExpect(status().isOk());

        assertThat(capturePageable().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "amount"));
    }

    @Test
    void getAll_withoutSort_shouldApplyDefaultCreatedAtDesc() throws Exception {
        mvc.perform(get("/expenses").with(user(authUser)))
                .andExpect(status().isOk());

        assertThat(capturePageable().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void getAll_sortByRelationship_shouldReturn400ListingAllowedFields() throws Exception {
        mvc.perform(get("/expenses").param("sort", "user.email").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value(
                        "Invalid sort property 'user.email'. Allowed: name, amount, dueDate, paidAt, createdAt."));

        verify(service, never()).getAll(any(), any(), any(), any());
    }

    @Test
    void sortFields_shouldAllBeExpenseProperties() {
        for (ExpenseSortField field : ExpenseSortField.values()) {
            assertThat(hasField(Expense.class, field.property()))
                    .as("Expense.%s", field.property()).isTrue();
        }
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAll(isNull(), isNull(), eq(authUser.getId()), captor.capture());
        return captor.getValue();
    }

    static boolean hasField(Class<?> type, String name) {
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
