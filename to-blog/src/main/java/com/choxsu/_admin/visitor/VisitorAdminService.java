package com.choxsu._admin.visitor;

import com.choxsu.common.base.BaseService;
import com.choxsu.common.base.SortEnum;
import com.choxsu.common.entity.Visitor;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author choxsu
 */
public class VisitorAdminService extends BaseService<Visitor> {


    public static final VisitorAdminService me = new VisitorAdminService();

    @Override
    public String getTableName() {
        return Visitor.tableName;
    }

    public Page<Visitor> paginate(int page){

        return paginateOrderBy(page, 10, "id", SortEnum.DESC);
    }



}