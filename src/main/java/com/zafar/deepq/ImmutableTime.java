package com.zafar.deepq;

public final class ImmutableTime {
	private static long time=0;
	public ImmutableTime(long time){
		this.time=time;
	}
	public static long getTime(){
		return time;
	}
}
