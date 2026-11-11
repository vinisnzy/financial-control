package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseCategoryResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.ExpenseCategoryMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseCategoryRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceTest {

    @Mock
    ExpenseCategoryRepository repository;
    @Mock
    ExpenseCategoryMapper mapper;
    @Mock
    UserService userService;

    @InjectMocks
    ExpenseCategoryService service;

    private UUID userId;
    private UUID categoryId;
    private User user;
    private ExpenseCategory entity;
    private ExpenseCategoryResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        entity = new ExpenseCategory();
        ReflectionTestUtils.setField(entity, "id", categoryId);
        entity.setName("Food");
        entity.setColor("#FF0000");
        entity.setUser(user);

        response = new ExpenseCategoryResponse(categoryId, "Food", "#FF0000", Instant.now(), null);
    }

    @Test
    void getEntityOwnedOrDefaultByIdOrThrow_shouldReturnEntity() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.of(entity));

        assertThat(service.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).isSameAs(entity);
    }

    @Test
    void getEntityOwnedOrDefaultByIdOrThrow_defaultCategory_shouldReturnEntity() {
        ExpenseCategory defaultCategory = new ExpenseCategory();
        defaultCategory.setIsDefault(true);
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.of(defaultCategory));

        assertThat(service.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).isSameAs(defaultCategory);
    }

    @Test
    void getEntityOwnedOrDefaultByIdOrThrow_whenNotFound_shouldThrow() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(categoryId.toString());
    }

    @Test
    void getAll_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByUserIdOrDefault(userId, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getAll(userId, pageable);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(categoryId, userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotFound_shouldThrow() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(categoryId, userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void create_shouldSetUserAndSave() {
        var request = new CreateExpenseCategoryRequest("Food", "#FF0000");
        ExpenseCategory newEntity = new ExpenseCategory();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<ExpenseCategory> captor = ArgumentCaptor.forClass(ExpenseCategory.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void create_whenUserNotFound_shouldThrowAndNotSave() {
        var request = new CreateExpenseCategoryRequest("Food", "#FF0000");
        when(mapper.toEntity(request)).thenReturn(new ExpenseCategory());
        when(userService.getEntity(userId)).thenThrow(NotFoundException.of("User", userId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldApplyChangesAndSave() {
        var request = new UpdateExpenseCategoryRequest("Market", null);
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(categoryId, request, userId)).isEqualTo(response);

        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrowAndNotSave() {
        var request = new UpdateExpenseCategoryRequest("Market", null);
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(categoryId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteEntity() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.of(entity));

        service.delete(categoryId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotFound_shouldNotDelete() {
        when(repository.findByIdAndUserIdOrDefault(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(categoryId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
