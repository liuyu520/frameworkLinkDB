package oa.dao.common;

import com.common.dao.generic.GenericDao;
import oa.entity.common.CommonDictionary;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommonDictionaryDao extends GenericDao<CommonDictionary> {
    public List<CommonDictionary> getList() {
        return (List<CommonDictionary>) super.getCurrentSession().createCriteria(CommonDictionary.class).addOrder(Order.asc("groupId"))
                .addOrder(Order.asc("key2")).list();//为什么取名"key2",因为"key"是数据库关键字

    }

    public CommonDictionary getDictionary(String groupId, String key) {
        return get("groupId", groupId, "key2", key);
    }
}
