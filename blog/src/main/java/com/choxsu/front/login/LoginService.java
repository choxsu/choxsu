

package com.choxsu.front.login;

import com.choxsu.common.authcode.AuthCodeService;
import com.choxsu.common.entity.Account;
import com.choxsu.common.entity.AccountOpen;
import com.choxsu.common.entity.AuthCode;
import com.choxsu.common.entity.Session;
import com.choxsu.front.login.entity.QQUserInfo;
import com.choxsu.front.login.entity.QQVo;
import com.choxsu.front.register.RegEntity;
import com.choxsu.front.login.entity.Token;
import com.choxsu.front.login.kit.QQKit;
import com.choxsu.kit.EmailKit;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;

import java.util.Date;

/**
 * 登录业务
 */
@Before(Tx.class)
public class LoginService {

    @Inject
    QQVo qqVo;

    public static final LoginService me = new LoginService();

    private Account accountDao = new Account().dao();
    private AccountOpen accountOpenDao = new AccountOpen().dao();

    // 存放登录用户的 cacheName
    public static final String loginAccountCacheName = "loginAccount";

    // "jfinalId" 仅用于 cookie 名称，其它地方如 cache 中全部用的 "sessionId" 来做 key
    public static final String sessionIdName = "jfinalId";

    /**
     * 登录成功返回 sessionId 与 loginAccount，否则返回一个 msg
     */
    public Ret login(String userName, String password, boolean keepLogin, String loginIp) {
        userName = userName.toLowerCase().trim();
        password = password.trim();
        Account loginAccount = accountDao.findFirst("select * from account where userName=? limit 1", userName);
        if (loginAccount == null) {
            return Ret.fail("msg", "用户名或密码不正确");
        }
        if (loginAccount.isStatusLockId()) {
            return Ret.fail("msg", "账号已被锁定");
        }
        if (loginAccount.isStatusReg()) {
            return Ret.fail("msg", "账号未激活，请先激活账号");
        }

        String salt = loginAccount.getSalt();
        String hashedPass = HashKit.sha256(salt + password);
        // 未通过密码验证
        if (!loginAccount.getPassword().equals(hashedPass)) {
            return Ret.fail("msg", "用户名或密码不正确");
        }

        // 如果用户勾选保持登录，暂定过期时间为 15 天，否则为 7 天，单位为秒
        long liveSeconds = keepLogin ? 15 * 24 * 60 * 60 : 7 * 24 * 60 * 60;
        // 传递给控制层的 cookie
        int maxAgeInSeconds = (int) (keepLogin ? liveSeconds : -1);
        return loginAction(liveSeconds, loginAccount, loginIp, maxAgeInSeconds);
    }

    /**
     * 登陆后的处理
     *
     * @param liveSeconds     存活毫秒数
     * @param loginAccount    登陆账户
     * @param loginIp         ip
     * @param maxAgeInSeconds 存活秒数
     * @return
     */
    private Ret loginAction(long liveSeconds, Account loginAccount, String loginIp, Integer maxAgeInSeconds) {
        // expireAt 用于设置 session 的过期时间点，需要转换成毫秒
        long expireAt = System.currentTimeMillis() + (liveSeconds * 1000);
        // 保存登录 session 到数据库
        Session session = new Session();
        String sessionId = StrKit.getRandomUUID();
        session.setId(sessionId);
        session.setAccountId(loginAccount.getId());
        session.setExpireAt(expireAt);
        if (!session.save()) {
            return Ret.fail("msg", "保存 session 到数据库失败，请联系管理员");
        }

        loginAccount.removeSensitiveInfo();                                 // 移除 password 与 salt 属性值
        loginAccount.put("sessionId", sessionId);                          // 保存一份 sessionId 到 loginAccount 备用
        CacheKit.put(loginAccountCacheName, sessionId, loginAccount);

        createLoginLog(loginAccount.getId(), loginIp);
        return Ret.ok(sessionIdName, sessionId)
                .set(loginAccountCacheName, loginAccount)
                .set("maxAgeInSeconds", maxAgeInSeconds);   // 用于设置 cookie 的最大存活时间

    }


    public Account getLoginAccountWithSessionId(String sessionId) {
        return CacheKit.get(loginAccountCacheName, sessionId);
    }

    /**
     * 通过 sessionId 获取登录用户信息
     * sessoin表结构：session(id, accountId, expireAt)
     * <p>
     * 1：先从缓存里面取，如果取到则返回该值，如果没取到则从数据库里面取
     * 2：在数据库里面取，如果取到了，则检测是否已过期，如果过期则清除记录，
     * 如果没过期则先放缓存一份，然后再返回
     */
    public Account loginWithSessionId(String sessionId, String loginIp) {
        Session session = Session.dao.findById(sessionId);
        if (session == null) {      // session 不存在
            return null;
        }
        if (session.isExpired()) {  // session 已过期
            session.delete();        // 被动式删除过期数据，此外还需要定时线程来主动清除过期数据
            return null;
        }

        Account loginAccount = accountDao.findById(session.getAccountId());
        // 找到 loginAccount 并且 是正常状态 才允许登录
        if (loginAccount != null && loginAccount.isStatusOk()) {
            loginAccount.removeSensitiveInfo();                                 // 移除 password 与 salt 属性值
            loginAccount.put("sessionId", sessionId);                          // 保存一份 sessionId 到 loginAccount 备用
            CacheKit.put(loginAccountCacheName, sessionId, loginAccount);

            createLoginLog(loginAccount.getId(), loginIp);
            return loginAccount;
        }
        return null;
    }

    /**
     * 创建登录日志
     */
    private void createLoginLog(Integer accountId, String loginIp) {
        Record loginLog = new Record().set("accountId", accountId).set("ip", loginIp).set("loginAt", new Date());
        Db.save("login_log", loginLog);
    }

    /**
     * 发送密码找回授权邮件
     */
    public Ret sendRetrievePasswordAuthEmail(String userName) {
        if (StrKit.isBlank(userName) || userName.indexOf('@') == -1) {
            return Ret.fail("msg", "email 格式不正确，请重新输入");
        }
        userName = userName.toLowerCase().trim();   // email 转成小写
        Account account = accountDao.findFirst("select * from account where userName=? limit 1", userName);
        if (account == null) {
            return Ret.fail("msg", "您输入的 email 还没注册，无法找回密码");
        }

        String authCode = AuthCodeService.me.createRetrievePasswordAuthCode(account.getId());

        String title = "Choxsu 密码找回邮件";
        String content = "在浏览器地址栏里输入并访问下面链接即可重置密码：\n\n"
                + " https://choxsu.cn/login/retrievePassword?authCode="
                + authCode;

        String toEmail = account.getStr("userName");
        try {
            EmailKit.sendEmail(toEmail, title, content, false);
            return Ret.ok("msg", "密码找回邮件已发送至邮箱，请收取邮件并重置密码");
        } catch (Exception e) {
            return Ret.fail("msg", "密码找回邮件发送失败，可能是邮件服务器出现故障，请联系choxsu@163.com");
        }
    }

    /**
     * 找回密码
     */
    public Ret retrievePassword(String authCodeId, String newPassword) {
        if (StrKit.isBlank(newPassword)) {
            return Ret.fail("msg", "密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Ret.fail("msg", "密码长度不能小于6");
        }

        AuthCode authCode = AuthCodeService.me.getAuthCode(authCodeId);
        if (authCode != null && authCode.isValidRetrievePasswordAuthCode()) {
            String salt = HashKit.generateSaltForSha256();
            newPassword = HashKit.sha256(salt + newPassword);
            int accountId = authCode.getAccountId();
            int result = Db.update("update account set password=?, salt=? where id=? limit 1", newPassword, salt, accountId);
            if (result > 0) {
                return Ret.ok("msg", "密码更新成功，现在即可登录");
            } else {
                return Ret.fail("msg", "未找到账号，请联系管理员");
            }
        } else {
            return Ret.fail("msg", "authCode 不存在或已经失效，可以尝试重新发送找回密码邮件");
        }
    }

    /**
     * 退出登录
     */
    public void logout(String sessionId) {
        if (sessionId != null) {
            CacheKit.remove(loginAccountCacheName, sessionId);
            Session.dao.deleteById(sessionId);
        }
    }

    /**
     * 从数据库重新加载登录账户信息
     */
    public void reloadLoginAccount(Account loginAccountOld) {
        String sessionId = loginAccountOld.get("sessionId");
        Account loginAccount = accountDao.findFirst("select * from account where id=? limit 1", loginAccountOld.getId());
        loginAccount.removeSensitiveInfo();               // 移除 password 与 salt 属性值
        loginAccount.put("sessionId", sessionId);        // 保存一份 sessionId 到 loginAccount 备用

        // 集群方式下，要做一通知其它节点的机制，让其它节点使用缓存更新后的数据，
        // 将来可能把 account 用 id : obj 的形式放缓存，更新缓存只需要 CacheKit.remove("account", id) 就可以了，
        // 其它节点发现数据不存在会自动去数据库读取，所以未来可能就是在 AccountService.getById(int id)的方法引入缓存就好
        // 所有用到 account 对象的地方都从这里去取
        CacheKit.put(loginAccountCacheName, sessionId, loginAccount);
    }

    Ret qqCallback(String code, String ip) {
        String result = QQKit.getToken(code, qqVo);
        Token token = QQKit.tokenHandler(result);
        if (token == null) {
            return Ret.fail();
        }
        String openId = QQKit.getOpenId(token.getAccessToken());
        if (openId == null) {
            return Ret.fail();
        }
        QQUserInfo userInfo = QQKit.getUserInfo(token.getAccessToken(), openId, qqVo.getAppId());
        if (userInfo == null) {
            return Ret.fail();
        }
        // 登陆成功，保存数据, 先查询是否已经登陆过
        AccountOpen accountOpen = accountOpenDao.findFirst("select * from account_open where openId = ? limit 1", openId);
        Account account;
        if (accountOpen != null) {
            account = accountDao.findById(accountOpen.getAccountId());
            accountOpen.setAccessToken(token.getAccessToken());
            accountOpen.setExpiredTime((long) token.getExpiresIn());
            accountOpen.update();
        } else {
            account = new Account();
            account.setAvatar(userInfo.getFigureurl_qq_1());
            account.setCreateAt(new Date());
            account.setIp(ip);
            account.setLikeCount(0);
            account.setNickName(userInfo.getNickname());
            account.setUserName("");
            account.setPassword("");
            account.setSalt("");
            account.setStatus(Account.STATUS_OK);
            account.setIsThird(Account.IS_THIRD_LOGIN);
            account.save();
            accountOpen = new AccountOpen();
            accountOpen.setAccessToken(token.getAccessToken());
            accountOpen.setOpenId(openId);
            accountOpen.setAccountId(account.getId());
            accountOpen.setExpiredTime((long) token.getExpiresIn());
            accountOpen.setOpenType(AccountOpen.OPEN_TYPE_QQ);
            accountOpen.save();
        }

        long liveSeconds = 7 * 24 * 60 * 60;
        // 传递给控制层的 cookie
        int maxAgeInSeconds = (int) liveSeconds;
        return loginAction(liveSeconds, account, ip, maxAgeInSeconds);
    }

    /**
     * 获取qq登陆页面
     *
     * @return
     */
    public String getAuthUrl() {
        return QQKit.getAuthUrl(qqVo);
    }

    public Ret register(RegEntity regModel) {

        return Ret.ok();
    }
}
