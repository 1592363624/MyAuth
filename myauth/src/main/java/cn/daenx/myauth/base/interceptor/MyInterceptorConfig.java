package cn.daenx.myauth.base.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 * 执行顺序：AdminAuthInterceptor(1) → ApiValidateInterceptor(2) → BanCheckInterceptor(3)
 *
 * @author DaenMax
 */
@Configuration
public class MyInterceptorConfig implements WebMvcConfigurer {

    @Bean
    public AdminAuthInterceptor adminAuthInterceptor() {
        return new AdminAuthInterceptor();
    }

    @Bean
    public ApiValidateInterceptor apiValidateInterceptor() {
        return new ApiValidateInterceptor();
    }

    @Bean
    public BanCheckInterceptor banCheckInterceptor() {
        return new BanCheckInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 第1个：请求解析 + 管理员鉴权
        registry.addInterceptor(adminAuthInterceptor())
                .addPathPatterns("/soft/**", "/web/**")
                .excludePathPatterns("/test/**")
                .order(1);
        // 第2个：API 合法性验证（软件/版本/解密/用户/签名）
        registry.addInterceptor(apiValidateInterceptor())
                .addPathPatterns("/soft/**", "/web/**")
                .excludePathPatterns("/test/**")
                .order(2);
        // 第3个：封禁检查
        registry.addInterceptor(banCheckInterceptor())
                .addPathPatterns("/soft/**", "/web/**")
                .excludePathPatterns("/test/**")
                .order(3);
    }
}
