package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.UserInfoService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;

/**
 * @ClassName: UserInfoController
 * @Description: 用户信息
 * @Author: 丁铭涛
 * @DateTime: 2025/5/17 15:35
 **/
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private ChannelContextUtils channelContextUtils;

    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVo = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVo);
    }

    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResponseVO saveUserInfo(HttpServletRequest request, UserInfo userInfo,
                                   MultipartFile avatarFile,
                                   MultipartFile avatarCover ) throws BusinessException, IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);
        this.userInfoService.updateUserInfo(userInfo,avatarFile,avatarCover);
        return getUserInfo(request);
    }

    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request,
                                     @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserInfo  userInfo = new UserInfo();
        userInfo.setPassword(StringTools.encodeMd5(password));
        this.userInfoService.updateUserInfoByUserId(userInfo,tokenUserInfoDto.getUserId());
        channelContextUtils.closeConext(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        channelContextUtils.closeConext(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
