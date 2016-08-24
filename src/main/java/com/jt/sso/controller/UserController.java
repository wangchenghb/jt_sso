package com.jt.sso.controller;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jt.common.vo.SysResult;
import com.jt.sso.pojo.User;
import com.jt.sso.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {

	@Autowired
	private UserService userservice;

	// 检查数据是否可用
	// http://sso.jt.com/user/check/{param}/{type}
	@RequestMapping("check/{param}/{type}")
	@ResponseBody
	public SysResult check(@PathVariable String param, @PathVariable Integer type) {
		try {
			Integer countNum = userservice.check(param, type);
			return SysResult.ok(countNum == 0 ? false : true);
		} catch (Exception e) {
			e.printStackTrace();
			return SysResult.build(201, "检查出错！");
		}
	}

	// http://sso.jt.com/user/register
	@RequestMapping("/register")
	@ResponseBody
	public SysResult register(User user) {
		try {
			userservice.register(user);
			return SysResult.ok();
		} catch (Exception e) {
			return SysResult.build(201, "注册用户失败");
		}
	}

	// http://sso.jt.com/user/login
	@RequestMapping("login")
	@ResponseBody
	public SysResult login(String u, String p) throws JsonProcessingException {
		String ticket = userservice.login(u, p);

		if (StringUtils.isNotEmpty(ticket)) {
			return SysResult.ok(ticket);
		} else {
			return SysResult.build(201, "登陆失败，请检查用户名密码是否正确！");
		}
	}

	// http://sso.jt.com/user/query/{ticket}
	@RequestMapping("query/{ticket}")
	@ResponseBody
	public SysResult query(@PathVariable String ticket) {
		String jedisData = userservice.queryTic(ticket);
		if (StringUtils.isNotEmpty(jedisData)) {
			return SysResult.ok(jedisData);
		} else {
			return SysResult.build(201, "按ticket没有找到数据");
		}
	}

}
