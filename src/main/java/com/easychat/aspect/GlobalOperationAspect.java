package com.easychat.aspect;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.exception.BusinessException;
import com.easychat.redis.RedisUtils;
import com.easychat.utils.StringTools;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @ClassName: GlobalOperationAspect
 * @Description: 全局切面
 * @Author: 丁铭涛
 * @DateTime: 2025/4/12 13:37
 **/
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;
    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    @Before("@annotation(com.easychat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint point) throws BusinessException {
        try {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (interceptor == null) {
                return;
            }
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }
        } catch (BusinessException e) {
            logger.error("全局拦截异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("全局拦截异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            logger.error("全局拦截异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }


    }

    private void checkLogin(Boolean checkAdmin) throws BusinessException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if(StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (checkAdmin && !tokenUserInfoDto.getAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
