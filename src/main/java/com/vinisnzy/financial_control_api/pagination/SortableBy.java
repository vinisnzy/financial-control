package com.vinisnzy.financial_control_api.pagination;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SortableBy {
    Class<? extends SortField> value();

    String defaultSort() default "createdAt,desc";
}
