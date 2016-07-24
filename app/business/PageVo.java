package business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Project: fsp</p>
 * <p>Title: PageVo.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:zhencai.yuan@sunlights.cc">yuanzhencai</a>
 */
public class PageVo<O> {
    //当前页第一条数据游标
    private int index = 0;
    //每页显示到数据条数
    private int pageSize = 0;
    //当前页码
    private int pageNum = 0;
    //总条数
    private int count = 0;
    //分页数据
    private List<O> list = new ArrayList<O>();

    //  @JsonIgnore
    private Map<String, Object> filter = new HashMap<String, Object>();

    public void put(String key, Object value) {
        filter.put(key, value);
    }

    public Object get(String key) {
        return filter.get(key);
    }

    public void clear() {
        filter.clear();
    }

    public int getIndex() {
        return pageNum > 0 ? pageSize * (pageNum - 1) : index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
