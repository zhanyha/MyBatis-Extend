package com.zyh.mybatisframework.test.dao;


import com.zyh.mybatisframework.plugin.impl.tableselect.TableSelect;
import com.zyh.mybatisframework.plugin.impl.tableselect.DBRouterStrategy;
import com.zyh.mybatisframework.test.po.Activity;

import java.util.List;

@DBRouterStrategy
public interface IActivityDao {

    @TableSelect(key = "activityId")
    Activity queryActivityById(Activity activity);

    List<Activity> queryActivity();


}
