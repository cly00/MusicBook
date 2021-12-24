package com.cn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * @author ngcly
 * @version V1.0
 * @since 2021/8/24 22:43
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**设置swagger 为默认主页*/
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/swagger-ui.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        WebMvcConfigurer.super.addViewControllers(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //添加映射路径
        registry.addMapping("/**")
                //设置放行哪些原始域
                .allowedOriginPatterns("*")
                //是否发送Cookie
                .allowCredentials(true)
                //放行哪些请求方式
                .allowedMethods("*")
                //允许头部
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }
}
