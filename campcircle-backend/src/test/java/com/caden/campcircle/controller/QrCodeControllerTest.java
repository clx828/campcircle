package com.caden.campcircle.controller;

import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * 二维码控制器测试
 *
 * @author caden
 */
@SpringBootTest
@AutoConfigureMockMvc
public class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateQrCode() throws Exception {
        // 创建测试请求
        QrCodeGenerateRequest request = new QrCodeGenerateRequest();
        request.setScene("id=123");
        request.setPage("pages/userProfile/userProfile");
        request.setWidth(280);
        request.setAutoColor(false);
        request.setCheckPath(false);

        // 执行请求（注意：这个测试需要有效的微信小程序配置才能成功）
        mockMvc.perform(MockMvcRequestBuilders.post("/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.qrCodeBase64").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.mimeType").value("image/png"));
    }

    @Test
    public void testGenerateQrCodeWithInvalidParams() throws Exception {
        // 创建无效的测试请求（缺少必需的scene参数）
        QrCodeGenerateRequest request = new QrCodeGenerateRequest();
        request.setPage("pages/userProfile/userProfile");
        request.setWidth(280);

        // 执行请求，期望参数验证失败
        mockMvc.perform(MockMvcRequestBuilders.post("/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
