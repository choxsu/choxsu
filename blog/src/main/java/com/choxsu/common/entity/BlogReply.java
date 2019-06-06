package com.choxsu.common.entity;


import com.choxsu.common.entity.base.BaseBlogReply;
import lombok.Data;

/**
 * Generated by JFinal.
 */
public class BlogReply extends BaseBlogReply<BlogReply> {

    /**
     * 文章标题
     */
    public String getTitle() {
        return get("title");
    }

    /**
     * 昵称
     */
    public String getNickName() {
        return get("nickName");
    }
}
