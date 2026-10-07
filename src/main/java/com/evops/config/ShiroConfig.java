package com.evops.config;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {
    @Bean
    public Realm localRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("bootstrap", "bootstrap", "system");
        return realm;
    }

    /**
     * Shiro 自动配置注册的 defaultAdvisorAutoProxyCreator 默认走 JDK 动态代理，
     * 而本项目 Service 为无接口的具体类，注入具体类型会失败。
     * 在 Bean 定义阶段把该创建器强制为 CGLIB 代理。
     */
    @Bean
    public static BeanFactoryPostProcessor shiroProxyTargetClassPostProcessor() {
        return beanFactory -> {
            if (beanFactory instanceof BeanDefinitionRegistry
                    && ((BeanDefinitionRegistry) beanFactory).containsBeanDefinition("defaultAdvisorAutoProxyCreator")) {
                ((DefaultListableBeanFactory) beanFactory).getBeanDefinition("defaultAdvisorAutoProxyCreator")
                        .getPropertyValues().add("proxyTargetClass", Boolean.TRUE);
            }
        };
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chain = new DefaultShiroFilterChainDefinition();
        chain.addPathDefinition("/api/health", "anon");
        chain.addPathDefinition("/error", "anon");
        // 业务接口启用 HTTP Basic（bootstrap/bootstrap），便于 curl 联调
        chain.addPathDefinition("/api/**", "authcBasic");
        return chain;
    }
}
