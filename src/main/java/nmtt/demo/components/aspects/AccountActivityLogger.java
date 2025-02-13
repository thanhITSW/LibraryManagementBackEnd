package nmtt.demo.components.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
public class AccountActivityLogger {

    //named pointcut
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods(){

    }

    @Around("controllerMethods() && execution(* nmtt.demo.controller.AccountController.*(..))")
    public Object logAccountActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        log.info("Account activity started: {}, IP address: {}", methodName, remoteAddress);

        //implement origin method
        Object result = joinPoint.proceed();

        //write log after implement method
        log.info("User activity finished: {} ", methodName);
        return result;
    }
}
