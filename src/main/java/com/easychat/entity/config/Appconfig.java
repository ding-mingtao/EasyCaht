package com.easychat.entity.config;


import com.easychat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName: Appconfig
 * @Description: 配置文件
 * @Author: 丁铭涛
 * @DateTime: 2025/4/4 10:34
 **/
@Component("appConfig")
public class Appconfig {

    /**
     * websocket 端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;
    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.emails:}")
    private String adminEmails;

    /**
     * DeepSeek API配置
     */
    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        if (StringTools.isEmpty(projectFolder)) {
            return "/";
        }

        if (!projectFolder.endsWith("/")) {
            projectFolder += "/";
        }

        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public String getDeepseekApiKey() {
        return deepseekApiKey;
    }

    public String getDeepseekApiUrl() {
        return deepseekApiUrl;
    }

    public String getDeepseekModel() {
        return deepseekModel;
    }
}
