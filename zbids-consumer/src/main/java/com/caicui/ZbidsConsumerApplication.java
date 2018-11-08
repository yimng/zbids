package com.caicui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.Charset;

@SpringBootApplication
public class ZbidsConsumerApplication extends SpringBootServletInitializer {

    @Bean
    public HttpMessageConverter<String> createStringHttpMessageConverter() {

        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));

        return converter;
    }

    @Bean
    public HttpMessageConverter createMappingJackson2HttpMessageConverter () {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setPrettyPrint(true);
        return converter;
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZbidsConsumerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZbidsConsumerApplication.class, args);
    }
}
