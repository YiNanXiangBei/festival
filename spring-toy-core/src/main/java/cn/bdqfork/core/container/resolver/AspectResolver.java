package cn.bdqfork.core.container.resolver;

import cn.bdqfork.core.aop.Advisor;
import cn.bdqfork.core.aop.aspect.*;
import cn.bdqfork.core.container.BeanDefinition;
import cn.bdqfork.core.exception.ResolvedException;
import cn.bdqfork.core.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author bdq
 * @since 2019-07-31
 */
public class AspectResolver implements Resolver<Map<String, List<Advisor>>> {
    private Collection<BeanDefinition> beanDefinitions;

    public AspectResolver(Collection<BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
    }

    @Override
    public Map<String, List<Advisor>> resolve() throws ResolvedException {
        Map<String, List<Advisor>> beanAdvisorMapper = new HashMap<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinition.getClazz().getAnnotation(Aspect.class) != null) {
                List<Advisor> advisors = resolveAdvisors(beanDefinition);
                beanAdvisorMapper.put(beanDefinition.getBeanName(), advisors);
            }
        }
        return beanAdvisorMapper;
    }

    private List<Advisor> resolveAdvisors(BeanDefinition beanDefinition) {
        List<Advisor> advisors = new LinkedList<>();
        Class<?> clazz = beanDefinition.getClazz();
        Map<String, String> pointcuts = new HashMap<>();
        Method[] methods = clazz.getDeclaredMethods();
        //先解析pointcut，防止npe问题
        for (Method method : methods) {
            resolvePointcut(pointcuts, method);
        }
        for (Method method : methods) {
            method.setAccessible(true);
            resolveBeforeAdvice(advisors, pointcuts, method);
            resolveAfterReturningAdvice(advisors, pointcuts, method);
            resolveAroundAdvice(advisors, pointcuts, method);
            resolveAfterThrowingAdvice(advisors, pointcuts, method);
        }
        return advisors;
    }

    private void resolvePointcut(Map<String, String> pointcuts, Method method) {
        Pointcut pointcut = method.getAnnotation(Pointcut.class);
        if (pointcut != null) {
            pointcuts.put(method.getName() + "()", pointcut.value());
        }
    }

    private void resolveBeforeAdvice(List<Advisor> advisors, Map<String, String> pointcuts, Method method) {
        Before before = method.getAnnotation(Before.class);
        if (before == null) {
            return;
        }
        AspectMethodBeforeAdvice beforeAdvice = new AspectMethodBeforeAdvice();
        beforeAdvice.setAdviceMethod(method);
        AspectAdvisor aspectAdvisor = getAspectAdvisor(pointcuts, before.value(), beforeAdvice);
        advisors.add(aspectAdvisor);
    }

    private void resolveAfterReturningAdvice(List<Advisor> advisors, Map<String, String> pointcuts, Method method) {
        AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
        if (afterReturning == null) {
            return;
        }
        AspectAfterReturningAdvice afterReturningAdvice = new AspectAfterReturningAdvice();
        afterReturningAdvice.setAdviceMethod(method);

        AspectAdvisor aspectAdvisor = getAspectAdvisor(pointcuts, afterReturning.value(), afterReturningAdvice);
        advisors.add(aspectAdvisor);
    }

    private void resolveAroundAdvice(List<Advisor> advisors, Map<String, String> pointcuts, Method method) {
        Around around = method.getAnnotation(Around.class);
        if (around == null) {
            return;
        }
        AspectAroundAdvice aroundAdvice = new AspectAroundAdvice();
        aroundAdvice.setAdviceMethod(method);

        AspectAdvisor aspectAdvisor = getAspectAdvisor(pointcuts, around.value(), aroundAdvice);
        advisors.add(aspectAdvisor);
    }

    private void resolveAfterThrowingAdvice(List<Advisor> advisors, Map<String, String> pointcuts, Method method) {
        AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
        if (afterThrowing == null) {
            return;
        }
        AspectThrowsAdvice aspectThrowsAdvice = new AspectThrowsAdvice();
        aspectThrowsAdvice.setAdviceMethod(method);

        AspectAdvisor aspectAdvisor = getAspectAdvisor(pointcuts, afterThrowing.value(), aspectThrowsAdvice);
        advisors.add(aspectAdvisor);
    }

    private AspectAdvisor getAspectAdvisor(Map<String, String> pointcuts, String pointcut, AspectAdvice aspectAdvice) {
        AspectAdvisor aspectAdvisor = new AspectAdvisor();
        aspectAdvisor.setAspectAdvice(aspectAdvice);

        String expression = pointcut;
        if (!pointcut.startsWith("execution")) {
            expression = pointcuts.get(pointcut);
        }
        aspectAdvisor.setPointcut(StringUtils.substring(expression, 10, expression.length() - 1));
        return aspectAdvisor;
    }

}
