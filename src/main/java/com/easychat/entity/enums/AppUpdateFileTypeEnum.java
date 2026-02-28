package com.easychat.entity.enums;

/**
 * @ClassName: AppUpdateFileTypeEnum
 * @Description: App更新文件类型枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/5/31 17:25
 **/
public enum AppUpdateFileTypeEnum {
    LOCAL(0, "本地"), OUTER_LINK(1, "外链");
    private Integer type;
    private String description;

    AppUpdateFileTypeEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    public static AppUpdateFileTypeEnum getByType(Integer type) {
        for (AppUpdateFileTypeEnum at : AppUpdateFileTypeEnum.values()) {
            if(at.type.equals(type)) {
                return at;
            }
        }
        return null;
    }
}
