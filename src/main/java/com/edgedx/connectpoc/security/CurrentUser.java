package com.edgedx.connectpoc.security;

import com.edgedx.connectpoc.entity.User;

@FunctionalInterface
public interface CurrentUser {

	User getUser();
}
