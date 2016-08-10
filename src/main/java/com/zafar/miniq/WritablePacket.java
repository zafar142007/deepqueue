package com.zafar.miniq;

import java.io.Serializable;

public class WritablePacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588841303799478392L;
	private String payload;
	private String uuid;
	
	public WritablePacket(String p, String t){
		payload=p;
		uuid=t;
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
