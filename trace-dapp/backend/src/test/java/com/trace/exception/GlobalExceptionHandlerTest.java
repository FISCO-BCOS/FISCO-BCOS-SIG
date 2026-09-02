package com.trace.exception;

import com.trace.model.Result;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @Test
    void returnsClientErrorForInvalidRequestBody() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registerDTO");
        bindingResult.addError(new FieldError("registerDTO", "username", "用户名长度需在3-20个字符之间"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(org.springframework.core.MethodParameter.class), bindingResult);

        Result<String> result = new GlobalExceptionHandler().handleValidationException(exception);

        assertEquals(400, result.getCode());
        assertEquals("用户名长度需在3-20个字符之间", result.getMsg());
    }
}
