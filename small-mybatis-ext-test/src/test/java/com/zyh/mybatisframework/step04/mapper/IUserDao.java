package com.zyh.mybatisframework.step04.mapper;

import com.zyh.mybatisframework.step04.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);
}