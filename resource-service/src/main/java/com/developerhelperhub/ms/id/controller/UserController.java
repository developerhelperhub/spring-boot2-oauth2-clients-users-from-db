package com.developerhelperhub.ms.id.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public String listUser() {
		return "success";
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public String create() {
		return "success";
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	public String delete(@PathVariable(value = "id") Long id) {
		return "success";
	}
}
