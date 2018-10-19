package javabean.newcredit.creditreport.util;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javabean.basicinfo.creditchannel.dao.CreditChannelDao;
import javabean.basicinfo.creditchannel.dao.ReportTypeDao;
import javabean.basicinfo.creditchannel.dao.SetChannelDao;
import javabean.basicinfo.creditchannel.po.TCreditIcbusiness;
import javabean.basicinfo.creditchannel.po.TCreditInquirychannel;
import javabean.newcredit.creditreport.dao.TCreditApplyDao;
import javabean.newcredit.creditreport.dao.TCreditOrderDao;
import javabean.newcredit.creditreport.po.TCreditApply;
import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;
import javabean.newcredit.creditreport.po.TCreditOrder;
import javabean.newcredit.creditreport.po.TCreditReport;
import javabean.newcredit.creditreport.service.CreditOrderService;
import javabean.newcredit.creditreport.service.MailService;
import javabean.newcredit.creditreport.service.UserService;
import javabean.newcredit.creditreport.service.WorkFlowService;
import javabean.sinosure.util.Utility;
import org.hibernate.Session;
import com.j2ee.rbac.DAO.UserBasicInfo;

/**
  * @ClassName: CreditApplyExecutor
  * @Description: 资信申请执行类
  * @author Limz
  * @date 2017-10-20 下午2:38:31
 */
public class CreditApplyExecutor {
    
    private WorkFlowService workflowservice;

    private UserService userservice;

    private CreditOrderService creditorderservice;

    private MailService mailService;

    private TCreditApplyDao tcreditapplydao;

    private SetChannelDao setchanneldao;

    private TCreditOrderDao tcreditOrderDao;

    private ReportTypeDao reportTypeDao;

    private CreditChannelDao creditchanneldao;

    /**
     *  默认申请类型：活跃买方申请
     */
    private static final String DEFAULT_TYPE = "7";
    /**
     * 默认加工类型：普通
     */
    private static final String DEFAULT_APPLYTYPE = "0";
    /**
     * 默认报告类型：标准报告
     */
    private static final String DEFAULT_REPORTTYPE = "3";
    
    /**
     * 资信申请
     *  @param callBack 获取参数
      * @author Limz
     * @param callBack 
     */
    public void run(CreditApplyParameter param) {

        TCreditBatchApplyDetail detail = param.getDetail();
        UserBasicInfo userInfo = param.getUserInfo();
        String applyCode = param.getApplyCode();
        String propertySerialNo = param.getPropertySerialNo();
        String orderCode = param.getOrderCode();
        Date srdate = param.getSrDate();

        TCreditApply tCreditApply = getTCreditApply(detail);
        tCreditApply.setApplyno(applyCode);
        @SuppressWarnings("rawtypes")
        List waitList = userservice.getVirtualUsers();
        int serialno = Integer.parseInt(propertySerialNo);
        TCreditOrder tcreditOrder = getTCreditOrder(tCreditApply, serialno, "");
        tcreditOrder.setOrderno(orderCode);

        TCreditReport tcreditReport = getTCreditReport(tcreditOrder);
        tCreditApply.setTCreditInquirychannel(tcreditOrder.getTCreditInquirychannel());
        tCreditApply.setState("2");
        tCreditApply.setSpeed(tcreditOrder.getSpeed());
        tcreditapplydao.saveTCreditApply(tCreditApply);
        // 创建委托单和报告
        String orderNo;

        try {
            if (srdate != null) {
                orderNo = creditorderservice.createOrderAndReportNew(tcreditOrder, tcreditReport, srdate);
            }else {
                orderNo = creditorderservice.createOrderAndReport(tcreditOrder, tcreditReport);
            }
        } catch (Exception e) {
            throw new CreditBatchApplyException("创建委托单和报告失败！" + e.getMessage());
        }
        TCreditOrder tco = creditorderservice.getOrderByOrderNo(orderNo);
        tCreditApply.setTCreditReporttype(reportTypeDao.getReportType(DEFAULT_REPORTTYPE));
        tCreditApply.setTCreditOrder(tco);
        tcreditapplydao.saveOrUpdateTCreditApply(tCreditApply);
        // 发起等待渠道流程
        try {
            workflowservice.toOrderWaitChannel(waitList, tcreditOrder.getOrderno(), tCreditApply.getUserid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CreditBatchApplyException("发起渠道流程失败！" + e.getMessage());
        }
        Session sn = tcreditOrderDao.getCSession();
        if (sn != null) {
            sn.flush();
        }
        
        Map<Object, Object> mmap;
        try {
            mmap = mailService
                    .sendInquiryEmail(tco, userInfo.getDeptId(), userInfo.getUserId(), userInfo.getUserName());
        } catch (Exception e) {
            throw new CreditBatchApplyException("发送邮件失败！" + e.getMessage());
        }
        String savepath = (String) mmap.get("filekey");
        tco.setSavepath(savepath);
        tcreditOrderDao.saveOrUpdateTCreditOrder(tco);
    }

    /**
     * 根据业务特征ID和申请信息填充委托单信息
     * 
     * @param serialno
     *            --业务特征ID
     * @param orderType
     *            国内委托类型 1 自行 2原档 3成品
     * @param orderCode 
     * @return TCreditOrder --申请信息
     */
    public TCreditOrder getTCreditOrder(TCreditApply tCreditApply, int serialno, String orderType){
        TCreditOrder tcreditorder = new TCreditOrder();
        Date sysDate = new Date();
        String speed = "";
        TCreditInquirychannel tcreditinquirychannel = new TCreditInquirychannel();
        String reportTypeNo = "";
        // 根据业务特征ID获得渠道编号,报告类型,报告速度
        TCreditIcbusiness tcrediticbusiness = setchanneldao.getBusiness(serialno);
        String channelNo = null;
        channelNo = tcrediticbusiness.getChannelNo();
        reportTypeNo = tcrediticbusiness.getReportTypeNo();
        speed = tcrediticbusiness.getSpeed();
        // modify by gaojp 2009-03-27 加时，分，秒,价格，币种，天期
        sysDate = new Date(System.currentTimeMillis());
        tcreditorder.setMoney(tcrediticbusiness.getMoney());
        tcreditorder.setMoneyId(tcrediticbusiness.getMoneyId());
        tcreditorder.setLimitedDay1(tcrediticbusiness.getLimitedDay1());
        tcreditorder.setLimitedDay2(tcrediticbusiness.getLimitedDay2());
        tcreditorder.setState("2");//等待渠道报告
        tcreditinquirychannel = creditchanneldao.getCreditChannelInfo(channelNo);
        // 转换当前系统时间
        tcreditorder.setCreatedate(sysDate);
        // 转换当前系统时间
        tcreditorder.setInquirydate(sysDate);
        tcreditorder.setSpeed(speed);
        // 填充报告类型
        tcreditorder.setTCreditReporttype(reportTypeDao.getReportType(reportTypeNo));
        // 填充渠道编号
        tcreditorder.setTCreditInquirychannel(tcreditinquirychannel);
        // 获得国别
        String countryCode = StrTools.BasesBuyernoGetCountyCode(tCreditApply.getBuyerno());
        tcreditorder.setCountrycode(countryCode);
        // 获得委托单备注
        String remark = Utility.replace(tCreditApply.getOrderdesc());
        tcreditorder.setRemark(remark);
        tcreditorder.setBuyerno(tCreditApply.getBuyerno());
        tcreditorder.setUserid(tCreditApply.getUserid());
        tcreditorder.setNodeid(tCreditApply.getNodeid());
        tcreditorder.setLangtype(tCreditApply.getLangtype());
        return tcreditorder;
    }

    /**
     * 根据委托单填充报告信息
     * 
     * @param tcreditorder
     *            -委托单信息
     * @return TCreditReport
     */
    public TCreditReport getTCreditReport(TCreditOrder tcreditorder){
        TCreditReport tcreditreport = new TCreditReport();
        tcreditreport.setReportno(tcreditorder.getOrderno());
        tcreditreport.setTCreditOrder(tcreditorder);
        tcreditreport.setTCreditInquirychannel(tcreditorder.getTCreditInquirychannel());
        tcreditreport.setTCreditReporttype(tcreditorder.getTCreditReporttype());
        tcreditreport.setBuyerno(tcreditorder.getBuyerno());
        tcreditreport.setCountrycode(tcreditorder.getCountrycode());
        tcreditreport.setState("0");
        tcreditreport.setSpeed(tcreditorder.getSpeed());
        tcreditreport.setUserid(tcreditorder.getUserid());
        tcreditreport.setNodeid(tcreditorder.getNodeid());
        return tcreditreport;
    }

    /**
     * 获取申请信息
     * 
     * @param applyform
     *            --页面传入参数
     */
    public TCreditApply getTCreditApply(TCreditBatchApplyDetail detail){
        TCreditApply tCreditApply = new TCreditApply();
        tCreditApply.setBuyerno(detail.getBuyerNo());
        tCreditApply.setType(DEFAULT_TYPE);
        tCreditApply.setAccountfirdeptid(detail.getCreditBatchApply().getFirDeptId());
        tCreditApply.setAccountsecdeptid(detail.getCreditBatchApply().getSecDeptId());
        tCreditApply.setApplytype(DEFAULT_APPLYTYPE);
        tCreditApply.setSelectflag("0");
        tCreditApply.setProcesstype(DEFAULT_APPLYTYPE);
        tCreditApply.setUserid(detail.getCreditBatchApply().getUserId());
        tCreditApply.setNodeid(detail.getCreditBatchApply().getFirDeptId());
        tCreditApply.setSpecificflag("0");
        tCreditApply.setApplydate(new Date(System.currentTimeMillis()));
        tCreditApply.setBusinessno(detail.getCreditBatchApply().getBatchNo());
        return tCreditApply;
    }

    /**
     * @param workflowservice the workflowservice to set
     */
    public void setWorkflowservice(WorkFlowService workflowservice) {
        this.workflowservice = workflowservice;
    }

    /**
     * @param userservice the userservice to set
     */
    public void setUserservice(UserService userservice) {
        this.userservice = userservice;
    }

    /**
     * @param creditorderservice the creditorderservice to set
     */
    public void setCreditorderservice(CreditOrderService creditorderservice) {
        this.creditorderservice = creditorderservice;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @param tcreditapplydao the tcreditapplydao to set
     */
    public void setTcreditapplydao(TCreditApplyDao tcreditapplydao) {
        this.tcreditapplydao = tcreditapplydao;
    }

    /**
     * @param setchanneldao the setchanneldao to set
     */
    public void setSetchanneldao(SetChannelDao setchanneldao) {
        this.setchanneldao = setchanneldao;
    }

    /**
     * @param tcreditOrderDao the tcreditOrderDao to set
     */
    public void setTcreditOrderDao(TCreditOrderDao tcreditOrderDao) {
        this.tcreditOrderDao = tcreditOrderDao;
    }

    /**
     * @param reportTypeDao the reportTypeDao to set
     */
    public void setReportTypeDao(ReportTypeDao reportTypeDao) {
        this.reportTypeDao = reportTypeDao;
    }

    /**
     * @param creditchanneldao the creditchanneldao to set
     */
    public void setCreditchanneldao(CreditChannelDao creditchanneldao) {
        this.creditchanneldao = creditchanneldao;
        
        
    }
}
