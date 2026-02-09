package com.easychat.entity.enums;

/**
 * @ClassName: GroupStatusEnum
 * @Description: 群聊状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/4/12 22:13
 **/
public enum GroupStatusEnum {
    NORMAL(1,"正常"),
    DISSOLUTION(0,"解散");
    private Integer status;
    private String desc;
    GroupStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public Integer getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }
    public static GroupStatusEnum getByStatus(Integer status) {
        for (GroupStatusEnum item : GroupStatusEnum.values()) {
            if(item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
