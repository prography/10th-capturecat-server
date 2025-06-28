package com.capturecat.core.support.aop;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
@Aspect
public class CommonValidationAdvice {

	@Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
	public void postMapping() {
	}

	@Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
	public void putMapping() {
	}

	@Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
	public void deleteMapping() {
	}

	/**
	 * validation error 가 있으면 이곳에서 공통 예외 처리를 한다.
	 */
	@Around("postMapping() || putMapping() || deleteMapping()")
	public Object validationAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		for (Object arg : args) {
			if (arg instanceof BindingResult bindingResult && bindingResult.hasErrors()) {
				Map<String, String> errorMap = new HashMap<>();
				bindingResult.getFieldErrors().forEach(err -> errorMap.put(err.getField(), err.getDefaultMessage()));
				throw new CoreException(ErrorType.VALIDATION_FAIL); //TODO: errorMap 전달
			}
		}
		return joinPoint.proceed();
	}
}
