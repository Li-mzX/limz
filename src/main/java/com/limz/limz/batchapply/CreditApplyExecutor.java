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
  * @Description: ��������ִ����
  * @author Limz
  * @date 2017-10-20 ����2:38:31
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
     *  Ĭ���������ͣ���Ծ������
     */
    private static final String DEFAULT_TYPE = "7";
    /**
     * Ĭ�ϼӹ����ͣ���ͨ
     */
    private static final String DEFAULT_APPLYTYPE = "0";
    /**
     * Ĭ�ϱ������ͣ���׼����
     */
    private static final String DEFAULT_REPORTTYPE = "3";
    
    /**
     * ��������
     *  @param callBack ��ȡ����
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
        // ����ί�е��ͱ���
        String orderNo;

        try {
            if (srdate != null) {
                orderNo = creditorderservice.createOrderAndReportNew(tcreditOrder, tcreditReport, srdate);
            }else {
                orderNo = creditorderservice.createOrderAndReport(tcreditOrder, tcreditReport);
            }
        } catch (Exception e) {
            throw new CreditBatchApplyException("����ί�е��ͱ���ʧ�ܣ�" + e.getMessage());
        }
        TCreditOrder tco = creditorderservice.getOrderByOrderNo(orderNo);
        tCreditApply.setTCreditReporttype(reportTypeDao.getReportType(DEFAULT_REPORTTYPE));
        tCreditApply.setTCreditOrder(tco);
        tcreditapplydao.saveOrUpdateTCreditApply(tCreditApply);
        // ����ȴ���������
        try {
            workflowservice.toOrderWaitChannel(waitList, tcreditOrder.getOrderno(), tCreditApply.getUserid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CreditBatchApplyException("������������ʧ�ܣ�" + e.getMessage());
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
            throw new CreditBatchApplyException("�����ʼ�ʧ�ܣ�" + e.getMessage());
        }
        String savepath = (String) mmap.get("filekey");
        tco.setSavepath(savepath);
        tcreditOrderDao.saveOrUpdateTCreditOrder(tco);
    }

    /**
     * ����ҵ������ID��������Ϣ���ί�е���Ϣ
     * 
     * @param serialno
     *            --ҵ������ID
     * @param orderType
     *            ����ί������ 1 ���� 2ԭ�� 3��Ʒ
     * @param orderCode 
     * @return TCreditOrder --������Ϣ
     */
    public TCreditOrder getTCreditOrder(TCreditApply tCreditApply, int serialno, String orderType){
        TCreditOrder tcreditorder = new TCreditOrder();
        Date sysDate = new Date();
        String speed = "";
        TCreditInquirychannel tcreditinquirychannel = new TCreditInquirychannel();
        String reportTypeNo = "";
        // ����ҵ������ID����������,��������,�����ٶ�
        TCreditIcbusiness tcrediticbusiness = setchanneldao.getBusiness(serialno);
        String channelNo = null;
        channelNo = tcrediticbusiness.getChannelNo();
        reportTypeNo = tcrediticbusiness.getReportTypeNo();
        speed = tcrediticbusiness.getSpeed();
        // modify by gaojp 2009-03-27 ��ʱ���֣���,�۸񣬱��֣�����
        sysDate = new Date(System.currentTimeMillis());
        tcreditorder.setMoney(tcrediticbusiness.getMoney());
        tcreditorder.setMoneyId(tcrediticbusiness.getMoneyId());
        tcreditorder.setLimitedDay1(tcrediticbusiness.getLimitedDay1());
        tcreditorder.setLimitedDay2(tcrediticbusiness.getLimitedDay2());
        tcreditorder.setState("2");//�ȴ���������
        tcreditinquirychannel = creditchanneldao.getCreditChannelInfo(channelNo);
        // ת����ǰϵͳʱ��
        tcreditorder.setCreatedate(sysDate);
        // ת����ǰϵͳʱ��
        tcreditorder.setInquirydate(sysDate);
        tcreditorder.setSpeed(speed);
        // ��䱨������
        tcreditorder.setTCreditReporttype(reportTypeDao.getReportType(reportTypeNo));
        // ����������
        tcreditorder.setTCreditInquirychannel(tcreditinquirychannel);
        // ��ù���
        String countryCode = StrTools.BasesBuyernoGetCountyCode(tCreditApply.getBuyerno());
        tcreditorder.setCountrycode(countryCode);
        // ���ί�е���ע
        String remark = Utility.replace(tCreditApply.getOrderdesc());
        tcreditorder.setRemark(remark);
        tcreditorder.setBuyerno(tCreditApply.getBuyerno());
        tcreditorder.setUserid(tCreditApply.getUserid());
        tcreditorder.setNodeid(tCreditApply.getNodeid());
        tcreditorder.setLangtype(tCreditApply.getLangtype());
        return tcreditorder;
    }

    /**
     * ����ί�е���䱨����Ϣ
     * 
     * @param tcreditorder
     *            -ί�е���Ϣ
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
     * ��ȡ������Ϣ
     * 
     * @param applyform
     *            --ҳ�洫�����
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
