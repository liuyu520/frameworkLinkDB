package oa.web.controller.base;

import com.common.bean.ClientOsInfo;
import com.common.dict.Constant2;
import com.common.util.ReflectHWUtils;
import com.common.util.WebServletUtil;
import com.string.widget.util.ValueWidget;
import com.time.util.TimeHWUtil;
import oa.dao.common.AccessLogDao;
import oa.entity.common.AccessLog;
import oa.service.DictionaryParam;
import oa.web.controller.generic.GenericController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

/***
 * @param <T>
 * @author Administrator
 */
public abstract class BaseController<T> extends GenericController<T> {
    private AccessLogDao accessLogDao;

    /***
     * 进入页面
     *
     * @param request
     */
    protected AccessLog logInto(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_INTO);
    }

    /***
     * 上传文件
     *
     * @param request
     * @return
     */
    protected AccessLog logUploadFile(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_UPLOAD_FILE);
    }

    /***
     * 删除记录
     *
     * @param request
     * @return
     */
    protected AccessLog logDelete(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_DELETE);
    }

    /***
     * 修改记录
     *
     * @param request
     * @return
     */
    protected AccessLog logUpdate(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_UPDATE);
    }

    /***
     * 离开页面
     *
     * @param request
     */
    protected AccessLog logLeave(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LEAVE);
    }

    /***
     * 用户登录
     *
     * @param request
     * @return
     */
    protected AccessLog logLogin(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LOGIN);
    }

    /***
     * 用户注销/退出
     *
     * @param request
     * @return
     */
    protected AccessLog logLogout(HttpServletRequest request) {
        return logByMethod(request, Constant2.LOGS_ACCESS_TYPE_LOGOUT);
    }
    protected AccessLog logByMethod(HttpServletRequest request, int accessType) {
        AccessLog accessLog = new AccessLog();
        if (WebServletUtil.isLocalIp(request)) {
            return accessLog;
        }
        accessLog.setAccessType(accessType);//1:访问页面;2:离开页面
        //获得当前的方法的名称
        String methodName = Thread.currentThread().getStackTrace()[3]/*调用logByMethod 的方法logInto上一个方法*/
                .getMethodName();
        accessLog.setRequestTarget(getJspFolder() + ":" + methodName);
        String osType = request.getParameter("osType");
        String deviceId = request.getParameter("deviceId");
        ClientOsInfo info = WebServletUtil.getMobileOsInfo(request);
        accessLog.setUserAgent(info.getUserAgent());
        accessLog.setDeviceType(info.getDeviceType());//Pad或Phone
        if (!ValueWidget.isNullOrEmpty(osType)) {
            accessLog.setOsType(osType);
        } else {
            accessLog.setOsType(info.getOsType());
        }
        if (!ValueWidget.isNullOrEmpty(deviceId)) {
            accessLog.setDeviceId(deviceId);
        }
        return accessLog;
    }

    /***
     * 保存日志到数据库<br>
     * 从session获取用户信息
     *
     * @param accessLog
     * @param request
     */
    protected void logSave(AccessLog accessLog, HttpServletRequest request) {
        if (WebServletUtil.isLocalIp(request)) {
            return;
        }
        if (accessLog == null) {
            accessLog = new AccessLog();
        }
        HttpSession session = request.getSession(true);
        Object user2 = session.getAttribute(Constant2.SESSION_KEY_LOGINED_USER);
        if (!ValueWidget.isNullOrEmpty(user2)) {
            try {
                Object username = ReflectHWUtils.getObjectValue(user2, "username");
                if (!ValueWidget.isNullOrEmpty(username)) {
                    accessLog.setUserId(ReflectHWUtils.getObjectIntValue(user2, "id"));
                    accessLog.setUsername((String) username);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        accessLog.setSessionId(session.getId());
        Date now = new Date();
        accessLog.setTime(TimeHWUtil.getCurrentTimeSecond(now));
        accessLog.setAccessDay(TimeHWUtil.formatDateShortEN(now));
        accessLog.setAccessDayTime(TimeHWUtil.formatDateTime(now));
        String ip = getIpAddress(request);
        System.out.println("client ip:" + ip);
        accessLog.setIp(ip);
        accessLog.setExtranetIp(ip);
        String requestURI = request.getRequestURI();
        accessLog.setRequestURI(requestURI);//例如"/SSLServer/addUser.security"
        if (accessLog.getAccessType() != Constant2.LOGS_ACCESS_TYPE_UPLOAD_FILE) {
            //上传文件,则不记录request的QueryString
            String queryString = request.getQueryString();
            accessLog.setQueryString(queryString);//例如"username=whuang&password=root"
        }

        saveLog(accessLog);
    }

    /**
     * 子类需要覆写
     *
     * @param accessLog
     */
    public void saveLog(AccessLog accessLog) {
        accessLogDao.save(accessLog);
//		logger.error("child class need to override");
    }

    public AccessLogDao getAccessLogDao() {
        return accessLogDao;
    }

    @Resource
    public void setAccessLogDao(AccessLogDao accessLogDao) {
        this.accessLogDao = accessLogDao;
    }

    /***
     * 获取客户端真是IP地址
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
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

    @Override
    protected Map<Integer, String> deviceTypePathMap() {
        return DictionaryParam.getMap("device_type_path");
//		return null;
    }
}
