package com.proj.proxyservice.exception;

public class BaseException extends Exception {
	private static final long serialVersionUID = 8471862908104551426L;
	/*
	 * 错误码
	 */
	private String errCode;
	/*
	 * 错误信息
	 */
	private String errMsg;
	/*
	 * 真正的异常
	 */
	private Exception exception;

	public BaseException(String errCode, String errMsg) {
		super(errCode + errMsg);
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public BaseException(String errCode, String errMsg, Exception exception) {
		super(errCode + errMsg, exception);
		this.errCode = errCode;
		this.errMsg = errMsg;
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

}
