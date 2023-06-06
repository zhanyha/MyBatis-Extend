package com.zyh.mybatisframework.step01.mapper;

public interface IUserDao {

    String queryUserName(String uId);

    Integer queryUserAge(String uId);

}