package com.choxsu.common.entity;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * @author choxsu , do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {

    public static void mapping(ActiveRecordPlugin arp) {
        arp.addMapping(Account.tableName, "id", Account.class);
        arp.addMapping("account_open", "id", AccountOpen.class);
        // Composite Primary Key order: accountId,roleId
        arp.addMapping(AccountRole.tableName, "accountId,roleId", AccountRole.class);
        arp.addMapping("blog", "id", Blog.class);
        arp.addMapping(BlogCategory.tableName, "id", BlogCategory.class);
        arp.addMapping(BlogTag.tableName, "id", BlogTag.class);
        arp.addMapping(Permission.tableName, "id", Permission.class);
        arp.addMapping(Role.tableName, "id", Role.class);
        // Composite Primary Key order: permissionId,roleId
        arp.addMapping(RolePermission.tableName, "permissionId,roleId", RolePermission.class);
        arp.addMapping(SensitiveWords.tableName, "id", SensitiveWords.class);
        arp.addMapping("visitor", "id", Visitor.class);

        arp.addMapping(AuthCode.tableName, "id", AuthCode.class);
        arp.addMapping(Session.tableName, "id", Session.class);

        arp.addMapping("blog_reply", "id", BlogReply.class);
        arp.addMapping("news_feed", "id", NewsFeed.class);
        arp.addMapping("refer_me", "id", ReferMe.class);
        arp.addMapping("remind", "accountId", Remind.class);

        arp.addMapping("ip_repository", "id", IpRepository.class);
        arp.addMapping("code_config", "id", CodeConfig.class);
    }
}

