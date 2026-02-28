package com.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.ChatSession;
import com.easychat.entity.po.ChatSessionUser;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.query.*;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.ChatSessionMapper;
import com.easychat.mappers.ChatSessionUserMapper;
import com.easychat.mappers.UserContactMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.utils.CopyTools;
import com.easychat.utils.DateUtil;
import com.easychat.utils.JsonUtils;
import com.easychat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.easychat.entity.po.ChatMessage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.mappers.ChatMessageMapper;
import com.easychat.service.ChatMessageService;
import com.easychat.service.DeepSeekService;
import com.easychat.utils.StringTools;
import org.springframework.web.multipart.MultipartFile;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private MessageHandler messageHandler;
    @Resource
    private Appconfig appconfig;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private DeepSeekService deepSeekService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(param);
        PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        // 不是机器人回复，判断好友状态
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            if (!contactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        String sessionId = null;
        String sendUserId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        // 修正：确保在所有情况下都能正确生成sessionId
        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringTools.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringTools.getChatSessionId4Group(contactId);
        }

        // 添加更严格的null检查
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.error("无法生成有效的会话ID，sendUserId: {}, contactId: {}, contactTypeEnum: {}",
                    sendUserId, contactId, contactTypeEnum);
            throw new BusinessException("会话ID生成失败");
        }

        chatMessage.setSessionId(sessionId);
        Long curTime = System.currentTimeMillis();
        chatMessage.setSendTime(curTime);
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, chatMessage.getMessageType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        chatMessage.setStatus(status);

        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        //更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            chatSession.setLastMessage(tokenUserInfoDto.getNickName() + "：" + messageContent);
        }
        chatSession.setLastReceiveTime(curTime);
        int updateResult = chatSessionMapper.updateBySessionId(chatSession, sessionId);
        logger.debug("更新会话结果: {} rows affected, sessionId: {}", updateResult, sessionId);

        //记录消息表
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setContactType(contactTypeEnum.getType());

        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);

        // 接入DeepSeek - 修正机器人回复逻辑
        if (Constants.ROBOT_UID.equals(contactId) && !Constants.ROBOT_UID.equals(sendUserId)) {
            try {
                SysSettingDto sysSettingDto = redisComponent.getSysSetting();
                TokenUserInfoDto robot = new TokenUserInfoDto();
                robot.setUserId(sysSettingDto.getRobotUid());
                robot.setNickName(sysSettingDto.getRobotNickName());
                robot.setToken(tokenUserInfoDto.getToken());

                ChatMessage robotChatMessage = new ChatMessage();
                robotChatMessage.setMessageContent(deepSeekService.getAIResponse(messageContent));
                robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
                robotChatMessage.setContactId(sendUserId); // 回复给原始发送者

                // 为机器人消息重新计算sessionId
                String robotSessionId = StringTools.getChatSessionId4User(
                        new String[]{sysSettingDto.getRobotUid(), sendUserId});

                if (robotSessionId != null && !robotSessionId.trim().isEmpty()) {
                    robotChatMessage.setSessionId(robotSessionId);
                    saveMessage(robotChatMessage, robot);
                } else {
                    logger.error("机器人回复无法生成有效sessionId");
                }
            } catch (Exception e) {
                logger.error("处理机器人回复时发生错误", e);
                // 即使AI回复失败，也不影响正常消息流程
            }
        } else if (!Constants.ROBOT_UID.equals(contactId)) {
            messageHandler.sendMessage(messageSendDto);
        }

        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());

        if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toUpperCase())
                && file.getSize() > sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toUpperCase())
                && file.getSize() > sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringTools.isEmpty(fileSuffix)
                && !ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toUpperCase())
                && !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toUpperCase())
                && file.getSize() > sysSettingDto.getMaxFileSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String fileName = file.getOriginalFilename();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        String month = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appconfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath() + Constants.COVER_IMAGE_SUFFIX));
        } catch (IOException e) {
            logger.error("上传文件失败", e);
            throw new BusinessException("文件上传失败");
        }

        ChatMessage uploadInfo = new ChatMessage();
        uploadInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setMessageId(messageId);
        messageQuery.setStatus(MessageStatusEnum.SENDING.getStatus());
        chatMessageMapper.updateByParam(uploadInfo, messageQuery);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDto);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public File downloadFile(TokenUserInfoDto userInfoDto, Long messageId, Boolean showCover) {
        ChatMessage message = chatMessageMapper.selectByMessageId(messageId);
        String contactId = message.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.USER == contactTypeEnum && !userInfoDto.getUserId().equals(message.getContactId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(userInfoDto.getUserId());
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(userContactQuery);
            if (contactCount == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        String month = DateUtil.format(new Date(message.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appconfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = message.getFileName();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        if (showCover!=null&&showCover){
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileRealName);
        if (!file.exists()) {
            logger.info("文件不存在: {}", messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
            return file;
    }
}