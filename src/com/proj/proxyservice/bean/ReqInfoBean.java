package com.proj.proxyservice.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proj.proxyservice.Constant;
import com.proj.proxyservice.exception.BaseException;
import com.proj.proxyservice.exception.ErrorCode;
import com.proj.proxyservice.util.HttpClientUtil;

public class ReqInfoBean {
	private final static Logger _log = LoggerFactory.getLogger(ReqInfoBean.class);

	/**
	 * http请求参数的类型
	 */
	public static final String HTTP_REQ_TYPE = "http_req_type";
	/**
	 * http_rule_req_url http请求头中存放的真实的请求路径
	 */
	public static final String HTTP_PROXY_URL = "proxy_Url";
	/**
	 * http的读取超时时间
	 */
	public static final String HTTP_READ_TIME = "http_read_time";
	/**
	 * http的连接超时时间
	 */
	public static final String HTTP_CONNECT_TIME = "http_connect_time";
	/*
	 * 请求参数长度
	 */
	private String contentLength;
	/*
	 * 代理清楚的url路径
	 */
	private String proxyUrl;
	/*
	 * 请求此工程时的路径
	 */
	private String reqUrl;
	/*
	 * 请求的类型
	 */
	private HttpType httpReqType;
	/*
	 * 请求信息
	 */
	private String postInfo;
	/*
	 * 请求的读取超时时间
	 */
	private Integer readTime;
	/*
	 * 请求的连接超时时间
	 */
	private Integer connectTime;
	/*
	 * 消息类型
	 */
	private String contentType;

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

	public HttpType getHttpReqType() {
		return httpReqType;
	}

	public void setHttpReqType(HttpType httpReqType) {
		this.httpReqType = httpReqType;
	}

	public String getPostInfo() {
		return postInfo;
	}

	public void setPostInfo(String postInfo) {
		this.postInfo = postInfo;
	}

	public Integer getReadTime() {
		return readTime;
	}

	public void setReadTime(Integer readTime) {
		this.readTime = readTime;
	}

	public Integer getConnectTime() {
		return connectTime;
	}

	public void setConnectTime(Integer connectTime) {
		this.connectTime = connectTime;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * http的请求类型
	 * 
	 * @author xuxiaojia
	 */
	public enum HttpType {
		/**/
		get {
			@Override
			public String getTypeName() {
				return "get";
			}

			@Override
			public HttpResponse send(HttpClient httpClient, ReqInfoBean reqInfo) throws Exception {
				bulidHeader(httpClient, reqInfo);
				_log.info("以get方式请求,url为:{}",reqInfo.getProxyUrl());
				HttpGet httpGet = new HttpGet(reqInfo.getProxyUrl());
				RequestConfig config = RequestConfig.custom().setConnectTimeout(reqInfo.getConnectTime())
						.setSocketTimeout(reqInfo.getReadTime()).build();
				httpGet.setConfig(config);
				return httpClient.execute(httpGet);
			}
		},
		postBodyData {
			@Override
			public String getTypeName() {
				return "postBodyData";
			}

			@Override
			public HttpResponse send(HttpClient httpClient, ReqInfoBean reqInfo) throws Exception {
				bulidHeader(httpClient, reqInfo);
				HttpPost httpPost = null;
				httpPost = new HttpPost(reqInfo.getProxyUrl());
				_log.info("以postBodyData方式请求,url为:{}  请求信息为:{}",reqInfo.getProxyUrl(),reqInfo.getPostInfo());
				StringEntity resEntity = new StringEntity(reqInfo.getPostInfo(), Consts.UTF_8);
				httpPost.setEntity(resEntity);
				RequestConfig config = RequestConfig.custom().setConnectTimeout(reqInfo.getConnectTime())
						.setSocketTimeout(reqInfo.getReadTime()).build();
				httpPost.setConfig(config);
				return httpClient.execute(httpPost);
			}

		},
		postParams {
			@Override
			public String getTypeName() {
				return "postParams";
			}

			@Override
			public HttpResponse send(HttpClient httpClient, ReqInfoBean reqInfo) throws Exception {
				bulidHeader(httpClient, reqInfo);
				HttpPost httpPost = null;
				httpPost = new HttpPost(reqInfo.getProxyUrl());
				List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
				Map<String, String> reqMap = HttpClientUtil.packageReqMap(reqInfo.getPostInfo());
				for (String key : reqMap.keySet()) {
					pairList.add(new BasicNameValuePair(key, reqMap.get(key)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(pairList, Constant.UTF8));
				RequestConfig config = RequestConfig.custom().setConnectTimeout(reqInfo.getConnectTime())
						.setSocketTimeout(reqInfo.getReadTime()).build();
				httpPost.setConfig(config);
				_log.info("以postParams方式请求,url为:{}  请求信息为:{}",reqInfo.getProxyUrl(),reqInfo.getPostInfo());
				return httpClient.execute(httpPost);
			}
		};
		public abstract String getTypeName();

		/**
		 * 设置请求头 请求响应时间等信息
		 * 
		 * @param httpClient
		 * @param reqInfo
		 */
		private static void bulidHeader(HttpClient httpClient, ReqInfoBean reqInfo) {
			List<Header> headers = new ArrayList<Header>();
			Header header = null;
			if (StringUtils.isNotEmpty(reqInfo.getContentType())) {
				header = new BasicHeader(Constant.CONTENT_TYPE, reqInfo.getContentType());
				headers.add(header);
			}
		}

		/**
		 * 获取http请求的类型
		 * 
		 * @param httpType
		 * @return
		 * @throws BaseException
		 */
		public static HttpType getHttpType(String httpType) throws BaseException {
			if (get.getTypeName().equals(httpType)) {
				return get;
			} else if (postBodyData.getTypeName().equals(httpType)) {
				return postBodyData;
			} else if (postParams.getTypeName().equals(httpType)) {
				return postParams;
			}
			throw new BaseException(ErrorCode.NO_HTTP_TYPE, "没有找到对应的http请求类型：" + httpType);
		}

		/**
		 * 通过各种http类型发送信息
		 * 
		 * @param httpClient
		 */
		public abstract HttpResponse send(HttpClient httpClient, ReqInfoBean reqInfo) throws Exception;
	}
}
