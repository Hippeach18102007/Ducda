package com.example.ASM1_DUCDATH04243_SD20202.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Cấu hình basename cho các tệp ngôn ngữ (i18n/messages_vi.properties, i18n/messages_en.properties, ...)
        messageSource.setBasename("classpath:i18n/messages");
        // RẤT QUAN TRỌNG: Đảm bảo mã hóa UTF-8 để hiển thị tiếng Việt chính xác
        messageSource.setDefaultEncoding("UTF-8");
        // Tùy chọn: Tải lại tin nhắn sau 1 giây (cho môi trường phát triển)
        messageSource.setCacheSeconds(1);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        // Lưu trữ thông tin ngôn ngữ vào Session
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // Đặt ngôn ngữ mặc định là Tiếng Việt (vi)
        slr.setDefaultLocale(new Locale("vi"));
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        // Đặt tên tham số trên URL để chuyển đổi ngôn ngữ (e.g., /students?lang=en)
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Đăng ký interceptor để theo dõi tham số "lang" trên mọi request
        registry.addInterceptor(localeChangeInterceptor());
    }
}
