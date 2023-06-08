package com.zyh.mybatisframework.step08.mapper;


import com.zyh.mybatisframework.step06.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);
    User queryUserInfoByIdAndUsername(Long uId, String username);

    User queryUserInfoByUsername(String username);

}
