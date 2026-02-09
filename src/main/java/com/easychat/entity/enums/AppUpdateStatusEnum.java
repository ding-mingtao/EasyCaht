package com.easychat.entity.enums;

/**
 * @ClassName: AppUpdateStatusEnum
 * @Description: App更新状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/5/31 22:18
 **/
public enum AppUpdateStatusEnum {
    INIT(0, "未发布"), GRAYSCALE(1, "灰度发布"), ALL(2, "全网发布");

    private Integer status;
    private String description;

    AppUpdateStatusEnum(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() { return status; }
    public String getDescription() { return description; }

    public static AppUpdateStatusEnum getByStatus(Integer status) {
        for (AppUpdateStatusEnum at : AppUpdateStatusEnum.values()) {
            if (at.status.equals(status)) {
                return at;
            }
        }
        return null;
    }
}
