package com.choxsu.web.admin.user;

import com.choxsu.component.base.BaseProjectController;
import com.choxsu.component.util.JFlyFoxUtils;
import com.choxsu.util.StrUtils;
import com.choxsu.util.encrypt.Md5Utils;
import com.choxsu.web.admin.annotation.ControllerBind;
import com.choxsu.web.admin.common.db.SQLUtils;
import com.choxsu.web.admin.department.DepartmentSvc;
import com.choxsu.web.admin.role.SysRole;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * 用户管理
 * 
 * @author flyfox 2014-2-11
 */
@ControllerBind(controllerKey = "/system/user")
public class UserController extends BaseProjectController {

	private static final String path = "/pages/system/user/user_";

	public void index() {
		list();
	}
	
	public void list() {
		SysUser model = getModelByAttr(SysUser.class);

		SQLUtils sql = new SQLUtils(" from sys_user t " //
				+ " left join sys_department d on d.id = t.departid " //
				+ " where 1 = 1 and userid != 1 ");

		if (model.getAttrValues().length != 0) {
			sql.whereLike("username", model.getStr("username"));
			sql.whereLike("realname", model.getStr("realname"));
			sql.whereEquals("usertype", model.getInt("usertype"));
			sql.whereEquals("departid", model.getInt("departid"));
		}

		// 排序
		String orderBy = getBaseForm().getOrderBy();
		if (StrUtils.isEmpty(orderBy)) {
			sql.append(" order by userid desc");
		} else {
			sql.append(" order by ").append(orderBy);
		}
		
		Page<SysUser> page = SysUser.dao.paginate(getPaginator(), "select t.*,d.name as departname ", sql.toString());
		// 下拉框
		setAttr("departSelect", new DepartmentSvc().selectDepart(model.getInt("departid")));

		setAttr("page", page);
		setAttr("attr", model);
		render(path + "list.html");
	}

	public void add() {
		setAttr("departSelect", new DepartmentSvc().selectDepart(0));

		render(path + "add.html");
	}

	public void view() {
		SysUser model = SysUser.dao.findById(getParaToInt());
		model.put("secretKey", new Md5Utils().getMD5(model.getStr("password")).toLowerCase());

		// 部门名称
		model.put("departname", new DepartmentSvc().getDepartName(model.getInt("departid")));
		setAttr("model", model);

		render(path + "view.html");
	}

	public void delete() {
		int userid = getParaToInt();
		// 日志添加
		SysUser model = new SysUser();
		String now = getNow();
		model.put("update_id", getSessionUser().getUserID());
		model.put("update_time", now);

		// 删除授权
		Db.update("delete from sys_user_role where userid = ? ", userid);

		model.deleteById(userid);

		UserCache.init();
		list();
	}

	public void edit() {
		SysUser model = SysUser.dao.findById(getParaToInt());

		setAttr("departSelect", new DepartmentSvc().selectDepart(model.getInt("departid")));

		setAttr("model", model);
		render(path + "edit.html");
	}

	public void save() {
		String msg = "保存成功";
		Integer pid = getParaToInt();
		SysUser model = getModel(SysUser.class);
		//对用户名作唯一验证
		String username = model.get("username");
		List<SysUser> userList = model.findByWhere("where username = ?", username);

		if (pid != null && pid > 0) { // 更新
			userList = model.findByWhere("where username = ? and userid !=?", username,model.getUserid());
			if (userList != null && userList.size()>0){
				msg = "保存失败,用户名重复";
			} else {
				// 日志添加
				Integer userid = getSessionUser().getUserID();
				String now = getNow();
				model.put("update_id", userid);
				model.put("update_time", now);
				model.update();
			}
		} else { // 新增
			if (userList != null && userList.size()>0){
				msg = "保存失败,用户名重复";
			} else {
				// 日志添加
				Integer userid = getSessionUser().getUserID();
				String now = getNow();
				model.put("update_id", userid);
				model.put("update_time", now);
				model.remove("userid");
				if (StrUtils.isEmpty(model.getStr("password"))) {
					model.put("password", JFlyFoxUtils.getDefaultPassword());
				}
				model.put("create_id", userid);
				model.put("create_time", now);
				model.save();
			}
		}
		UserCache.init();
		renderMessage(msg);
	}

	/**
	 * 跳转到授权页面
	 * 
	 * 2015年4月28日 下午12:00:05 flyfox 369191470@qq.com
	 */
	public void auth() {
		int userid = getParaToInt();
		List<SysRole> list = SysRole.dao.findByWhere(" where status=1 order by sort ");

		String roleids = new UserSvc().getRoleids(userid);
		setAttr("userid", userid);
		setAttr("roleids", roleids);
		// 根结点
		setAttr("list", list);
		render(path + "auth.html");
	}

	/**
	 * 保存角色信息
	 * 
	 * 2015年4月28日 下午3:18:33 flyfox 369191470@qq.com
	 */
	public void auth_save() {
		int userid = getParaToInt("userid");
		String roleids = getPara("roleids");

		new UserSvc().saveAuth(userid, roleids, getSessionUser().getUserID());
		renderMessage("保存成功");
	}

}