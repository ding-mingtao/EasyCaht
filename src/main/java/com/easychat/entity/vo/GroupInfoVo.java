package com.easychat.entity.vo;

import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserContact;

import java.util.List;

/**
 * @ClassName: GroupInfoVo
 * @Description: 群组信息
 * @Author: 丁铭涛
 * @DateTime: 2025/4/13 12:53
 **/
public class GroupInfoVo {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
    public List<UserContact> getUserContactList() {
        return userContactList;
    }
    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }
    public GroupInfo getGroupInfo() {
        return groupInfo;
    }
    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }
}
