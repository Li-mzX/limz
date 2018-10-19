package javabean.newcredit.creditdreport.util;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import javabean.newcredit.creditdreport.dao.daoimpl.IBaseDaoImpl;

public class HolidayUtils extends IBaseDaoImpl{

    
    private Set<Integer> holidaySet = new HashSet<Integer>();
    private Set<Integer> adjustSet = new HashSet<Integer>();
    private List<Integer> holidayList = new ArrayList<Integer>();
    /**
     * today of week
     */
    private Integer todayW;
    /**
     * today of year
     */
    private Integer todayY;
    
    private Integer maxY;
    
    private Integer days;
    
    private Date start;
    
    
    public Date getDateOfReceipt(Date start, int day, String... countryCode){
        reset();
        this.days = day;
        this.start = start;
        int dn = this.days + 10 + this.days * 2 / 5;
        Calendar ca = Calendar.getInstance();
        ca.setTime(start == null ? new Date(System.currentTimeMillis()) : start);
        this.todayW = ca.get(Calendar.DAY_OF_WEEK);
        this.todayY = ca.get(Calendar.DAY_OF_YEAR);
        this.maxY = this.todayY + dn;
        ca.set(Calendar.DAY_OF_YEAR, execute(dn, countryCode));
        return ca.getTime();
    }
    
    /**
     * 重置参数
      * reset
      * @author Limz
     */
    private void reset(){
        this.holidaySet = new HashSet<Integer>();
        this.adjustSet = new HashSet<Integer>();
        this.holidayList = new ArrayList<Integer>();
        this.todayW = 0;
        this.todayY = 0;
        this.maxY = 0;
        this.days = 0;
        this.start = null;
        
    }
    /**
     * 计算
      * execute
      * @param dn
      * @param countryCode
      * @return
      * @author Limz
     */
    private Integer execute(int dn, String... countryCode){
        getHolidays(dn, countryCode);
        int tarDay = this.todayY;         // 从明天开始算
//      int tarDay = todayY - 1;     // 从今天开始算
        
        Iterator<Integer> it = this.holidayList.iterator();
        
        int holiday = it.hasNext() ? it.next() : 10000;
        for (int i = 0; i < this.days; ) {
            tarDay += 1;
            if (holiday > tarDay) {
                i++;
            }else if (holiday == tarDay) {
                holiday = it.hasNext() ? it.next() : 10000;
            }else {
                holiday = it.hasNext() ? it.next() : 10000;
                tarDay -= 1;
            }
        }
        if (tarDay > this.maxY) {
            this.maxY = tarDay + this.days;
            return execute(this.maxY - this.todayY, countryCode);
        }
        return tarDay;
    }
    /**
     * 获取非工作日
      * getHolidays
      * @param dn
      * @param countryCode
      * @author Limz
     */
    private void getHolidays(int dn, String... countryCode){
        //查询数据库中的节假日
        this.getHolidaysDB(dn, countryCode);
        //记录周末休息日
        for (int i = 0; i < (this.maxY - this.todayY) / 7 + 2; i++) {
            if (!(i == 0 && this.todayW > 1)) {
                this.holidaySet.add(this.todayY + i * 7 - this.todayW + 1);
            }
            this.holidaySet.add(this.todayY + i * 7 - this.todayW + 7);
        }
        //去除 调休日
        boolean flag = true;
        for (String cc : countryCode) {
            if (!"CHN".equals(cc)) {
                flag = false;
            }
        }
        if (flag) {
            this.holidaySet.removeAll(this.adjustSet);
        }
        //排序
        this.holidayList.addAll(this.holidaySet);
        Collections.sort(this.holidayList);
    }
    /**
      * 获取数据库中的节假日
      * getHolidays
      * @param ed
      * @param countryCode
      * @author Limz
     */
    @SuppressWarnings("unchecked")
    private void getHolidaysDB(final int ed, final String... countryCode){
        Calendar ca = Calendar.getInstance();
        try {
            String sql = " SELECT CASE WHEN t.holidayname LIKE '%调休%' THEN 2 ELSE 1 END AS TYPE,t.startdate,t.enddate  FROM t_credit_HOLIDAY t " +
                    " WHERE t.countryno in ('1'";
            for (int i = 0 ;i < countryCode.length;i++) {
                sql +=",?";
            }
            sql += ") AND " +
            		" (t.startdate <= ? AND t.enddate >= ? or t.startdate >= ? AND t.startdate <= (? + ?)) " +
            		"order by t.startdate";
            final String sqlQ = sql;
            final Date date = this.start;
            List<Object[]> result = (List<Object[]>) this.getHibernateTemplate().execute(new HibernateCallback() {
                
                @Override
                public List<Object[]> doInHibernate(Session session) throws HibernateException, SQLException {
                    SQLQuery query = session.createSQLQuery(sqlQ);
                    int i;
                    for (i = 0; i < countryCode.length; i++) {
                        query.setParameter(i, countryCode[i]);
                    }
                    query.setParameter(i, date);
                    query.setParameter(i+1, date);
                    query.setParameter(i+2, date);
                    query.setParameter(i+3, date);
                    query.setParameter(i+4, ed);
                    
                    return query.list();
                }
            });
            Iterator<Object[]> rs = result.iterator();
            while (rs.hasNext()) {
                Object[] next = rs.next();
                int type = ((BigDecimal) next[0]).intValue();
                Date startDate = (Date) next[1];
                ca.setTime(startDate);
                int s = ca.get(Calendar.DAY_OF_YEAR);
                Date endDate = (Date) next[2];
                ca.setTime(endDate);
                int e = ca.get(Calendar.DAY_OF_YEAR);
                for (int j = s; j <= e; j++) {
                    if (j < this.todayY) {
                        continue;
                    }
                    if (type == 1) {
                        this.holidaySet.add(j);
                    }else {
                        this.adjustSet.add(j);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
