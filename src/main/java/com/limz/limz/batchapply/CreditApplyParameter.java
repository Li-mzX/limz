package javabean.newcredit.creditreport.util;

import java.util.Date;

import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;

import com.j2ee.rbac.DAO.UserBasicInfo;


/**
  * @ClassName: CreditApplyParameter
  * @Description: 批量资信申请 参数对象
  * @author Limz
  * @date 2017-10-25 下午3:30:09
 */
public class CreditApplyParameter implements Comparable<CreditApplyParameter>{
    private TCreditBatchApplyDetail detail;
    private UserBasicInfo userInfo;
    private String propertySerialNo;
    private String applyCode;
    private String orderCode;
    private Date srDate;
    private String countryCode;
    
    /**
     * @Description: 覆写比较方法
     * @param target
     * @return
     * @author Limz
     */
    @Override
    public int compareTo(CreditApplyParameter target) {
        if (target == null) {
            throw new NullPointerException("target must is not null !");
        }
        if (target.orderCode == null) {
            throw new NullPointerException("target.orderCode must is not null !");
        }
        String thisPre = this.countryCode;
        String anotherPre = target.countryCode;
        if (!thisPre.equals(anotherPre)) {
            return thisPre.compareTo(anotherPre);
        }
        int thisVal = 0;
        int anotherVal = 0;
        thisVal = Integer.valueOf(this.orderCode.substring(this.orderCode.length() - 5, this.orderCode.length()));
        anotherVal = Integer.valueOf(target.orderCode.substring(target.orderCode.length() - 5, target.orderCode.length()));
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
      * 创建一个新的实例 CreditApplyParameter. 
      * <p>Description: </p>
      * @param detail
      * @param userInfo
      * @param propertySerialNo
      * @param applyCode
      * @param orderCode
     */
    public CreditApplyParameter(String countryCode, TCreditBatchApplyDetail detail, UserBasicInfo userInfo, String propertySerialNo,
            String applyCode, String orderCode, Date srDate) {
        super();
        this.countryCode = countryCode;
        this.detail = detail;
        this.userInfo = userInfo;
        this.propertySerialNo = propertySerialNo;
        this.applyCode = applyCode;
        this.orderCode = orderCode;
        this.srDate = srDate;
    }
    
    /**
     * @return 批量申请明细
     */
    public TCreditBatchApplyDetail getDetail() {
        return detail;
    }
    /**
     * @return 用户信息
     */
    public UserBasicInfo getUserInfo() {
        return userInfo;
    }
    /**
     * @return 渠道号
     */
    public String getPropertySerialNo() {
        return propertySerialNo;
    }
    /**
     * @return 资信申请号
     */
    public String getApplyCode() {
        return applyCode;
    }
    /**
     * @return 委托单号
     */
    public String getOrderCode() {
        return orderCode;
    }
    /**
     * @return 应收到日期
     */
    public Date getSrDate() {
        return srDate;
    }

    /**
     * @param detail the detail to set
     */
    public void setDetail(TCreditBatchApplyDetail detail) {
        this.detail = detail;
    }

    /**
     * @param userInfo the userInfo to set
     */
    public void setUserInfo(UserBasicInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        
        return countryCode;
    }
}
