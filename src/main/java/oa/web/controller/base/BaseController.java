package oa.web.controller.base;

import com.common.dict.Constant2;
import oa.dao.common.AccessLogDao;
import oa.entity.common.AccessLog;
import oa.service.DictionaryParam;
import oa.util.LogUtil;
import oa.web.controller.generic.GenericController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/***
 * @param <T>
 * @author Administrator
 */
public abstract class BaseController<T> extends GenericController<T> {
    private AccessLogDao accessLogDao;

    /***
     * 获取客户端真是IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (null != inet) {
                    ipAddress = inet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    /***
     * 进入页面
     *
     * @param request
     */
    protected AccessLog logInto(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_INTO, getJspFolder());
    }

    /***
     * 上传文件
     *
     * @param request
     * @return
     */
    protected AccessLog logUploadFile(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_UPLOAD_FILE, getJspFolder());
    }

    /***
     * 删除记录
     *
     * @param request
     * @return
     */
    protected AccessLog logDelete(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_DELETE, getJspFolder());
    }

    /***
     * 修改记录
     *
     * @param request
     * @return
     */
    protected AccessLog logUpdate(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_UPDATE, getJspFolder());
    }

    /**
     * 增加记录
     *
     * @param request
     * @return
     */
    protected AccessLog logAdd(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_ADD, getJspFolder());
    }

    /***
     * 离开页面
     *
     * @param request
     */
    protected AccessLog logLeave(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LEAVE, getJspFolder());
    }

    /***
     * 用户登录
     *
     * @param request
     * @return
     */
    protected AccessLog logLogin(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LOGIN, getJspFolder());
    }

    /***
     * 用户注销/退出
     *
     * @param request
     * @return
     */
    protected AccessLog logLogout(HttpServletRequest request) {
        return LogUtil.logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LOGOUT, getJspFolder());
    }

    protected void logSave(AccessLog accessLog, HttpServletRequest request) {
        LogUtil.logSave(accessLog, request, getAccessLogDao());
    }

    public AccessLogDao getAccessLogDao() {
        return accessLogDao;
    }

    @Resource
    public void setAccessLogDao(AccessLogDao accessLogDao) {
        this.accessLogDao = accessLogDao;
    }

    @Override
    protected Map<Integer, String> deviceTypePathMap() {
        return DictionaryParam.getMap("device_type_path");
//		return null;
    }
}
