package com.easychat.entity.enums;

/**
 * @ClassName: UserStatusEnum
 * @Description: 用户状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/4/3 22:13
 **/
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    public Integer status;
    public String desc;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
