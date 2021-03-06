

package com.choxsu.front.login;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

/**
 * @author choxsu
 * ajax 登录参数验证
 */
public class LoginValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		setShortCircuit(true);

		validateRequired("userName", "userNameMsg", "邮箱不能为空");
		validateEmail("userName", "userNameMsg", "邮箱格式不正确");
		validateRequired("encryptPwd", "passwordMsg", "密码key不能为空");
		validateCaptcha("captcha", "captchaMsg", "验证码不正确");
	}

	@Override
	protected void handleError(Controller c) {
		c.renderJson();
	}
}
