package com.jt.sso.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.common.service.RedisService;
import com.jt.sso.mapper.UserMapper;
import com.jt.sso.pojo.User;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisService redisService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// 1.检查数据是否可用
	public Integer check(String param, Integer type) {
		String condition = "";
		if (type == 1) {
			condition = "username='" + param + "'";
		} else if (type == 2) {
			condition = "phone='" + param + "'";
		} else if (type == 3) {
			condition = "email='" + param + "'";
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("condition", condition);

		return userMapper.check(map);
	}

	// 2.用户注册
	public void register(User user) {
		user.setCreated(new Date());
		user.setUpdated(user.getCreated());
		userMapper.insertSelective(user);

	}

	// 3.用户登陆，返回值封装ticket
	public String login(String username, String password) throws JsonProcessingException {
		// 先按用户查询，然后得到的结果中比较密码
		User user = new User();
		user.setUsername(username);
		List<User> userList = userMapper.select(user);
		// 对应用户名的用户存在
		if (userList != null && userList.size() > 0) {
			// 获取第一个元素
			User _user = userList.get(0);
			password = DigestUtils.md5Hex(password);
			if (password.equals(_user.getPassword())) {
				String ticket = DigestUtils.md5Hex("TICKET_" + username + System.currentTimeMillis());
				// 写入redis
				redisService.set(ticket, MAPPER.writeValueAsString(_user));
				return ticket;
			}
		}
		return null;
	}

	// 4. 通过ticket查询用户信息
	public String queryTic(String ticket) {
		return redisService.get(ticket);
	}

}
