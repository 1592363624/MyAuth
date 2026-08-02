package cn.daenx.myauth.base.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 接口文档配置
 * 访问地址：/myauth/swagger-ui.html
 *
 * @author DaenMax
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyAuth 授权中心 API")
                        .description("MyAuth 网络验证系统接口文档")
                        .version("v1.1.5")
                        .contact(new Contact().name("DaenMax")));
    }
}
