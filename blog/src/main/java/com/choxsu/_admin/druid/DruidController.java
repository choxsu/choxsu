package com.choxsu._admin.druid;

import com.choxsu._admin.permission.Remark;
import com.choxsu.common.base.BaseController;

public class DruidController extends BaseController {

    @Remark("Druid管理首页")
    public void index(){
        render("index.html");
    }
}
