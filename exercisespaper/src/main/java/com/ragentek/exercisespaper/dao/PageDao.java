package com.ragentek.exercisespaper.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.ragentek.exercisespaper.dao.models.Page;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PAGE".
*/
public class PageDao extends AbstractDao<Page, Long> {

    public static final String TABLENAME = "PAGE";

    /**
     * Properties of entity Page.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property PageNumber = new Property(1, int.class, "pageNumber", false, "PAGE_NUMBER");
        public final static Property BookId = new Property(2, Long.class, "bookId", false, "BOOK_ID");
        public final static Property Chapter = new Property(3, String.class, "chapter", false, "CHAPTER");
    }

    private DaoSession daoSession;

    private Query<Page> book_PagesQuery;

    public PageDao(DaoConfig config) {
        super(config);
    }
    
    public PageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"PAGE_NUMBER\" INTEGER NOT NULL ," + // 1: pageNumber
                "\"BOOK_ID\" INTEGER NOT NULL ," + // 2: bookId
                "\"CHAPTER\" TEXT);"); // 3: chapter
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PAGE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Page entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getPageNumber());
        stmt.bindLong(3, entity.getBookId());
 
        String chapter = entity.getChapter();
        if (chapter != null) {
            stmt.bindString(4, chapter);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Page entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getPageNumber());
        stmt.bindLong(3, entity.getBookId());
 
        String chapter = entity.getChapter();
        if (chapter != null) {
            stmt.bindString(4, chapter);
        }
    }

    @Override
    protected final void attachEntity(Page entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Page readEntity(Cursor cursor, int offset) {
        Page entity = new Page( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // pageNumber
            cursor.getLong(offset + 2), // bookId
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3) // chapter
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Page entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPageNumber(cursor.getInt(offset + 1));
        entity.setBookId(cursor.getLong(offset + 2));
        entity.setChapter(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Page entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Page entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Page entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "pages" to-many relationship of Book. */
    public List<Page> _queryBook_Pages(Long bookId) {
        synchronized (this) {
            if (book_PagesQuery == null) {
                QueryBuilder<Page> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.BookId.eq(null));
                book_PagesQuery = queryBuilder.build();
            }
        }
        Query<Page> query = book_PagesQuery.forCurrentThread();
        query.setParameter(0, bookId);
        return query.list();
    }

}
