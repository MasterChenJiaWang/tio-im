/**
 *
 */
package com.daren.chen.im.server.protocol.http;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.cache.guava.GuavaCache;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.http.HttpConvertPacket;
import com.daren.chen.im.core.http.HttpProtocol;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.http.HttpRequestDecoder;
import com.daren.chen.im.core.http.HttpResponse;
import com.daren.chen.im.core.http.HttpResponseEncoder;
import com.daren.chen.im.core.http.handler.IHttpRequestHandler;
import com.daren.chen.im.core.protocol.AbstractProtocol;
import com.daren.chen.im.core.session.id.impl.UUIDSessionIdGenerator;
import com.daren.chen.im.server.JimServer;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.AbstractProtocolHandler;
import com.daren.chen.im.server.protocol.http.mvc.Routes;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年8月3日 下午3:07:54
 */
public class HttpProtocolHandler extends AbstractProtocolHandler {

    private Logger log = LoggerFactory.getLogger(HttpProtocolHandler.class);

    private HttpConfig httpConfig;

    private IHttpRequestHandler httpRequestHandler;

    public HttpProtocolHandler() {
        this(null, new HttpProtocol(new HttpConvertPacket()));
    };

    public HttpProtocolHandler(HttpConfig httpConfig, AbstractProtocol protocol) {
        super(protocol);
        this.httpConfig = httpConfig;
    }

    @Override
    public void init(ImServerConfig imServerConfig) throws ImException {
        this.httpConfig = imServerConfig.getHttpConfig();
        if (Objects.isNull(httpConfig.getSessionStore())) {
            GuavaCache guavaCache =
                GuavaCache.register(httpConfig.getSessionCacheName(), null, httpConfig.getSessionTimeout());
            httpConfig.setSessionStore(guavaCache);
        }
        if (Objects.isNull(httpConfig.getSessionIdGenerator())) {
            httpConfig.setSessionIdGenerator(UUIDSessionIdGenerator.INSTANCE);
        }
        if (Objects.isNull(httpConfig.getScanPackages())) {
            // JIM MVC需要扫描的根目录包
            String[] scanPackages = new String[] {JimServer.class.getPackage().getName()};
            httpConfig.setScanPackages(scanPackages);
        } else {
            String[] scanPackages = new String[httpConfig.getScanPackages().length + 1];
            scanPackages[0] = JimServer.class.getPackage().getName();
            System.arraycopy(httpConfig.getScanPackages(), 0, scanPackages, 1, httpConfig.getScanPackages().length);
            httpConfig.setScanPackages(scanPackages);
        }
        Routes routes = new Routes(httpConfig.getScanPackages());
        httpRequestHandler = new DefaultHttpRequestHandler(httpConfig, routes);
        httpConfig.setHttpRequestHandler(httpRequestHandler);
        log.info("Http Protocol initialized");
    }

    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        HttpResponse httpResponsePacket = (HttpResponse)imPacket;
        ByteBuffer byteBuffer = HttpResponseEncoder.encode(httpResponsePacket, imChannelContext, false);
        return byteBuffer;
    }

    @Override
    public void handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {
        HttpRequest httpRequestPacket = (HttpRequest)imPacket;
        HttpResponse httpResponsePacket =
            httpRequestHandler.handler(httpRequestPacket, httpRequestPacket.getRequestLine());
        JimServerAPI.send(imChannelContext, httpResponsePacket);
    }

    @Override
    public ImPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
        ImChannelContext imChannelContext) throws ImDecodeException {
        HttpRequest request = HttpRequestDecoder.decode(buffer, imChannelContext, true);
        imChannelContext.setAttribute(ImConst.HTTP_REQUEST, request);
        return request;
    }

    public IHttpRequestHandler getHttpRequestHandler() {
        return httpRequestHandler;
    }

    public void setHttpRequestHandler(IHttpRequestHandler httpRequestHandler) {
        this.httpRequestHandler = httpRequestHandler;
    }

    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    public void setHttpConfig(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

}
