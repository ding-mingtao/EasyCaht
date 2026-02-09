package com.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.UserContactMapper;
import com.easychat.mappers.UserInfoBeautyMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ChatSessionUserService;
import com.easychat.service.UserContactService;
import com.easychat.utils.CopyTools;
import com.easychat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.query.SimplePage;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.service.UserInfoService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoQuery> userInfoBeautyMapper;
    @Resource
    private Appconfig appconfig;
    @Resource
    private RedisComponent redisComponent;
    @Autowired
    private UserContactMapper userContactMapper;
    @Resource
    private UserContactService userContactService;
    @Resource
    private ChatSessionUserService  chatSessionUserService;
    @Resource
    private MessageHandler messageHandler;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password) {
        Map<String, Object> result = new HashMap<>();
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已经存在");
        }
        String userId = StringTools.getUserId();
        UserInfoBeauty beautyAccount = this.userInfoBeautyMapper.selectByEmail(email);
        Boolean useBeautyAccount = null != beautyAccount && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
        if (useBeautyAccount) {
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
        }
        Date curDate = new Date();
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeMd5(password));
        userInfo.setEmail(email);
        userInfo.setCreateTime(curDate);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setLastOffTime(curDate.getTime());
        userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
        this.userInfoMapper.insert(userInfo);

        if (useBeautyAccount) {
            UserInfoBeauty updateBeauty = new UserInfoBeauty();
            updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
            this.userInfoBeautyMapper.updateById(updateBeauty, beautyAccount.getId());
        }

        userContactService.addContact4Robot(userId);
    }

    @Override
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        if (UserStatusEnum.DISABLE.equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        //查询联系人
        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(userInfo.getUserId());
        contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> contactList = userContactMapper.selectList(contactQuery);
        List<String> contactIdList = contactList.stream().map(item -> item.getContactId()).collect(Collectors.toList());
        redisComponent.cleanUserContact(userInfo.getUserId());
        if(!contactIdList.isEmpty()){
            redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);
        }
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (null != lastHeartBeat) {
            throw new BusinessException("此账号已在别处登录，请退出后再登录");
        }

        //保存登录信息到redis中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);
        String token = StringTools.encodeMd5(tokenUserInfoDto.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));
        tokenUserInfoDto.setToken(token);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);


        UserInfoVO userInfoVO = CopyTools.copy(tokenUserInfoDto, UserInfoVO.class);
        userInfoVO.setToken(tokenUserInfoDto.getToken());
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return userInfoVO;
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());

        String adminEmails = appconfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
            tokenUserInfoDto.setAdmin(true);
        } else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (avatarFile != null) {
            String baseFoder = appconfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFoder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId() + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        UserInfo dbInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        String contactNameUpdate = null;
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        if (contactNameUpdate == null){
            return;
        }
        //更新token中的昵称信息
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(contactNameUpdate);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        chatSessionUserService.updateRedundanceInfo(contactNameUpdate, userInfo.getUserId());
    }

    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum userStatusEnum = UserStatusEnum.getByStatus(status);
        if (userStatusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(userStatusEnum.getStatus());
        this.userInfoMapper.updateByUserId(userInfo, userId);
    }

    @Override
    public void forceOffLine(String userId) {
        MessageSendDto sendDto = new MessageSendDto();
        sendDto.setContactType(UserContactTypeEnum.USER.getType());
        sendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        sendDto.setContactId(userId);
        messageHandler.sendMessage(sendDto);
    }
}