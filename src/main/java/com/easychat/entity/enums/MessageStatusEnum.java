package com.easychat.entity.enums;

/**
 * @ClassName: MessageStatusEnum
 * @Description: 消息状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/6/4 10:28
 **/
public enum MessageStatusEnum {
    SENDING(0, "发送中"),
    SENDED(1, "已发送");

    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum item : MessageStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
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

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
