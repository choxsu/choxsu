package com.choxsu.common.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseAccountOpen<M extends BaseAccountOpen<M>> extends Model<M> implements IBean {

	/**
	 * ID
	 */
	public void setId(Integer id) {
		set("id", id);
	}

	/**
	 * ID
	 */
	public Integer getId() {
		return getInt("id");
	}

	/**
	 * 账户ID
	 */
	public void setAccountId(Integer accountId) {
		set("accountId", accountId);
	}

	/**
	 * 账户ID
	 */
	public Integer getAccountId() {
		return getInt("accountId");
	}

	/**
	 * 第三方类型，比如qq、weibo
	 */
	public void setOpenType(String openType) {
		set("openType", openType);
	}

	/**
	 * 第三方类型，比如qq、weibo
	 */
	public String getOpenType() {
		return getStr("openType");
	}

	/**
	 * 代表用户唯一身份的ID
	 */
	public void setOpenId(String openId) {
		set("openId", openId);
	}

	/**
	 * 代表用户唯一身份的ID
	 */
	public String getOpenId() {
		return getStr("openId");
	}

	/**
	 * 调用接口需要用到的token，比如利用accessToken发表微博等，如果只是对接登录的话，这个其实没啥用
	 */
	public void setAccessToken(String accessToken) {
		set("accessToken", accessToken);
	}

	/**
	 * 调用接口需要用到的token，比如利用accessToken发表微博等，如果只是对接登录的话，这个其实没啥用
	 */
	public String getAccessToken() {
		return getStr("accessToken");
	}

	/**
	 * 授权过期时间，第三方登录授权都是有过期时间的，比如3个月之后，这里存放毫秒数，过期的毫秒数
	 */
	public void setExpiredTime(Long expiredTime) {
		set("expiredTime", expiredTime);
	}

	/**
	 * 授权过期时间，第三方登录授权都是有过期时间的，比如3个月之后，这里存放毫秒数，过期的毫秒数
	 */
	public Long getExpiredTime() {
		return getLong("expiredTime");
	}

}
