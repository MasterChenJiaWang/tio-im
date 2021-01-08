package com.daren.chen.im.server.springboot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daren.chen.im.server.springboot.constant.RabbitConstants;

/**
 * @author chendaren
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(RabbitConstants.EXCHANGE_IM_CHAT);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(RabbitConstants.TOPIC_EXCHANGE_IM_CHAT);
    }

    /**
     * 申明业务服务操作日志的队列和绑定规则
     *
     * @return
     */
    @Bean
    public Queue queueImChat() {
        // 队列持久
        return new Queue(RabbitConstants.QUEUE_IM_CHAT, true);
    }

    @Bean
    public Binding bindingImChatMsg() {
        return BindingBuilder.bind(queueImChat()).to(directExchange()).with(RabbitConstants.ROUTINGKEY_IM_CHAT);
    }

    // ------------- 修改最后消息

    @Bean
    public Queue queueImChatOfLastMsgId() {
        // 队列持久
        return new Queue(RabbitConstants.QUEUE_IM_CHAT_UPDATE_LAST_ID, true);
    }

    @Bean
    public Binding bindingImChatOfLastMsgId() {
        return BindingBuilder.bind(queueImChatOfLastMsgId()).to(directExchange())
            .with(RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_LAST_ID);
    }

    // ------------- 修改用户状态

    @Bean
    public Queue queueImChatOfUpdateUserOnlineStatus() {
        // 队列持久
        return new Queue(RabbitConstants.QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS, true);
    }

    @Bean
    public Binding bindingImChatOfUpdateUserOnlineStatus() {
        return BindingBuilder.bind(queueImChatOfUpdateUserOnlineStatus()).to(topicExchange())
            .with(RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS);
    }

    // @Bean
    // public Queue queueImChatOfUpdateUserOnlineStatus1() {
    // // 队列持久
    // return new Queue(RabbitConstants.QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS_1, true);
    // }
    //
    // @Bean
    // public Queue queueImChatOfUpdateUserOnlineStatus2() {
    // // 队列持久
    // return new Queue(RabbitConstants.QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS_2, true);
    // }
    //
    // @Bean
    // public Binding bindingImChatOfUpdateUserOnlineStatus1() {
    // return BindingBuilder.bind(queueImChatOfUpdateUserOnlineStatus1()).to(topicExchange())
    // .with(RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS);
    // }
    //
    // @Bean
    // public Binding bindingImChatOfUpdateUserOnlineStatus2() {
    // return BindingBuilder.bind(queueImChatOfUpdateUserOnlineStatus2()).to(topicExchange())
    // .with(RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS);
    // }

    // /**
    // * 申明业务服务接口的队列和绑定规则
    // *
    // * @return
    // */
    // @Bean
    // public Queue queueFunction() {
    // // 队列持久
    // return new Queue(RabbitConstants.QUEUE_IM_FUNCTION, true);
    // }
    //
    // @Bean
    // public Binding bindingFunction() {
    // return BindingBuilder.bind(queueFunction()).to(directExchange()).with(RabbitConstants.ROUTINGKEY_IM_FUNCTION);
    // }

    // @Bean
    // public Queue queueImChatUpdateLastId() {
    // // 队列持久
    // return new Queue(RabbitConstants.QUEUE_IM__CHAT_UPDATE_LAST_ID, true);
    // }

    // @Bean
    // public Binding bindingImChatUpdateLastId() {
    // return BindingBuilder.bind(queueImChatUpdateLastId()).to(directExchangeOfImLastMsg())
    // .with(RabbitConstants.ROUTINGKEY_IM_CHAT);
    // }

    // @Bean
    // public Queue queueFunctionOfImChatUpdateLastId() {
    // // 队列持久
    // return new Queue(RabbitConstants.QUEUE_IM_FUNCTION_UPDATE_LAST_ID, true);
    // }

    // @Bean
    // public Binding bindingFunctionOfImChatUpdateLastId() {
    // return BindingBuilder.bind(queueFunctionOfImChatUpdateLastId()).to(directExchangeOfImLastMsg())
    // .with(RabbitConstants.ROUTINGKEY_IM_FUNCTION_UPDATE_LAST_ID);
    // }

}
