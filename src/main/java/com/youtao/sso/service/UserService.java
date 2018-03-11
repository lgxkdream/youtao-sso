package com.youtao.sso.service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.youtao.common.service.RedisService;
import com.youtao.sso.mapper.UserMapper;
import com.youtao.sso.pojo.User;

/**
 * @title: UserService
 * @description: 
 * @Copyright: Copyright (c) 2018
 * @Company: lgxkdream.github.io
 * @author gang.li
 * @version 1.0.0
 * @since 2018年3月11日 下午5:18:12
 */
@Service
public class UserService {
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private RedisService redisService;

	/**
	 * 检查参数是否可用
	 * @param param 参数
	 * @param type 类型(1-用户名;2-手机号;3-邮箱)
	 * @return
	 */
	public Boolean check(String param, Integer type) {
		User record = new User();
		switch (type) {
		case 1:
			record.setUsername(param);
			break;
		case 2:
			record.setPhone(param);
			break;
		case 3:
			record.setEmail(param);
			break;
		default:
			return null;
		}
		int count = userMapper.selectCount(record);
		return count < 1;
	}

	/**
	 * 注册
	 * @param user
	 * @return
	 */
	public Boolean register(User user) {
		Date date = new Date();
		user.setId(null);
		user.setCreated(date);
		user.setUpdated(date);
		user.setPassword(DigestUtils.md5Hex(user.getPassword()));
		return 1 == this.userMapper.insert(user);
	}

	/**
	 * 登录
	 * @param username 用户名
	 * @param password 密码
	 * @return token
	 * @throws JsonProcessingException 
	 */
	public String login(String username, String password) throws JsonProcessingException {
		User record = new User();
		record.setUsername(username);
		User user = this.userMapper.selectOne(record);
		if (!Objects.isNull(user)) {
			// 登录成功
			if (StringUtils.equals(DigestUtils.md5Hex(password), user.getPassword())) {
				String token = DigestUtils.md5Hex(System.currentTimeMillis() + username);
				redisService.set("yttoken_" + token, user, 1800);
				return token;
			}
			// 密码错误
		}
		// 用户名不存在
		return null;
	}

	/**
	 * 用户信息查询
	 * @param token 浏览器token
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public User queryUserByToken(String token) throws JsonParseException, JsonMappingException, IOException {
		String key = "yttoken_" + token;
		User user = this.redisService.get(key, User.class);
		if (Objects.isNull(user)) {
			return null;
		}
		this.redisService.expire(key, 1800);
		return user;
	}

}
