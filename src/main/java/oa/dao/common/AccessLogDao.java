package oa.dao.common;

import com.common.dao.generic.GenericDao;
import com.common.dict.Constant2;
import com.common.util.SystemHWUtil;
import com.string.widget.util.ValueWidget;
import com.time.util.TimeHWUtil;
import oa.bean.statistics.StatisticsBean;
import oa.entity.common.AccessLog;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccessLogDao extends GenericDao<AccessLog> {
    public static final String ACCESSDAY = "accessDay";
    public static final String COLUMN_TIME = "time";

    public long count(String requestTarget) {
        return super.count(Constant2.KEY_REQUESTTARGET, requestTarget);
    }

    public long count(String requestTarget, String day) {
        Map condition = new HashMap();
        condition.put(Constant2.KEY_REQUESTTARGET, requestTarget);
        condition.put("accessDay", day);
        return count(condition);
    }

    /***
     * 仅更新accessDayTime 字段
     *
     * @param id
     * @param accessDayTime
     */
    public void updateAccessDayTime(int id, String accessDayTime) {
        System.out.println("updateAccessDayTime");
        super.getCurrentSession().createQuery("update AccessLog log set accessDayTime=:accessDayTime where log.id=:id and (log.accessDayTime is null or log.accessDayTime='' )")
                .setString("accessDayTime", accessDayTime)
                .setInteger("id", id).executeUpdate();
    }

    /***
     * @param requestTarget
     * @param day            : 格式必须是:yyyy-MM-dd
     * @param statisticsType
     * @param power          : 往前移动几个周期
     * @return
     */
    public StatisticsBean count(String requestTarget, String day, int statisticsType, int power) {
        if (power < 0) {
            throw new RuntimeException("power 必须大于或者等于0");
        }
        StatisticsBean statisticsBean = new StatisticsBean();
        Map condition = new HashMap();
        Criteria criteria = null;
        if (!ValueWidget.isNullOrEmpty(requestTarget)) {
            condition.put(Constant2.KEY_REQUESTTARGET, requestTarget);
        }

        if (statisticsType == Constant2.STATISTICS_TYPE_DAY) {
            if (ValueWidget.isNullOrEmpty(day)) {
                day = TimeHWUtil.formatDateShortEN(new Date());
            }
            condition.put(ACCESSDAY, day);
            criteria = getCriteriaByPage(condition, SystemHWUtil.NEGATIVE_ONE,
                    SystemHWUtil.NEGATIVE_ONE, false);
        } else if (statisticsType == Constant2.STATISTICS_TYPE_WEEK) {
            if (power > 0) {
                try {
                    day = TimeHWUtil.formatDate(TimeHWUtil.getDateBefore(day, 7 * power));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            statisticsBean.setEndDay(day);
            criteria = getCriteria(condition, day, 7);// 一个星期
        } else if (statisticsType == Constant2.STATISTICS_TYPE_MONTH) {
            if (power > 0) {
                try {
                    day = TimeHWUtil.formatDate(TimeHWUtil.getDateBefore(day, 30 * power));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            statisticsBean.setEndDay(day);
            criteria = getCriteria(condition, day, 30);// 一个月
        }
        statisticsBean.setCount(count(criteria));
        return statisticsBean;
    }

    /***
     * 按周或者按月统计
     *
     * @param requestTarget
     * @param day
     * @param statisticsType
     * @param times          :从1开始
     * @return
     */
    public StatisticsBean[] countBatch(String requestTarget, String day, int statisticsType, int times) {
        if (times < 1) {
            throw new RuntimeException("times 必须大于0");
        }
        StatisticsBean[] counts = new StatisticsBean[times];
        if (ValueWidget.isNullOrEmpty(day)) {
            day = TimeHWUtil.formatDateShortEN(new Date());
        }
        for (int i = times; i > 0; i--) {
            StatisticsBean statisticsBean = count(requestTarget, day, statisticsType, i - 1);

            counts[times - i] = statisticsBean;
        }
        return counts;
    }

    private Criteria getCriteria(/* Criteria criteria */Map condition, String day,
                                 int beforeDay) {
        Criteria criteria = getCriteriaByPage(condition,
                SystemHWUtil.NEGATIVE_ONE, SystemHWUtil.NEGATIVE_ONE, false);
        Date now = null;
        try {
            now = TimeHWUtil.getDate4Str(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return criteria.add(Restrictions.between(COLUMN_TIME,
                TimeHWUtil.getSecondBefore(now, beforeDay),
                now.getTime() / 1000));

    }

    /***
     * 激活量<br>
     * 单位:天
     *
     * @param statisticsType
     * @return
     */
    public List<Object[]> getStatisticsActivation(String requestTarget/*,
            int statisticsType*/) {
        Criteria criteria = super.getCurrentSession().createCriteria(clz);
        if (!ValueWidget.isNullOrEmpty(requestTarget)) {
            criteria.add(Restrictions.eq(Constant2.KEY_REQUESTTARGET,
                    requestTarget));
        }
        criteria = criteria.setProjection(
                Projections.projectionList().add(Projections.count("id"))
                        .add(Projections.groupProperty(ACCESSDAY)))
                .addOrder(Order.asc(COLUMN_TIME));
        criteria.setFirstResult(0);//显示前十条
        criteria.setMaxResults(10);
        List list = criteria.list();
        return list;
    }

}
