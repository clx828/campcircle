package com.caden.campcircle.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
@Slf4j
public class WxMaConfiguration {

    private String appId;
    private String appSecret;

    @PostConstruct
    public void validate() {
        if (StringUtils.isBlank(appId)) {
            throw new IllegalStateException("微信小程序 appid 配置不能为空");
        }
        if (StringUtils.isBlank(appSecret)) {
            throw new IllegalStateException("微信小程序 secret 配置不能为空");
        }
        log.info("微信小程序配置加载成功，appid: {}", appId);
    }

    @Bean
    public WxMaService getWxMaService() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(appSecret);

        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }
}
