package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.UserInfoBeautyQuery;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UserInfoBeautyService;
import com.easychat.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName: AdminUserInfoBeautyController
 * @Description: 管理员靓号管理
 * @Author: 丁铭涛
 * @DateTime: 2025/5/17 15:35
 **/
@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
public class AdminUserInfoBeautyController extends ABaseController {

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO resultVO = userInfoBeautyService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty beauty) {
        userInfoBeautyService.saveAccount(beauty);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautyAccount(@NotNull Integer id) {
        userInfoBeautyService.deleteUserInfoBeautyById(id);
        return getSuccessResponseVO(null);
    }
}
