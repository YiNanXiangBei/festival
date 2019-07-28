package cn.bdqfork.ioc.example;

import cn.bdqfork.core.context.AnnotationApplicationContext;
import cn.bdqfork.core.context.ApplicationContext;
import cn.bdqfork.core.exception.SpringToyException;
import cn.bdqfork.ioc.example.controller.UserController;

/**
 * @author bdq
 * @date 2019-02-19
 */
public class TestIoc {
    public static void main(String[] args) throws SpringToyException, InterruptedException {
        ApplicationContext applicationContext = new AnnotationApplicationContext("cn");
        UserController userController = applicationContext.getBean(UserController.class);
        System.out.println(userController.getUsername());
        System.out.println(userController.getCreateTime().toString());
        Thread.sleep(10000);
        System.out.println(userController.getCreateTime().toString());
    }
}
