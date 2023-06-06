package com.zyh.mybatisframework.step05.mapper;

import com.zyh.mybatisframework.step05.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);
}