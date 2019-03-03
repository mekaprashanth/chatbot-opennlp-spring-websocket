package com.example.websocketdemo.session.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.embedded.RedisServer;

/**
 * Runs an embedded Redis instance. This is only necessary since we do not want
 * users to have to setup a Redis instance. In a production environment, this
 * would not be used since a Redis Server would be setup.
 *
 * @author Rob Winch
 */
@Configuration
public class EmbeddedRedisConfiguration {

    @Bean
    public RedisServerBean redisServer() {
        return new RedisServerBean();
    }

    class RedisServerBean implements InitializingBean, DisposableBean {
        private RedisServer redisServer;

        public void afterPropertiesSet() throws Exception {
            redisServer = new RedisServer(6379);
//        	redisServer = RedisServer.newRedisServer(Protocol.DEFAULT_PORT);
            redisServer.start();
        }

        public void destroy() throws Exception {
            if(redisServer != null) {
                redisServer.stop();
            }
        }
    }
}