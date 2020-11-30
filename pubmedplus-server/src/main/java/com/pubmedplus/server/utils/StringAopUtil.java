package com.pubmedplus.server.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

@Aspect
@Component
public class StringAopUtil {
	
	@Pointcut("execution(* com.pubmedplus.server.controller.*.*(..))")
	private void aop() {}

	@Around("aop()")
	@Order(2)
	private String apoAround2(ProceedingJoinPoint thisJoinPoint) {
		var responseJson = new JSONObject();
		try {
			var res = (String) thisJoinPoint.proceed(thisJoinPoint.getArgs());
			responseJson.put("res", res);
			responseJson.put("status", 200);
			responseJson.put("message", "success");
		} catch (Throwable e) {
			e.printStackTrace();
			responseJson.put("status", 500);
			responseJson.put("message", e.getMessage());
		}
		return responseJson.toString();
	}
	
}


