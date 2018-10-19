package javabean.newcredit.creditreport.util;

import java.util.Date;

import org.hibernate.exception.ConstraintViolationException;

import javabean.newcredit.creditdreport.util.StringUtil;
import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;



/**
  * @ClassName: CreditApplyThread
  * @Description: ���������߳���
  * @author Limz
  * @date 2017-10-16 ����11:10:48
 */
public class CreditApplyThread implements Runnable{

    private volatile CreditApplyCallable callBack;
    
    public CreditApplyThread(CreditApplyCallable callBack) {
        super();
        this.callBack = callBack;
    }
    
    private Boolean running = true;
    
    @Override
    public void run() {
        CreditApplyExecutor executor = SpringContextUtil.getBean("credit_applyExecutor");
        TCreditBatchApplyDetail detail = null;
        CreditApplyParameter param = null;
        while (true) {
            try {
                param = callBack.next();
                if (param == null) {
                    break;
                }
                detail = param.getDetail();
                executor.run(param);
                detail.setSuccess();
                detail.setErrorLog(null);
            } catch (Exception e) {
                boolean flag = true;
                if (e instanceof ConstraintViolationException) {
                    ConstraintViolationException e2 = (ConstraintViolationException) e;
                    if (e2.getErrorCode() == 1) {
                        flag = false;
                    }
                }
                if (flag) {
                    CreditBatchApplyCodeBuilder codeBuild = CreditBatchApplyCodeBuilder.getInstance();
                    synchronized (codeBuild) {
                        codeBuild.backParam(param);
                    }
                    detail.setFaild();
                }else if (StringUtil.isEmptyOrNull(detail.getErrorLog())) {
                    callBack.callBack(detail);
                }else {
                    detail.setFaild();
                }
                
                if (e instanceof CreditBatchApplyException) {
                    detail.setErrorLog(e.getMessage());
                }else {
                    detail.setErrorLog("�����쳣�������Ի���ϵ�ͷ�");
                }
                e.printStackTrace();
            }
            detail.setProcessTime(new Date(System.currentTimeMillis()));
        }
        running = false;
    }
    
    /**
      * �Ƿ����
      * @return
      * @author Limz
     */
    public boolean isEnd() {
        return !running;
        
        
    }
}