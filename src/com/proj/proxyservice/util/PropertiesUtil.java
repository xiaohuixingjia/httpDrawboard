package com.proj.proxyservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {

	private static final Logger _log = LoggerFactory
			.getLogger("PropertiesReader");

	private static final String PFILE = System.getProperty("user.dir")
			+ "/resource/";// 路径

	private Properties m_props = null;// 配置文件
	private String fileName;// 文件�?
	private File m_file = null;// 文件
	private long m_lastModifiedTime = 0;// �?��修改时间

	/**
	 * 加载配置文件
	 * 
	 * @param filename
	 */
	private PropertiesUtil(String filename) {
		this.fileName = filename;
		m_file = new File(PFILE + fileName);
		m_lastModifiedTime = m_file.lastModified();

		if (m_lastModifiedTime == 0) {
			_log.error("file[" + PFILE + fileName + "] not found . ");
		}
		m_props = new Properties();
		try {
			m_props.load(new FileInputStream(PFILE + fileName));
		} catch (FileNotFoundException e) {
			_log.error("file[" + PFILE + fileName + "] not found : ", e);
		} catch (IOException e) {
			_log.error("Properties IO error : ", e);
		}
	}

	/**
	 * 单例
	 * 
	 * @param filename
	 * @return
	 */
	synchronized public static PropertiesUtil getInstance(String filename) {
		return new PropertiesUtil(filename);
	}

	/**
	 * 获取配置文件
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return m_props;
	}

	/**
	 * 获取配置文件参数�?
	 * 
	 * @param name
	 * @return
	 */
	public String getConfigItem(String name) {
		long newTime = m_file.lastModified();

		// 判断�?��修改时间，如果变化则重新加载文件�?
		if (newTime == 0) {
			if (m_lastModifiedTime == 0) {
				_log.error("file[" + PFILE + fileName + "] not found . ");
			} else {
				_log.error("file[" + PFILE + fileName + "] was deleted . ");
			}
			// return defaultVal;
		} else if (newTime > m_lastModifiedTime) {
			m_props.clear();
			try {
				m_props.load(new FileInputStream(PFILE + fileName));
			} catch (FileNotFoundException e) {
				_log.error("file[" + PFILE + fileName + "] not found : ", e);
			} catch (IOException e) {
				_log.error("Properties IO error : ", e);
			}
		}

		m_lastModifiedTime = newTime;

		// 获取参数�?
		String val = m_props.getProperty(name);
		return val;
	}
}
