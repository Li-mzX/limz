package javabean.newcredit.creditreport.util;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import sinosure.common.query.QueryUtils;
import sinosure.database.DatabaseException;
import javabean.basicinfo.creditchannel.dao.CreditChannelDao;
import javabean.basicinfo.creditchannel.dao.SetChannelDao;
import javabean.basicinfo.creditchannel.dao.TCreditHolidayDao;
import javabean.basicinfo.creditchannel.po.TCreditHoliday;
import javabean.basicinfo.creditchannel.po.TCreditIcbusiness;
import javabean.basicinfo.creditchannel.po.TCreditInquirychannel;
import javabean.basicinfo.creditchannel.util.CodeBuilder;
import javabean.basicinfo.creditchannel.util.DayComputeUtil;
import javabean.function.database.DbConnection;
import javabean.newcredit.creditdreport.util.StringUtil;

/**
 * @ClassName: CreditBatchApplyCodeBuilder
 * @Description: 批量资信申请 获取代码参数工具类
 * @author Limz
 * @date 2017-10-26 上午10:23:54
 */
public class CreditBatchApplyCodeBuilder {

    private CodeBuilder codebuilder;
    //国内渠道业务
    private CreditChannelDao creditChannelDao;

    private SetChannelDao setchanneldao;

    private TCreditHolidayDao tcreditHolidayDao;

    private ConcurrentMap<String, Queue<CreditApplyParameter>> queueMap = new ConcurrentHashMap<String, Queue<CreditApplyParameter>>();

    private static CreditBatchApplyCodeBuilder builder;

    public synchronized static CreditBatchApplyCodeBuilder getInstance() {
        return builder;
    }
    
    public synchronized static void initApply() {
        if (builder == null)
            builder = SpringContextUtil.getBean("credit_applyCodeBuilder");
        builder.init();
    }
    
    public void init() {
        this.chananMap = new HashMap<String, String>();
        this.endDates = new HashMap<String, Date>();
        this.holidayMap = new HashMap<String, HashSet<String>>();
        this.prefixMap = new HashMap<String, String>();
        this.queueMap = new ConcurrentHashMap<String, Queue<CreditApplyParameter>>();
    }

    /**
     * 线程回滚时调用，将不用的代码返回队列下次使用
     * 
     * @param param
     * @author Limz
     */
    public void backParam(CreditApplyParameter param) {
        synchronized (queueMap) {
            Queue<CreditApplyParameter> queue = queueMap.putIfAbsent(param.getCountryCode(),
                    new PriorityBlockingQueue<CreditApplyParameter>(20));
            if (queue == null) {
                queue = queueMap.get(param.getCountryCode());
            }
            param.setDetail(null);
            queue.add(param);
        }
    }

    private Map<String, String> prefixMap = new HashMap<String, String>();

    /**
     * 获取资信申请参数
     * 
     * @param countryCode
     * @return 参数对象
     * @throws CreditBatchApplyException
     * @author Limz
     */
    public CreditApplyParameter runGetParams(String countryCode) {

        String propertySerialNo = getChanan(countryCode);

        synchronized (queueMap) {
            if (queueMap.containsKey(countryCode)) {
                CreditApplyParameter par;
                
                if ((par = queueMap.get(countryCode).poll()) != null) {
                    return par;
                }
            }
        }

        String orderCode;
        String applyCode;
        if ("NULL".equals(propertySerialNo)) {
            throw new CreditBatchApplyException("国别：" + countryCode + " 渠道获取失败！");
        }
        try {
            String prefixCode = prefixMap.get(countryCode);
            if (prefixCode == null) {
                TCreditIcbusiness tcrediticbusiness = setchanneldao.getBusiness(Integer.valueOf(propertySerialNo));
                TCreditInquirychannel tcreditinquirychannel = creditChannelDao.getCreditChannelInfo(tcrediticbusiness
                        .getChannelNo());
                prefixCode = tcreditinquirychannel.getPrefixCode();
                prefixMap.put(countryCode, prefixCode);
            }
            orderCode = codebuilder.createOrderCode("order", prefixCode).toString();
            if (orderCode == null) {
                throw new Exception(" orderCode is null !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CreditBatchApplyException("委托单号获取失败！" + e.getMessage());
        }
        try {

            applyCode = codebuilder.createApplyCode("applyInfo").toString();
            if (applyCode == null) {
                throw new Exception(" applyCode is null !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CreditBatchApplyException("资信申请号获取失败！" + e.getMessage());
        }
        Date srDate = null;
        try {
            srDate = getDateOfReceipt(propertySerialNo, countryCode);
        } catch (Exception e) {
            srDate = null;
        }
        CreditApplyParameter param = new CreditApplyParameter(countryCode, null, null, propertySerialNo, applyCode, orderCode,
                srDate);
        return param;
    }

    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
    private SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
    private Map<String, HashSet<String>> holidayMap = new HashMap<String, HashSet<String>>();
    private Map<String, Date> endDates = new HashMap<String, Date>();

    /**
     * 获取应收到日期
     * 
     * @param 渠道号
     * @param 买方国家
     * @return
     * @throws Exception
     * @author Limz
     */
    public Date getDateOfReceipt(String serialno, String countryCode) throws Exception {

        TCreditIcbusiness tcrediticbusiness = setchanneldao.getBusiness(Integer.valueOf(serialno));
        int day = tcrediticbusiness == null ? 0 : tcrediticbusiness.getLimitedDay2();

        return getDateOfReceipt(countryCode, tcrediticbusiness.getCountryCode(), day);
    }

    /**
     * 应收到日期
     * 
     * @param 买方国家
     * @param 渠道国家
     * @param 时限
     * @return
     * @throws Exception
     * @author Limz
     */
    public Date getDateOfReceipt(String countryCode, String businessCountryCode, int day) throws Exception {
        int dayx = day * 2;
        int days = dayx + 1;
        Date date = null;
        Date nowDate = format2.parse(format2.format(new Date(System.currentTimeMillis())));
        while (days > dayx) {
            HashSet<String> holidays = getHolidays(countryCode, businessCountryCode, dayx);
            date = format2.parse(format2.format(new Date(System.currentTimeMillis() + 24 * 3600 * 1000)));
            for (int i = 0; i < day;) {
                if (!holidays.contains(format3.format(date))) {
                    i++;
                }
                date.setTime(date.getTime() + 1000 * 3600 * 24);
            }
            days = (int) ((date.getTime() - nowDate.getTime()) / (1000 * 3600 * 24));
            if (days < dayx) {
                break;
            } else {
                dayx = dayx * 2;
                days = dayx + 1;
            }
        }
        return date;
    }

    /**
     * 获取节假日信息
     * 
     * @param 买方国家
     * @param 渠道国家
     * @param 预期天数
     * @return
     * @throws Exception
     * @author Limz
     */
    @SuppressWarnings("unchecked")
    public HashSet<String> getHolidays(String countryCode, String businessCountryCode, int days) throws Exception {
        Date nowEnd = endDates.get(countryCode);
        Date tarEnd = format2.parse(format2.format(new Date(System.currentTimeMillis() + days * 24 * 3600 * 1000)));
        HashSet<String> hSet;
        if (nowEnd == null) {
            hSet = new HashSet<String>();
        } else {
            hSet = holidayMap.get(countryCode);
            if (nowEnd.after(tarEnd)) {
                return hSet;
            }
        }

        DayComputeUtil dcU = new DayComputeUtil();
        Date nowDate = new Date();
        String dateStr = format2.format(nowDate);
        GregorianCalendar gCalendar = dcU.parse(dateStr);
        gCalendar.add(GregorianCalendar.DATE, days);
        String endDateStr = dcU.format(gCalendar);
        List<TCreditHoliday> list1 = tcreditHolidayDao.findHolidays3(countryCode, nowDate, format3.parse(endDateStr),
                "2");
        List<TCreditHoliday> list2 = tcreditHolidayDao.findHolidays3(businessCountryCode, nowDate,
                format3.parse(endDateStr), "2");
        list1.addAll(list2);
        for (int i = 0; i < list1.size(); i++) {
            TCreditHoliday th = list1.get(i);
            Date startDate = th.getStartDate();
            Date endDate = th.getEndDate();
            List<Date> betweenDates = getBetweenDates(startDate, endDate);
            for (Date date : betweenDates) {
                hSet.add(format3.format(date));
            }
        }
        endDates.put(countryCode, format3.parse(endDateStr));
        holidayMap.put(countryCode, hSet);
        return hSet;
    }

    private List<Date> getBetweenDates(Date start, Date end) {
        List<Date> result = new ArrayList<Date>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        do {
            result.add(tempStart.getTime());
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        } while (tempStart.before(tempEnd));
        return result;
    }

    private Map<String, String> chananMap = new HashMap<String, String>();
    /**
     * 默认报告类型：标准报告
     */
    private static final String DEFAULT_REPORTTYPE = "3";

    /**
     * 获取指定国别最高配比渠道
     * 
     * @param countryCode
     * @return propertySerialNo
     * @author Limz
     */
    public String getChanan(String countryCode) {

        if (chananMap.containsKey(countryCode)) {
            return chananMap.get(countryCode);
        }
        try {
            DbConnection db=new DbConnection();
            Connection con=db.connectByIplanet();
            String propertyserialno="";
            String sql="select t1.*, t2.serialNo as PROPERTYSERIALNO ,t3.priority from "+       
            "(select * from T_Credit_ICBusinessProperty  where percent<>'-1' )t1, "+ 
            "(select *  FROM T_Credit_ICBusiness  WHERE state = '1') t2,t_credit_reporttype t3, " +
            "T_Credit_Inquirychannel t4 "+           
            "where t1.channelno=t2.channelno " +
            "AND t4.channelno = t2.channelno AND t4.prefixcode NOT LIKE '%IS%' " +
            "AND t1.countryCode = t2.countryCode(+) "+ 
            "AND t1.reportTypeNo = t2.reportTypeNo(+) "+ 
            "AND t1.reportTypeNo = t3.reportTypeNo "+                                   
            "AND t1.speed = t2.speed(+) "+  
            "and t1.reporttypeno='"+DEFAULT_REPORTTYPE+"' and t1.countrycode='"+countryCode+"' "+
            "order by t1.count desc,t1.percent desc,t1.speed asc ";             
            javabean.sinosure.util.Utility.logMessage(sql);
            Map<String, String> datum = QueryUtils.executeSelectInfo(con, sql);
            if (!datum.isEmpty()) {
                propertyserialno =  datum.get("PROPERTYSERIALNO").toString();         
            }
            if (StringUtil.isEmptyOrNull(propertyserialno)) {
                propertyserialno = "NULL";
            }
            chananMap.put(countryCode, propertyserialno);
            return propertyserialno;
        } catch (DatabaseException e1) {
            return null;
        }
    }

    /**
     * @param codebuilder
     *            the codebuilder to set
     */
    public void setCodebuilder(CodeBuilder codebuilder) {
        this.codebuilder = codebuilder;
    }

    /**
     * @param creditChannelDao
     *            the creditChannelDao to set
     */
    public void setCreditChannelDao(CreditChannelDao creditChannelDao) {
        this.creditChannelDao = creditChannelDao;
    }

    /**
     * @param setchanneldao
     *            the setchanneldao to set
     */
    public void setSetchanneldao(SetChannelDao setchanneldao) {
        this.setchanneldao = setchanneldao;
    }

    /**
     * @param tcreditHolidayDao
     *            the tcreditHolidayDao to set
     */
    public void setTcreditHolidayDao(TCreditHolidayDao tcreditHolidayDao) {
        this.tcreditHolidayDao = tcreditHolidayDao;
    }
}
