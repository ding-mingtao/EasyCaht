package com.easychat.entity.enums;

import com.easychat.utils.StringTools;

/**
 * @ClassName: UserContactApplyStatusEnum
 * @Description: 用户联系人申请状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/5/15 16:00
 **/
public enum UserContactApplyStatusEnum {
    INIT(0, "待处理"),
    PASS(1, "已同意"),
    REJECT(2, "已拒绝"),
    BLACKLIST(3, "已拉黑");

    private Integer status;
    private String desc;

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public Integer getStatus() {return status;}
    public String getDesc() {return desc;}

    public static UserContactApplyStatusEnum getByStatus(String status) {
        try{
            if(StringTools.isEmpty(status)){
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(status.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        for(UserContactApplyStatusEnum item : UserContactApplyStatusEnum.values()) {
            if(item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
