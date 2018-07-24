package com.proj.proxyservice.util;
public class TimeCountUtil {
	/*记录起始时间的线程变量*/
	private static final ThreadLocal<Long> startTime=new ThreadLocal<Long>();
	/* 记录请求耗时的线程变量*/
	private static final ThreadLocal<Long> queryTime=new ThreadLocal<Long>();
	
	/**
	 * 设置起始时间
	 */
	public static void setStartTime(){
		startTime.set(System.currentTimeMillis());
	}
	/**
	 * 获取现在时间对应起始时间的耗时
	 * @return
	 */
	public static long getTimeConsuming(){
		if(startTime.get()!=null){
			return System.currentTimeMillis()-startTime.get();
		}else{
			return 0L;
		}
	}
	
	/**
	 * 设置请求耗时
	 * @param time
	 */
	public static void setQueryTime(long time){
		queryTime.set(time);
	}
	
	/**
	 *获取请求耗时 
	 */
	public static long getQueryTime(){
		if(queryTime.get()!=null){
			return queryTime.get();
		}else{
			return 0L;
		}
	}
}
