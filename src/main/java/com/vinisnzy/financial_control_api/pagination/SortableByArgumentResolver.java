package com.vinisnzy.financial_control_api.pagination;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;

public class SortableByArgumentResolver implements HandlerMethodArgumentResolver {

    private final PageableHandlerMethodArgumentResolver delegate = new PageableHandlerMethodArgumentResolver();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.isAssignableFrom(parameter.getParameterType())
                && parameter.hasParameterAnnotation(SortableBy.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        SortableBy annotation = parameter.getParameterAnnotation(SortableBy.class);
        Pageable pageable = delegate.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        return SortValidator.validate(pageable, allowedProperties(annotation), annotation.defaultSort());
    }

    public static List<String> allowedProperties(SortableBy annotation) {
        return Arrays.stream(annotation.value().getEnumConstants())
                .map(SortField::property)
                .toList();
    }
}
