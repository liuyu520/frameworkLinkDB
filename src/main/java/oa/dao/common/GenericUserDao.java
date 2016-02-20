package oa.dao.common;

import com.common.dao.generic.GenericDao;
import com.common.dao.interf.IUserLoginDao;
import com.common.entity.user.interf.GenericUser;
import com.string.widget.util.ValueWidget;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.List;


public class GenericUserDao<T extends GenericUser> extends GenericDao<T> implements IUserLoginDao<T> {
    public T getByName(String username) {
        T user = (T) super.getCurrentSession().createCriteria(getClz())
                .add(Restrictions.eq("username", username)).uniqueResult();
        return user;
    }

    /***
     * @param username : 登录账号
     * @param password : 密码
     * @return
     */
    public T getByNameAndPasswd(String username, String password) {
        T user = (T) super.getCurrentSession().createCriteria(getClz())
                .add(Restrictions.eq("username", username))
                .add(Restrictions.eq("password", password)).uniqueResult();
        return user;
    }

    /***
     * @param username
     * @param password
     * @param status   : 0:审核中;1:激活,2:失效(不能登录)
     * @return
     */
    public T get(String username, String password, int status) {
        T user = (T) super.getCurrentSession().createCriteria(getClz())
                .add(Restrictions.eq("username", username))
                .add(Restrictions.eq("password", password))
                .add(Restrictions.eq("status", status)).uniqueResult();
        return user;
    }

    /***
     * 检查用户是否存在
     *
     * @param username
     * @return
     */
    public boolean isExist(String username) {
        GenericUser user = getByName(username);
        if (!ValueWidget.isNullOrEmpty(user) && !ValueWidget.isNullOrEmpty(user.getUsername())) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * 检查用户是否存在
     *
     * @param username
     * @param password
     * @return
     */
    public boolean isExist(String username, String password) {
        GenericUser user = getByNameAndPasswd(username, password);
        if (!ValueWidget.isNullOrEmpty(user) && !ValueWidget.isNullOrEmpty(user.getUsername())) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * 修改会员密码
     *
     * @param userId
     * @param new_password
     */
    public int modifyPass(int userId, String new_password) {
        String hql = "update User u set u.password=:old_password where id=:id";
        return this.getCurrentSession().createQuery(hql).setString("old_password", new_password)
                .setInteger("id", userId)
                .executeUpdate();
    }

    /***
     * 修改会员密码
     *
     * @param userId
     * @param new_password : 新密码
     */
    public int modifyPass(int userId, String old_password, String new_password) {
        String hql = "update User u set u.password=:new_password where id=:id and password=:old_password";
        return this.getCurrentSession().createQuery(hql).setString("new_password", new_password)
                .setString("old_password", old_password)
                .setInteger("id", userId)
                .executeUpdate();
    }

    @Override
    public T getByName(T user) throws Exception {
        return getByName(user.getUsername());
    }

    @Override
    public T getByNameAndPassword(T user2) throws Exception {
        return getByNameAndPasswd(user2.getUsername(), user2.getPassword());
    }

    @Override
    public Serializable add(T user) {
        return super.add(user);
    }

    @Override
    public List<T> find(T user) {
        return super.find(user, false, false);
    }
}
