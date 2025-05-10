package nmtt.demo.components.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Aspect
@Slf4j
public class PerformanceAspect {
    private String getMethodName(JoinPoint joinPoint){
        return joinPoint.getSignature().getName();
    }

    @Pointcut("within(nmtt.demo.controller.*)")
    public void controllerMethods(){

    }

    @Before("controllerMethods()")
    public void beforeMethodExecution(JoinPoint joinPoint){
        log.info("Starting execution of {} ", this.getMethodName(joinPoint));
    }

    @After("controllerMethods()")
    public void afterMethodExecution(JoinPoint joinPoint){
        log.info("Finished execution of {} ", this.getMethodName(joinPoint));
    }

    @Around("controllerMethods()")
    public Object measureControllerMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint)
            throws Throwable {

        long start = System.nanoTime();
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.nanoTime();

        String methodName = proceedingJoinPoint.getSignature().getName();
        log.info("Execution of {} took {} ms", methodName, TimeUnit.NANOSECONDS.toMillis(end - start));

        return returnValue;
    }
}
