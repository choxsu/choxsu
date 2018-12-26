package com.choxsu._admin.sensitive_word;

import com.choxsu.common.base.BaseController;
import com.choxsu.common.entity.SensitiveWords;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author choxsu
 * @date 2018/12/26
 */
public class SensitiveWordController extends BaseController {

    @Inject
    SensitiveWordService sensitiveWordService;

    public void index() {
        Integer p = getParaToInt("p", 1);
        Integer size = getParaToInt("size", 10);
        Page<SensitiveWords> page = sensitiveWordService.list(p, size);
        setAttr("page", page);
        render("index.html");
    }

    public void add() {
        render("addOrEdit.html");
    }


    @Before({POST.class})
    public void saveOrUpdate() {
        SensitiveWords sensitiveWords = getModel(SensitiveWords.class, "r");
        String word = sensitiveWords.getWord();
        if (StrKit.isBlank(word)) {
            renderJson(Ret.fail().set("msg", "请输入需要屏蔽敏感字"));
            return;
        }
        Ret ret = sensitiveWordService.saveOrUpdate(sensitiveWords);
        renderJson(ret);
    }


    public void edit() {
        Integer id = getParaToInt("id");
        SensitiveWords sensitiveWords = sensitiveWordService.findById(id);
        setAttr("r", sensitiveWords);
        render("addOrEdit.html");
    }

    public void delete() {
        boolean bool = sensitiveWordService.deleteById(getParaToInt("id"));
        if (bool) {
            renderJson(Ret.ok().set("msg", "删除成功"));
            return;
        }
        renderJson(Ret.fail().set("msg", "删除失败!"));

    }


}