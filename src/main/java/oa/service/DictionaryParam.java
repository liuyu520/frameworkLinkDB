package oa.service;

import com.common.dict.Constant2;
import com.common.util.SystemHWUtil;
import com.string.widget.util.ValueWidget;
import oa.dao.common.CommonDictionaryDao;
import oa.entity.common.CommonDictionary;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 字典服务类
 *
 * @author huangwei
 * @since 2014年9月6日
 */
@Service
@Scope("singleton")
public class DictionaryParam {
    protected static Logger logger = Logger.getLogger(DictionaryParam.class);
    private volatile static Map<String, List<CommonDictionary>> dictionaryMap = new HashMap<String, List<CommonDictionary>>();
    private CommonDictionaryDao commonDictionaryDao;

    public static Map<String, String> get(String groupId) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (!ValueWidget.isNullOrEmpty(dictionaryMap)) {
                for (CommonDictionary dictionary : dictionaryMap.get(groupId)) {
                    map.put(dictionary.getKey2(), dictionary.getValue());
                }
            }
        } catch (Exception e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("get Map Data Error.", e);
//            }
            e.printStackTrace();
            throw new RuntimeException("DictionaryParam error");
        }
        return map;
    }

    /***
     * key 是数据库id
     *
     * @param groupId
     * @return
     */
    public static Map<String, String> getIdValue(String groupId) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (!ValueWidget.isNullOrEmpty(dictionaryMap)) {
                for (CommonDictionary dictionary : dictionaryMap.get(groupId)) {
                    map.put(String.valueOf(dictionary.getId()), dictionary.getValue());
                }
            }
        } catch (Exception e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("get Map Data Error.", e);
//            }
            e.printStackTrace();
            throw new RuntimeException("DictionaryParam error");
        }
        return map;
    }

    public static Map<String, String> getDescriptionMap(String groupId) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (dictionaryMap != null && dictionaryMap.size() > 0) {
                for (CommonDictionary dictionary : dictionaryMap.get(groupId)) {
                    map.put(dictionary.getDescription(), dictionary.getKey2());
                }
            }
        } catch (Exception e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("get DescriptionMap Data Error.", e);
//            }
            e.printStackTrace();
            throw new RuntimeException("DictionaryParam error");
        }
        return map;
    }

    public static Map<Integer, String> getMap(String groupId) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        try {
            if (!ValueWidget.isNullOrEmpty(dictionaryMap)) {
                if (!ValueWidget.isNullOrEmpty(dictionaryMap.get(groupId))) {
                    for (CommonDictionary dictionary : dictionaryMap.get(groupId)) {
                        map.put(Integer.parseInt(dictionary.getKey2()), dictionary.getValue());
                    }
                }
            }
        } catch (Exception e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("get DescriptionMap Data Error.", e);
//            }
            e.printStackTrace();
            throw new RuntimeException("DictionaryParam error");
        }
        return map;
    }

    public static int size() {
        return dictionaryMap.size();
    }

    public static List<CommonDictionary> getList(String groupId) {
        return dictionaryMap.get(groupId);
    }

    public static String get(String groupId, String key) {
        String value = null;
        int count = 1;
        while (ValueWidget.isNullOrEmpty(dictionaryMap) && count > 0) {
            String message = "Dictionary load failed,because dictionaryMap is empty.";
            //当前的线程
            Thread currentThread = Thread.currentThread();
            //当前的线程名称
            String threadName = currentThread.getName();
            StackTraceElement stackElement = currentThread.getStackTrace()[2];
            //当前的方法名
            String methodName = stackElement.getMethodName();
            //当前的文件名
            String filename = stackElement.getFileName();
            int lineNum = stackElement.getLineNumber();
            logger.debug("thread name:\t\t" + threadName);
            logger.debug("file name:\t\t" + filename);
            logger.debug("method name:\t\t" + methodName);
            logger.debug("line number:\t\t" + lineNum);
            logger.debug(message);
//        	return value;
            try {
                Thread.sleep(3000);//等待执行init() 方法
                count--;
                System.out.println("sleep over.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (ValueWidget.isNullOrEmpty(dictionaryMap)) {
            /*try {
        		System.out.println("触发刷新数据字典");
				HttpSocketUtil.wgetStr("http://localhost:8080/tv_mobile/refresh/refresh", null);
			} catch (IOException e) {
				e.printStackTrace();
			}*/
            return null;
        }
        try {
            for (CommonDictionary dictionary : dictionaryMap.get(groupId)) {
                if (dictionary.getKey2().equalsIgnoreCase(key)) {
                    value = dictionary.getValue();
                    break;
                }
            }
        } catch (Exception e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("get Data Error.", e);
//            }
            throw new RuntimeException("DictionaryParam get error", e);
        }
        return value;
    }

    /***
     * 把key转化为int
     *
     * @param groupId
     * @param key
     * @return
     */
    public static int getInt(String groupId, String key) {
        String val = get(groupId, key);
        if (ValueWidget.isNullOrEmpty(val)) {
            return SystemHWUtil.NEGATIVE_ONE;
        } else {
            return Integer.parseInt(val);
        }
    }

    /***
     * @param groupId
     * @param key
     * @return
     */
    public static boolean getBoolean(String groupId, String key) {
        String val = get(groupId, key);
        if (ValueWidget.isNullOrEmpty(val) || !val.equalsIgnoreCase(String.valueOf(true))) {
            return false;
        } else {
            return true;
        }
    }

    public static int getGlobalSettingInt(String key2) {
        String value2 = DictionaryParam.get(Constant2.DICTIONARY_GROUP_GLOBAL_SETTING, key2);
        if (ValueWidget.isNullOrEmpty(value2)) {
            return 10;
        }
        return Integer.parseInt(value2);
    }

    /***
     * 执行完构造方法之后就会执行该方法
     */
//	@PostConstruct
    public void init() {
//       new Thread(new Runnable() {
//		@Override
//		public void run() {
//
//			 try {
//				Thread.sleep(8000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
        System.out.println("初始化字典");
        refresh2();
//		}
//	}).start();
    }

    public void refresh2() {
        try {
            if (null == dictionaryMap) {
                dictionaryMap = new HashMap<String, List<CommonDictionary>>();
            } else if (dictionaryMap.size() > 0) {
                dictionaryMap.clear();
            }
            List<CommonDictionary> dictionaryList = DictionaryParam.this.commonDictionaryDao.getList();
            for (CommonDictionary dictionary : dictionaryList) {
                String groupId = dictionary.getGroupId();
                if (!dictionaryMap.containsKey(groupId)) {
                    dictionaryMap.put(groupId, new ArrayList<CommonDictionary>());
                }
                dictionaryMap.get(groupId).add(dictionary);
            }
        } catch (Exception e) {
//            logger.error("Initialize dictionary error.", e);
            e.printStackTrace();
            throw new RuntimeException("DictionaryParam refresh2 error.");
        }
    }

    public CommonDictionaryDao getCommonDictionaryDao() {
        return commonDictionaryDao;
    }

    @Resource
    public void setCommonDictionaryDao(CommonDictionaryDao commonDictionaryDao) {
        this.commonDictionaryDao = commonDictionaryDao;
    }
}
