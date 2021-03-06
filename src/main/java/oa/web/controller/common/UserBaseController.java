package oa.web.controller.common;

import com.common.dao.interf.IUserLoginDao;
import com.common.dict.Constant2;
import com.common.entity.user.interf.GenericUser;
import com.common.util.WebServletUtil;
import com.string.widget.util.ValueWidget;
import com.string.widget.util.XSSUtil;
import oa.bean.LoginResultBean;
import oa.service.DictionaryParam;
import oa.util.AuthenticateUtil;
import oa.web.controller.base.BaseController;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class UserBaseController<T extends GenericUser> extends BaseController<T> {
    Logger logger = Logger.getLogger(this.getClass());
    private IUserLoginDao<T> userDao;

    /**
     * @param request : 用于记录日志时获取请求信息,比如ip,请求参数等
     * @param session
     */
    public void logoutCommon(HttpServletRequest request, HttpSession session) {
        GenericUser user = AuthenticateUtil.logout(session);
        afterLogout(request, session, user);//user 用于注销时日志记录userId
        session.getServletContext().setAttribute(session.getId(), null);
    }

    /**
     * 子类可以覆写,用于清理自定义的session key
     * @param request : 用于记录日志时获取请求信息,比如ip,请求参数等
     * @param session
     */
    public void afterLogout(HttpServletRequest request, HttpSession session, GenericUser user) {

    }
    public LoginResultBean loginCommon(Model model, GenericUser user, HttpServletRequest request, HttpServletResponse response
            , HttpSession session, String issaveUserName, String issavePasswd) {
        LoginResultBean loginResultBean = new LoginResultBean();
        loginResultBean.setFailed(true);
        int login_result = 0;
        if (ValueWidget.isNullOrEmpty(user) || ValueWidget.isNullOrEmpty(user.getUsername())) {
            loginResultBean.setMessage("请输入用户名.");
            return loginResultBean;
        } else if (user.getUsername().trim().length() < 3 || user.getUsername().trim().length() > 16) {
            loginResultBean.setMessage("请输入3-16位用户名字符.");
            return loginResultBean;
        }
        String passwordInput = user.getPassword();
        if (passwordInput != null) {
            passwordInput = passwordInput.trim();
        }
        if (ValueWidget.isNullOrEmpty(passwordInput)) {
            loginResultBean.setMessage("请输入密码.");
            return loginResultBean;
        }
        passwordInput = XSSUtil.cleanXSS(passwordInput);
        String username2 = user.getUsername();
        username2 = XSSUtil.cleanXSS(username2);
        GenericUser user1 = null;
        try {
            user1 = userDao.getByName(username2);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("userDao.getByName execute failed.", e);
        }
        if (user1 == null) {
            loginResultBean.setMessage("您输入的用户名不存在.");
            return loginResultBean;
        }

        if (!passwordInput.equals(user1.getPassword())) {
            loginResultBean.setMessage("您输入的密码有误.");
            return loginResultBean;
        }
        loginResultBean.setFailed(false);
        session.setAttribute(Constant2.SESSION_KEY_LOGINED_USER, user1);//登录成功的标识有两个:"user",Constant2.SESSION_KEY_LOGINED_FLAG
        session.setAttribute(Constant2.SESSION_KEY_LOGINED_FLAG, Constant2.FLAG_LOGIN_SUCCESS);//登录成功的标识有两个:"user",Constant2.SESSION_KEY_LOGINED_FLAG
        try {
            WebServletUtil.setSessionIdCookie(request, response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("save JSESSIONID to cookie failed", e);
        }
        model.addAttribute("user", user1);
        boolean isSaveUserName = !ValueWidget.isNullOrEmpty(issaveUserName)
                && issaveUserName.equalsIgnoreCase("save");
        boolean isSavePasswd = !ValueWidget.isNullOrEmpty(issavePasswd)
                && issavePasswd.equalsIgnoreCase("save");
        System.out.println("isSaveUserName:" + isSaveUserName);
//		Cookie[] cookies = request.getCookies();
        Map map = new HashMap();
        if (isSaveUserName) {//记住用户名
            map.put("userEmail", user.getUsername());
        } else {
            map.put("userEmail", false);
        }
        if (isSavePasswd) {//若自动登录,则必定记住密码
            map.put(Constant2.COOKIE_KEY_PASSWORD, user.getPassword());//记住密码
        } else {
            map.put(Constant2.COOKIE_KEY_PASSWORD, false);//记住密码
        }
        WebServletUtil.rememberMe(request, response, map);
//		WebServletUtil.rememberMe(cookies,response,"userEmail", user.getUsername(), isSave);
        return loginResultBean;
    }

        /***
         * 注销
         *
         * @param session
         * @return
         */
    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpSession session, String targetView) {
        logoutCommon(request, session);
        if (!ValueWidget.isNullOrEmpty(targetView)) {
            return targetView;
        }
        return getLogoutReturnUrl();
    }

    public String getLogoutReturnUrl() {
        return "redirect:/";
    }

    /**
     * 进入登录界面
     *
     * @param model
     * @param errorMessage
     * @param targetView
     * @return
     */
    @RequestMapping(value = "/loginInput")
    public String loginInput(Model model, String errorMessage, String targetView) {
        if (!ValueWidget.isNullOrEmpty(errorMessage)) {
            model.addAttribute("info", errorMessage);
            System.out.println("errorMessage:" + errorMessage);
        }
        String is_auto_login = DictionaryParam.get(Constant2.DICTIONARY_GROUP_GLOBAL_SETTING, "is_auto_login");
        if (ValueWidget.isNullOrEmpty(is_auto_login)) {//若没有设置pic_max_size,则采用默认值(Constant2.UPLOAD_SIZE_DEFAULT)
            is_auto_login = String.valueOf(false);
        }
        model.addAttribute("is_auto_login", is_auto_login);
        if (!ValueWidget.isNullOrEmpty(targetView)) {
            return targetView;
        }
        return "user/login";
    }

    @Resource
    public void setUserDao(IUserLoginDao<T> userDao) {
        this.userDao = userDao;
    }

    @Override
    protected void beforeAddInput(Model model, HttpServletRequest request) {

    }

    @Override
    protected void errorDeal(Model model) {

    }

    @Override
    public String getJspFolder() {
        return null;
    }
}
