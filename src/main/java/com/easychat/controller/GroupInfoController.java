package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.GroupStatusEnum;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.GroupInfoVo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.exception.BusinessException;
import com.easychat.service.GroupInfoService;

import com.easychat.service.UserContactService;
import com.easychat.service.impl.GroupInfoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * @Description:Controller
 * @author:丁铭涛
 * @date:2025/04/11
 */
@RestController("groupInfoController")
@RequestMapping("/group")
@Validated
public class GroupInfoController extends ABaseController {
    private static final Logger logger = LoggerFactory.getLogger(GroupInfoController.class);

    @Resource
    private GroupInfoService groupInfoService;
    @Resource
    private UserContactService userContactService;

    @RequestMapping("/saveGroup")
    @GlobalInterceptor
    public ResponseVO saveGroup(HttpServletRequest request, String groupId,
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                @RequestPart(name = "avatarFile", required = false) MultipartFile avatarFile,
                                @RequestPart(name = "avatarCover", required = false) MultipartFile avatarCover) throws BusinessException, IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        logger.info("groupInfo: {}", groupInfo);
        logger.info("avatarFile is null? {}", (avatarFile == null));
        logger.info("avatarCover is null? {}", (avatarCover == null));
        this.groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMyGroup")
    @GlobalInterceptor
    public ResponseVO loadMyGroup(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setStatus(GroupStatusEnum.NORMAL.getStatus()); // 只查正常状态的群
        groupInfoQuery.setOrderBy("create_time desc");
        List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVO(groupInfoList);
    }


    @RequestMapping("/getGroupInfo")
    @GlobalInterceptor
    public ResponseVO getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) throws BusinessException {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        Integer menberConut = this.userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(menberConut);
        return getSuccessResponseVO(groupInfo);
    }


    private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) throws BusinessException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
        if (null == userContact || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("你不在该群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (null == groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散");
        }
        return groupInfo;
    }

    @RequestMapping("/getGroupInfo4Chat")
    @GlobalInterceptor
    public ResponseVO getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId){
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setQueryUserInfo(true);
        userContactQuery.setOrderBy("create_time asc");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupInfo(groupInfo);
        groupInfoVo.setUserContactList(userContactList);
        return getSuccessResponseVO(groupInfoVo);
    }

    @RequestMapping("/addOrRemoveGroupUser")
    @GlobalInterceptor
    public ResponseVO addOrRemoveGroupUser(HttpServletRequest request,
                                           @NotEmpty String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType){
       TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
       groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto, groupId, selectContacts, opType);
       return getSuccessResponseVO(null);
    }

    @RequestMapping("/leaveGroup")
    @GlobalInterceptor
    public ResponseVO leaveGroup(HttpServletRequest request,
                                           @NotEmpty String groupId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor
    public ResponseVO dissolutionGroup(HttpServletRequest request,
                                 @NotEmpty String groupId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(), groupId);
        return getSuccessResponseVO(null);
    }

}