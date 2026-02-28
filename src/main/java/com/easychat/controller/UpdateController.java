package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.AppUpdateFileTypeEnum;
import com.easychat.entity.po.AppUpdate;
import com.easychat.entity.query.AppUpdateQuery;
import com.easychat.entity.vo.AppUpdateVO;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.AppUpdateService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: updateController
 * @Description: 检测更新
 * @Author: 丁铭涛
 * @DateTime: 2025/6/1 10:35
 **/
@RestController("updateController")
@RequestMapping("/update")
public class UpdateController extends ABaseController {

    @Resource
    private AppUpdateService appUpdateService;
    @Resource
    private Appconfig appconfig;


    @RequestMapping("/checkVersion")
    @GlobalInterceptor
    public ResponseVO checkVersion(String appVersion,String uid) {
        if (StringTools.isEmpty(appVersion)) {
            return getSuccessResponseVO(null);
        }
        AppUpdate appUpdate = appUpdateService.getLatestUpdate(appVersion,uid);
        if (appUpdate == null) {
            return getSuccessResponseVO(null);
        }
        AppUpdateVO updateVO = CopyTools.copy(appUpdate, AppUpdateVO.class);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())) {
            File file = new File(appconfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + appUpdate.getId()+Constants.APP_EXE_SUFFIX);
            updateVO.setSize(file.length());
        }else {
            updateVO.setSize(0L);
        }
        updateVO.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));
        String fileName = Constants.APP_NAME + appUpdate.getVersion()+Constants.APP_EXE_SUFFIX;
        updateVO.setFileName(fileName);
        return getSuccessResponseVO(updateVO);
    }


}
