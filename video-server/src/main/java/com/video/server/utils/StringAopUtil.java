package com.video.server.utils;

import net.sf.json.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StringAopUtil {

    @Pointcut("execution(* com.video.server.controller.*.*(..))")
    private void aop() {
    }

    /**
     * 环绕通知,监控方法是否正常
     *
     * @param thisJoinPoint
     * @return
     */
    @Around("aop()")
    @Order(2)
    private String apoAround2(ProceedingJoinPoint thisJoinPoint) {
        // 返回的对象json
        var responseJson = new JSONObject();
        try {
            var res = (String) thisJoinPoint.proceed(thisJoinPoint.getArgs());
            responseJson.put("res", res);
            responseJson.put("status", 200);
            responseJson.put("message", "success");
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            responseJson.put("status", 500);
            responseJson.put("message", e.getMessage());
        }
        return responseJson.toString();
    }

}


