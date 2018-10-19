package javabean.newcredit.creditreport.util;

import java.util.Date;

import org.hibernate.exception.ConstraintViolationException;

import javabean.newcredit.creditdreport.util.StringUtil;
import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;



/**
  * @ClassName: CreditApplyThread
  * @Description: 资信申请线程类
  * @author Limz
  * @date 2017-10-16 上午11:10:48
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
                    detail.setErrorLog("申请异常，请重试或联系客服");
                }
                e.printStackTrace();
            }
            detail.setProcessTime(new Date(System.currentTimeMillis()));
        }
        running = false;
    }
    
    /**
      * 是否结束
      * @return
      * @author Limz
     */
    public boolean isEnd() {
        return !running;
        
        
    }
}