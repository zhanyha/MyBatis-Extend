package com.zyh.mybatisframework.step07.mapper;


import com.zyh.mybatisframework.step06.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

}
