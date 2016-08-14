package com.zafar.deepq.domain;

import com.zafar.deepq.util.Constants;

public class Response<A> {
	
	private String status=Constants.STATUS_OK;
	
	private A data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public A getData() {
		return data;
	}

	public void setData(A data) {
		this.data = data;
	}
	
	public Response(A data){
		this.data=data;
	}
	
	public Response(A data, String st){
		this.data=data;
		status=st;
	}
	

}
