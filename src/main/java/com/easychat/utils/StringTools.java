package com.easychat.utils;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @ClassName: StringTools
 * @Description: String工具类
 * @Author: 丁铭涛
 * @DateTime: 2025/4/3 16:40
 **/
public class StringTools {

    public static void checkParam(Object param) throws BusinessException {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw  new BusinessException("检验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String filed){
        if (isEmpty(filed)){
            return filed;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (filed.length() > 1 && Character.isUpperCase(filed.charAt(1))) {
            return filed;
        }
        return filed.substring(0, 1).toUpperCase() + filed.substring(1);
    }

    public static boolean isEmpty(String str){
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String getUserId(){
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }
    public static String getGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }
    public static  String getRandomNumber(Integer count){
        return RandomStringUtils.random(count, false, true);
    }
    public static  String getRandomString(Integer count){
        return RandomStringUtils.random(count, true, true);
    }
    public static final String encodeMd5(String originString){
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }

    public static String cleanHtmlTag(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");
        return content;
    }

    public static final String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds, ""));
    }
    public static final String getChatSessionId4Group(String groupId) {
      return encodeMd5(groupId);
    }

    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)){
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean isNumber(String str) {
        String checkNumber = "^[0-9]+$";
        if (null == str) {
            return false;
        }
        if (!str.matches(checkNumber)) {
            return false;
        }
        return true;
    }
}
