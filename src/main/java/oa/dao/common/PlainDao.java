package oa.dao.common;

import com.common.dao.generic.UniversalDao;
import com.common.util.PageUtil;
import com.common.web.view.PageView;
import com.string.widget.util.ValueWidget;
import org.hibernate.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * 通用dao
 *
 * @author huangweii
 *         2015年11月16日<br>
 *         禁止@Component 或@Service注解
 */
//@Component
public class PlainDao extends UniversalDao {
    public PageView query(PageView view, Class clazz) {//TODO 直接调用PageUtil 的方法
        if (ValueWidget.isNullOrEmpty(clazz)) {
            return null;
        }
        Criteria criteria = this.getCurrentSession().createCriteria(clazz);
        int currentPage2 = view.getCurrentPage();
        if (currentPage2 < 1) {
            currentPage2 = 1;
            view.setCurrentPage(1);
        }
        int start = (currentPage2 - 1)
                * view.getRecordsPerPage();
        if (start < 0) {/* org.hibernate.exception.GenericJDBCException: could not execute query] with root cause
java.sql.SQLException: ResultSet may only be accessed in a forward direction.*/
            start = 0;
        }
        int maxRecordsNum = view.getRecordsPerPage();
        paging(criteria, start, maxRecordsNum);
        List list = new ArrayList();
        long count = listByPage(clazz, (Map) null, list, start, view.getRecordsPerPage(), false/*isDistinctRoot*/);
        view.setRecordList(list);
        PageUtil.paging(count, view);
        return view;
    }

}
