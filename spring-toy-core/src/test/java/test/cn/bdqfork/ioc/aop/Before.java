package test.cn.bdqfork.ioc.aop;

import cn.bdqfork.core.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @author bdq
 * @since 2019-07-30
 */
public class Before implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("before");
    }
}