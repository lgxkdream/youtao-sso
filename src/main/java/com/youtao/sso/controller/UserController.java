package com.youtao.sso.controller;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.youtao.common.utils.CookieUtils;
import com.youtao.sso.pojo.User;
import com.youtao.sso.service.UserService;

/**
 * @title: UserController
 * @description: 
 * @Copyright: Copyright (c) 2018
 * @Company: lgxkdream.github.io
 * @author gang.li
 * @version 1.0.0
 * @since 2018年3月11日 上午11:53:09
 */
@Controller
@RequestMapping("/user")
public class UserController {
	
	private static final String COOKIE_NAME = "YT_TOKEN";
	
	@Autowired
	private UserService userService;
	
	/**
	 * 检测数据是否可用
	 * @param param 参数
	 * @param type 类型(1-用户名;2-手机号;3-邮箱)
	 * @return
	 */
	@RequestMapping(value = "/check/{param}/{type}", method =RequestMethod.GET)
	public ResponseEntity<Boolean> check(@PathVariable("param") String param, @PathVariable("type") Integer type) {
		try {
			Boolean result = this.userService.check(param, type);
			if (Objects.isNull(result)) {
				// 参数有误
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			}
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	
	/**
	 * 用户注册页面
	 * @return
	 */
	@RequestMapping(value = "/register", method =RequestMethod.GET)
	public String register() {
		return "register";
	}
	
	/**
	 * 用户注册
	 * @param user
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/register", method =RequestMethod.POST)
	public Map<String, Object> register(@Validated User user, BindingResult bindingResult) {
		Map<String, Object> result = Maps.newHashMap();
		if (bindingResult.hasErrors()) {
			result.put("status", "400");
			result.put("data", bindingResult.getFieldError().getDefaultMessage());
			return result;
		}
		try {
			Boolean bool = this.userService.register(user);
			if (bool) {
				result.put("status", "200");
			} else {
				result.put("status", "300");
				result.put("data", "原因未知，请联系客服处理");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "500");
			result.put("data", e.getMessage());
		}
		return result;
	}
	
	/**
	 * 登录页面
	 * @return
	 */
	@RequestMapping(value = "/login", method =RequestMethod.GET)
	public String login() {
		return "login";
	}
	
	/**
	 * 登录
	 * @param username 用户名
	 * @param password 密码
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/login", method =RequestMethod.POST)
	public Map<String, Object> login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = Maps.newHashMap();
		try {
			String token = this.userService.login(username, password);
			if (StringUtils.isBlank(token)) {
				result.put("status", 300);
			} else {
				CookieUtils.setCookie(request, response, COOKIE_NAME, token);
				result.put("status", 200);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", 500);
		}
		return result;
	}
	
	/**
	 * 用户信息查询
	 * @param token 浏览器token
	 * @return
	 */
	@RequestMapping(value = "/query/{token}", method = RequestMethod.GET)
	public ResponseEntity<User> query(@PathVariable("token") String token) {
		try {
			User user = this.userService.queryUserByToken(token);
			if (Objects.isNull(user)) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

}
