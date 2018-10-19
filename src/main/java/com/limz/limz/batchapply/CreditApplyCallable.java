package javabean.newcredit.creditreport.util;

import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;

/**
  * @ClassName: CreditApplyCallBack
  * @Description: 批量资信申请 线程回调函数
  * @author Limz
  * @date 2017-10-25 下午3:26:10
  *
 */
public interface CreditApplyCallable {

    /**
      *  获取下一个
      * @return
      * @author Limz
     */
    CreditApplyParameter next();
    
    /**
      * callBack 因主键冲突回滚
      * @param detail
      * @author Limz
     */
    void callBack(TCreditBatchApplyDetail detail);
    
    
}
