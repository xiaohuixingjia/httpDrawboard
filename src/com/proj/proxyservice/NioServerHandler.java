package com.proj.proxyservice;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.http.MutableHttpRequest;
import org.apache.mina.filter.codec.http.MutableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bs3.inf.IProcessors.HSessionInf;
import com.bs3.nio.mina2.Mina2H4Rpc2;
import com.bs3.nio.mina2.codec.IHttp;
import com.proj.proxyservice.bean.ReqInfoBean;
import com.proj.proxyservice.exception.BaseException;
import com.proj.proxyservice.util.CastUtil;
import com.proj.proxyservice.util.HttpClientUtil;
import com.proj.proxyservice.util.TimeCountUtil;

public class NioServerHandler extends Mina2H4Rpc2 {

	private final static Logger _log = LoggerFactory.getLogger("NioServerHandler");

	@Override
	protected void onServerReadReq(HSessionInf session, Object req) {
		try {
			// 初始化
			TimeCountUtil.setStartTime();
			MutableHttpRequest request = (MutableHttpRequest) req;
			_log.info("解析请求的参数");
			ReqInfoBean reqInfo = getReqInfo(request);
			_log.info("发起请求");
			String respInfo = HttpClientUtil.send(reqInfo);
			this.responseContent(session, respInfo);
			_log.info("共耗时:{}*****返回给商户侧报文:{}", TimeCountUtil.getTimeConsuming(), respInfo);
		} catch (Exception e) {
			_log.error("处理异常，返回空信息", e);
			this.responseContent(session, "");
		}
	}


	/**
	 * 从请求流中获取请求信息
	 * @param request
	 * @return
	 * @throws BaseException
	 */
	private ReqInfoBean getReqInfo(MutableHttpRequest request) throws BaseException {
		ReqInfoBean reqInfo = new ReqInfoBean();
		reqInfo.setPostInfo(decode(getRequXml(request)));
		_log.info("请求的参数：{}", reqInfo.getPostInfo());
		reqInfo.setHttpReqType(ReqInfoBean.HttpType.getHttpType(request.getHeader(ReqInfoBean.HTTP_REQ_TYPE)));
		_log.info("请求的类型：{}", reqInfo.getHttpReqType().getTypeName());
		reqInfo.setProxyUrl(request.getHeader(ReqInfoBean.HTTP_PROXY_URL));
		reqInfo.setReqUrl(request.getRequestUri().getPath());
		_log.info("请求的路径：{}，代理的路径{}：", reqInfo.getReqUrl(), reqInfo.getProxyUrl());
		reqInfo.setConnectTime(CastUtil.string2int(request.getHeader(ReqInfoBean.HTTP_CONNECT_TIME), 1000));
		reqInfo.setReadTime(CastUtil.string2int(request.getHeader(ReqInfoBean.HTTP_READ_TIME), 3000));
		_log.info("请求的连接超时时间：{}，读取超时时间{}：", reqInfo.getConnectTime(), reqInfo.getReadTime());
		String contentType = request.getHeader(Constant.CONTENT_TYPE);
		if(StringUtils.isEmpty(contentType)){
			reqInfo.setContentType(Constant.TEST_PLAN);
		}else{
			reqInfo.setContentType(contentType);
		}
		_log.info("请求的参数类型：{}", reqInfo.getContentType());
		String contentLength = request.getHeader(Constant.CONTENT_LENGTH);
		if(StringUtils.isNotEmpty(contentLength)){
			reqInfo.setContentLength(contentLength);
			_log.info("请求的参数长度：{}", reqInfo.getContentLength());
		}
		return reqInfo;
	}

	/**
	 * 对入参进行utf-8的decode解码 如果传入为null或者解析异常则返回原信息
	 * 
	 * @param reqInfo
	 * @return
	 */
	private String decode(String reqInfo) {
		try {
			if (StringUtils.isNotEmpty(reqInfo)) {
				reqInfo = HttpClientUtil.decode(reqInfo, Constant.UTF8);
			}
		} catch (Exception e) {
			_log.error("解码失败,原信息：" + reqInfo, e);
		}
		return reqInfo;
	}

	/**
	 * 获取请求中的xml报文
	 * 
	 * @param req
	 * @return
	 */
	private String getRequXml(MutableHttpRequest request) {
		/* 接收请求，解析POST报文体 */
		IoBuffer content = (IoBuffer) request.getContent();
		byte[] conBytes = new byte[content.limit()];
		content.get(conBytes);
		String reqXML = new String(conBytes);
		return reqXML;
	}

	/**
	 * 返回响应给商户的方法
	 * 
	 * @param session
	 * @param responseStr
	 */
	private void responseContent(HSessionInf session, String respStr) {
		try {
			/* 第四步：返回 */
			_log.info("返回的报文如下:{}", respStr);
			MutableHttpResponse res = IHttp.makeResp(new IHttp.HResponse(), IHttp.HConst.SC_OK, "", null,
					Constant.TEST_PLAN, respStr.getBytes());
			session.write(res);
		} catch (Exception e) {
			_log.error("", e);
			session.close("");
		}
	}

}
