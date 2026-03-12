package com.luo.share.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String serverUrl;
    private String returnUrl;
    private String notifyUrl;
    private String signType;
    private String charset;
    private String format;

    @Bean
    public AlipayClient alipayClient() {
        // 实例化支付宝客户端
        return new DefaultAlipayClient(
                serverUrl,
                appId,
                privateKey,
                format,
                charset,
                alipayPublicKey,
                signType
        );
    }
}
