package com.vinisnzy.financial_control_api.config;

import com.vinisnzy.financial_control_api.pagination.SortableBy;
import com.vinisnzy.financial_control_api.pagination.SortableByArgumentResolver;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SortableOpenApiCustomizer {

    @Bean
    OperationCustomizer sortableFieldsCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getParameters() == null) {
                return operation;
            }
            for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
                SortableBy sortableBy = parameter.getParameterAnnotation(SortableBy.class);
                if (sortableBy == null || !Pageable.class.isAssignableFrom(parameter.getParameterType())) {
                    continue;
                }
                String allowed = String.join(", ", SortableByArgumentResolver.allowedProperties(sortableBy));
                String description = "Sort as `property[,asc|desc]`; repeat the parameter to sort by several. "
                        + "Sortable fields: " + allowed + ". Default: " + sortableBy.defaultSort() + ".";
                for (Parameter p : operation.getParameters()) {
                    if ("sort".equals(p.getName())) {
                        p.setDescription(description);
                    }
                }
            }
            return operation;
        };
    }
}
