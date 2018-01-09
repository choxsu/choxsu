package com.choxsu.common.interceptor;

import com.choxsu.common.kit.IpKit;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author choxsu
 */
public class VisitorInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorInterceptor.class);

    @Override
    public void intercept(Invocation inv) {
        //将用户访问信息保存到数据库
        Controller c = inv.getController();
        HttpServletRequest request = c.getRequest();
        String url = request.getRequestURL().toString();
        String ip = IpKit.getRealIp(request);
        String method = request.getMethod();
        String userAgent = request.getHeader("user-agent");
        Record record = new Record();
        record.set("ip", ip);
        LOGGER.info("请求ip:{}", ip);
        record.set("url", url);
        record.set("method", method);
        record.set("client", userAgent);
        record.set("requestTime", new Date());
        boolean b = Db.save("visitor", record);
        LOGGER.info("保存后返回的结果：{}", b);
        inv.invoke();
    }

}