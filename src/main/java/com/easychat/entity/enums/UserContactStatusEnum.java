package com.easychat.entity.enums;

import com.easychat.utils.StringTools;

/**
 * @ClassName: UserContactStatusEnum
 * @Description: 用户联系人状态枚举
 * @Author: 丁铭涛
 * @DateTime: 2025/4/12 15:14
 **/
public enum UserContactStatusEnum {
    NOT_FRIEND(0,"非好友"),
    FRIEND(1,"好友"),
    DEL(2,"已删除好友"),
    DEL_BE(3,"被好友删除"),
   BLACKLIST(4,"已拉黑好友"),
    BLACKLIST_BE(5,"被好友拉黑"),
    BLACKLIST_BE_FIRST(6,"首次被好友拉黑");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status,String desc){
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByStatus(String status){
        try{
            if (StringTools.isEmpty(status)){
                return null;
            }
            return UserContactStatusEnum.valueOf(status.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }
    public static UserContactStatusEnum getByStatus(Integer status){
        for (UserContactStatusEnum item : UserContactStatusEnum.values()){
            if(item.getStatus().equals(status)){
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

}
