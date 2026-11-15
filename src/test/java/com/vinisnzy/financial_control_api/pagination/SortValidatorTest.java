package com.vinisnzy.financial_control_api.pagination;

import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortValidatorTest {

    private static final List<String> ALLOWED = List.of("name", "amount", "createdAt");
    private static final String DEFAULT_SORT = "createdAt,desc";

    @Test
    void validate_allowedProperty_shouldKeepPageable() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "name"));

        Pageable result = SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT);

        assertThat(result).isEqualTo(pageable);
    }

    @Test
    void validate_multipleAllowedProperties_shouldKeepPageable() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("amount"), Sort.Order.asc("name")));

        assertThat(SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT)).isEqualTo(pageable);
    }

    @Test
    void validate_propertyOutsideWhitelist_shouldThrowInvalidParameterListingAllowedFields() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("paymentMethod"));

        assertThatThrownBy(() -> SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("'paymentMethod'")
                .hasMessageContaining("name, amount, createdAt")
                .extracting("code").isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    void validate_nestedRelationshipProperty_shouldThrow() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("user.email"));

        assertThatThrownBy(() -> SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("'user.email'");
    }

    @Test
    void validate_oneInvalidAmongValidProperties_shouldThrow() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name", "id"));

        assertThatThrownBy(() -> SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("'id'");
    }

    @Test
    void validate_propertyWithDifferentCase_shouldThrow() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("CreatedAt"));

        assertThatThrownBy(() -> SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validate_unsorted_shouldApplyDefaultSortKeepingPaging() {
        Pageable pageable = PageRequest.of(2, 7);

        Pageable result = SortValidator.validate(pageable, ALLOWED, DEFAULT_SORT);

        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(7);
        assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void validate_unsortedWithAscendingDefault_shouldApplyDirection() {
        Pageable result = SortValidator.validate(PageRequest.of(0, 10), ALLOWED, "name,asc");

        assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void validate_unsortedWithDefaultWithoutDirection_shouldSortAscending() {
        Pageable result = SortValidator.validate(PageRequest.of(0, 10), ALLOWED, "name");

        assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void validate_unsortedWithBlankDefault_shouldStayUnsorted() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThat(SortValidator.validate(pageable, ALLOWED, "")).isEqualTo(pageable);
    }

    @Test
    void validate_defaultSortOutsideWhitelist_shouldThrowIllegalState() {
        assertThatThrownBy(() -> SortValidator.validate(PageRequest.of(0, 10), ALLOWED, "id,desc"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validate_unpaged_shouldReturnUnchanged() {
        assertThat(SortValidator.validate(Pageable.unpaged(), ALLOWED, DEFAULT_SORT)).isEqualTo(Pageable.unpaged());
    }
}
