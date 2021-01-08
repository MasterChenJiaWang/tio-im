package com.daren.chen.im.server.command;

import java.util.ArrayList;
import java.util.List;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.server.processor.MultiProtocolCmdProcessor;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author: WChao 创建时间: 2017年9月11日 下午2:07:44
 */
public abstract class AbstractCmdHandler implements CmdHandler, ImConst {
    /**
     * 单协议业务处理器
     */
    private SingleProtocolCmdProcessor singleProcessor;
    /**
     * 多协议业务处理器
     */
    private List<MultiProtocolCmdProcessor> multiProcessors = new ArrayList<>();

    public AbstractCmdHandler() {};

    public SingleProtocolCmdProcessor getSingleProcessor() {
        return singleProcessor;
    }

    public AbstractCmdHandler setSingleProcessor(SingleProtocolCmdProcessor singleProcessor) {
        this.singleProcessor = singleProcessor;
        return this;
    }

    public <T> T getSingleProcessor(Class<T> clazz) {
        return (T)singleProcessor;
    }

    public AbstractCmdHandler addMultiProtocolProcessor(MultiProtocolCmdProcessor processor) {
        this.multiProcessors.add(processor);
        return this;
    }

    /**
     * 根据当前通道所属协议获取cmd业务处理器
     *
     * @param imChannelContext
     * @return
     */
    public <T> T getMultiProcessor(ImChannelContext imChannelContext, Class<T> clazz) {
        T multiCmdProcessor = null;
        for (MultiProtocolCmdProcessor multiProcessor : multiProcessors) {
            if (multiProcessor.isProtocol(imChannelContext)) {
                multiCmdProcessor = (T)multiProcessor;
            }
        }
        return multiCmdProcessor;
    }

}
