package com.zyh.mybatisframework.step03.mapper;

public interface IUserDao {

    String queryUserInfoById(String username);

    Integer queryUserAge(String age);

}