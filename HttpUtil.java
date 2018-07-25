package com.umpay.proxyservice.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.umpay.log.util.PropertiesUtil;

public class HttpUtil {

	private final static Logger _log = LoggerFactory.getLogger("HttpUtil");

	private final static String REQUEST_METHOD = "POST";
	private final static String REQUEST_CONTENT_TYPE = "Content-type";
	private final static String REQUEST_CONTENT_TYPE_VALUE = "text/plain";
	// private final static String REQUEST_CONTENT_TYPE_VALUE =
	// "application/json";
	private final static String REQUEST_CHARSET = "Accept-Charset";
	private final static String REQUEST_CHARSET_VALUE = "UTF-8";
	private final static String REQUEST_CONTENT_LENGTH = "Content-Length";

	private final static int READ_TIME_OUT = 3 * 1000;
	private final static int CONNECT_TIME_OUT = 1 * 1000;
	/*
	 * 发送请求的错误次数
	 */
	private final static AtomicInteger errorTimes = new AtomicInteger(0);
	/*
	 * 使用代理的次数
	 */
	private final static AtomicInteger useProxyTimes = new AtomicInteger(0);
	/*
	 * 最大的使用代理的次数
	 */
	private final static AtomicBoolean useProxy = new AtomicBoolean(false);

	public static String post(String u, String str) throws Exception {
		return HttpUtil.post(u, str, READ_TIME_OUT, CONNECT_TIME_OUT);
	}

	public static String post(String u, String str, int readTimeOut, int connectTimeOut) throws Exception {
		URL url = null;
		HttpURLConnection huc = null;
		InputStream is = null;
		BufferedReader br = null;
		DataOutputStream printout = null;
		StringBuffer sb = new StringBuffer();
		boolean backUrlFlag = false;
		try {
			if (CastUtil.string2boolean(PropertiesUtil.getInstance("config.properties").getConfigItem("proxyCanUse"),
					true) && useProxy.get()) {
				int incrementAndGet = useProxyTimes.incrementAndGet();
				int maxUseProxyTimes = CastUtil.string2int(PropertiesUtil.getInstance("config.properties").getConfigItem("maxUseProxyTimes"), 40);
				// 在使用代理的情况下调用一定次数后重新访问原访问路径，如果请求成功则不再使用代理
				if (incrementAndGet > maxUseProxyTimes) {
					_log.info("切回到原访问路径，看是否恢复");
					url = new URL(u);
					huc = (HttpURLConnection) url.openConnection();
					backUrlFlag = true;
					useProxyTimes.set(0);
				} else {
					_log.info("使用跳板已达：{}次，大于{}次后恢复访问原路径",incrementAndGet,maxUseProxyTimes);
					url = new URL(PropertiesUtil.getInstance("config.properties").getConfigItem("proxyUrl"));
//					url = new URL("http://localhost:9018");
					huc = (HttpURLConnection) url.openConnection();
					huc.setRequestProperty("http_req_type", "postBodyData");
					huc.setRequestProperty("proxy_Url", u);
					huc.setRequestProperty("http_read_time", String.valueOf(readTimeOut));
					huc.setRequestProperty("http_connect_time", String.valueOf(connectTimeOut));
				}
			} else {
				url = new URL(u);
				huc = (HttpURLConnection) url.openConnection();
			}
			huc.setRequestMethod(REQUEST_METHOD);
			huc.setRequestProperty(REQUEST_CONTENT_TYPE, REQUEST_CONTENT_TYPE_VALUE);
			huc.setRequestProperty(REQUEST_CHARSET, REQUEST_CHARSET_VALUE);
			huc.setRequestProperty(REQUEST_CONTENT_LENGTH, String.valueOf(str.getBytes().length));
			huc.setDoOutput(true);
			huc.setReadTimeout(readTimeOut);
			huc.setConnectTimeout(connectTimeOut);
			printout = new DataOutputStream(huc.getOutputStream());
			printout.write(str.getBytes());
			printout.flush();
			if (printout != null) {
				try {
					printout.close();
				} catch (IOException e) {
					_log.error("http close printout error:", e);
				}
			}
			String line;
			is = huc.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			if (backUrlFlag) {
				// 如果使用原请求路径成功则不再使用代理并清空代理使用次数，不清空原错误次数，只减一
				useProxy.set(false);
				errorTimes.decrementAndGet();
			}
			int i = errorTimes.get();
			if (i > 0 && !useProxy.get()) {
				// 在未使用代理并且错误次数大于0的情况下 请求成功则减少请求次数
				errorTimes.decrementAndGet();
			}
		} catch (Exception e) {
			if (!useProxy.get()) {
				int incrementAndGet = errorTimes.incrementAndGet();
				int maxErrorTimes = CastUtil.string2int(PropertiesUtil.getInstance("config.properties").getConfigItem("maxErrorTimes"), 20);
				_log.info("异常请求阀值为:{},启用跳板阀值为:{}",incrementAndGet,maxErrorTimes);
				if (incrementAndGet >= maxErrorTimes) {
					_log.info("启用跳板，访问的跳板地址为：{}",PropertiesUtil.getInstance("config.properties").getConfigItem("proxyUrl"));
					useProxy.set(true);
				}
			}
			_log.error("http input stream error:", e);
			return null;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (is != null) {
					is.close();
				}
				if (huc != null) {
					huc.disconnect();
				}
			} catch (Exception e) {
				_log.error("http close input stream error:", e);
				return null;
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		post("http://10.10.111.11", "asefsafsafe");
	}

	public static String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();

			connection.connect();
			System.out.println(connection.getHeaderFields());
			Map<String, List<String>> map = connection.getHeaderFields();
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	public static String replaceDalTag(String s) throws Exception {
		int st = s.indexOf("[start]");
		int et = s.indexOf("[end]");
		return s.substring(st + 7, et);
	}

	public static String sendGet(String url, int readTimeOut, int connectTimeOut) throws Exception {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setReadTimeout(readTimeOut);
			connection.setConnectTimeout(connectTimeOut);
			connection.connect();
			System.out.println(connection.getHeaderFields());
			Map<String, List<String>> map = connection.getHeaderFields();
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

}