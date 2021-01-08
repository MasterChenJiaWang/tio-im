/**
 *
 */
package com.daren.chen.im.core.protocol;

import java.nio.ByteBuffer;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;

/**
 * 协议校验接口
 * @author WChao
 *
 */
public interface IProtocol {
	/**
	 * 协议名称
	 * @return 如:http、ws、tcp等
	 */
	String name();

	/**
	 * 根据buffer判断是否属于指定协议
	 * @param buffer
	 * @param imChannelContext
	 * @return
	 * @throws ImException
	 */
	boolean isProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException;

	/**
	 * 根据imPacket判断是否属于指定协议
	 * @param imPacket
	 * @param imChannelContext
	 * @return
	 * @throws ImException
	 */
	boolean isProtocol(ImPacket imPacket, ImChannelContext imChannelContext)throws ImException;
}
