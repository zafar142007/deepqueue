package com.zafar.deepq;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * Has those messages which are yet to be acknowledged	
 * @author zafar
 *
 */
public abstract class UnacknowledgedPackets {
	
	protected ConcurrentHashMap<String ,WritablePacket> unacknowledgedPackets=new ConcurrentHashMap<String,WritablePacket>();

	/**
	 * delete the message from the map
	 * @param messageId
	 */
	public abstract void acknowledgePacket(String messageId);

	/**
	 * push the packet into the map
	 * @param messageId
	 * @param packet
	 */
	public abstract void addToUnacknowldged(WritablePacket packet);
}
