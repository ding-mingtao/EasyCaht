package com.easychat.entity.enums;

/**
 * @ClassName: BeautyAccountStatusEnum
 * @Description: 靓号状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/4/3 21:43
 **/
public enum BeautyAccountStatusEnum {
    NO_USE(0,"未使用"),
    USEED(1,"已使用");

    private Integer status;
    private String desc;

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    private static BeautyAccountStatusEnum getByStatus(Integer status) {
        for(BeautyAccountStatusEnum item: BeautyAccountStatusEnum.values()) {
            if(item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
