package com.zafar.deepq;

import java.util.concurrent.ConcurrentHashMap;

import com.zafar.deepq.domain.WritablePacket;
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
	 * @param messageId the message id you want to delete
	 */
	public abstract void acknowledgePacket(String messageId);

	/**
	 * push the packet into the map
	 * @param packet the packet you want to set
	 */
	public abstract void addToUnacknowldged(WritablePacket packet);
}
