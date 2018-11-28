package com.choxsu.web.front.code;

import com.jfinal.aop.Inject;
import com.choxsu.web.front.blog.BlogService;
import com.choxsu.common.base.BaseController;
import com.choxsu.common.constant.CategoryEnum;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author chox su
 * @date 2018/01/06 12:31
 */
public class CodeController extends BaseController {

    @Inject
    BlogService blogService;

    public void index() {
        Integer pageNumber = getParaToInt(0, 1);
        Page<Record> recordPage = blogService.findBlogListByCategory(pageNumber, 15, CategoryEnum.CODE.getName());
        setAttr("result", recordPage);
        render("list.html");
    }

    public void detail() {
        Integer id = getParaToInt();
        Record record = blogService.findBlog(id);
        if (record == null){
            renderError(404);
            return;
        }
        setAttr("blog", record);
        render("detail.html");
    }


}
