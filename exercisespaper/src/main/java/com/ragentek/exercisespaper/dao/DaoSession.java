package com.ragentek.exercisespaper.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.ragentek.exercisespaper.dao.models.Book;
import com.ragentek.exercisespaper.dao.models.Page;
import com.ragentek.exercisespaper.dao.models.Row;
import com.ragentek.exercisespaper.dao.models.User;

import com.ragentek.exercisespaper.dao.BookDao;
import com.ragentek.exercisespaper.dao.PageDao;
import com.ragentek.exercisespaper.dao.RowDao;
import com.ragentek.exercisespaper.dao.UserDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig bookDaoConfig;
    private final DaoConfig pageDaoConfig;
    private final DaoConfig rowDaoConfig;
    private final DaoConfig userDaoConfig;

    private final BookDao bookDao;
    private final PageDao pageDao;
    private final RowDao rowDao;
    private final UserDao userDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        bookDaoConfig = daoConfigMap.get(BookDao.class).clone();
        bookDaoConfig.initIdentityScope(type);

        pageDaoConfig = daoConfigMap.get(PageDao.class).clone();
        pageDaoConfig.initIdentityScope(type);

        rowDaoConfig = daoConfigMap.get(RowDao.class).clone();
        rowDaoConfig.initIdentityScope(type);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        bookDao = new BookDao(bookDaoConfig, this);
        pageDao = new PageDao(pageDaoConfig, this);
        rowDao = new RowDao(rowDaoConfig, this);
        userDao = new UserDao(userDaoConfig, this);

        registerDao(Book.class, bookDao);
        registerDao(Page.class, pageDao);
        registerDao(Row.class, rowDao);
        registerDao(User.class, userDao);
    }
    
    public void clear() {
        bookDaoConfig.clearIdentityScope();
        pageDaoConfig.clearIdentityScope();
        rowDaoConfig.clearIdentityScope();
        userDaoConfig.clearIdentityScope();
    }

    public BookDao getBookDao() {
        return bookDao;
    }

    public PageDao getPageDao() {
        return pageDao;
    }

    public RowDao getRowDao() {
        return rowDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

}
