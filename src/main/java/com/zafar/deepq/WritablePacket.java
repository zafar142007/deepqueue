package com.zafar.deepq;

import java.io.Serializable;

import com.zafar.deepq.util.Constants;

public class WritablePacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588841303799478393L;
	private String payload;
	private String uuid;
	private String status=Constants.STATUS_OK;
	
	public WritablePacket(String p, String t){
		payload=p;
		uuid=t;
	}
	public WritablePacket(String p, String t, String status){
		payload=p;
		uuid=t;
		this.status=status;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
		
	public String toString(){
		return payload+"-"+uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
