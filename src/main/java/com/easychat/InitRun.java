package com.easychat;

import com.easychat.redis.RedisUtils;
import com.easychat.websocket.netty.NettyWebSocketStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @ClassName: InitRun
 * @Description: 项目启动初始化
 * @Author: 丁铭涛
 * @DateTime: 2025/4/4 13:40
 **/
@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static Logger logger = LoggerFactory.getLogger(InitRun.class);
    @Resource
    private DataSource dataSource;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;
    @Override
    public void run(ApplicationArguments args){
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread(nettyWebSocketStarter).start();
            logger.info("服务启动成功");
        }catch (SQLException e){
            logger.error("数据库配置错误,请检查数据库配置");
        }catch (RedisConnectionFailureException e){
            logger.error("redis配置错误,请检查redis配置");
        }catch (Exception e){
            logger.error("服务启动失败");
        }
    }
}
