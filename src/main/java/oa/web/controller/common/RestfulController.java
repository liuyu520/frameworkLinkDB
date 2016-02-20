package oa.web.controller.common;

//import com.common.dao.generic.PlainDao;

import com.common.util.SystemHWUtil;
import com.common.web.view.PageView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 * 查询实体类对应的数据
 */
public class RestfulController {
//    private PlainDao plainDao;

    @ResponseBody
    @RequestMapping(value = "/query", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String query(/*Model model,*/ String entityName, PageView view) {
        /*Class clazz;
        try {
            clazz = Class.forName(entityName);
            view = plainDao.query(view, clazz);
            return HWJacksonUtils.getJsonP(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

        return null;
    }

    /*public PlainDao getPlainDao() {
        return plainDao;
    }

//    @Resource
    public void setPlainDao(PlainDao plainDao) {
        this.plainDao = plainDao;
    }*/

}
