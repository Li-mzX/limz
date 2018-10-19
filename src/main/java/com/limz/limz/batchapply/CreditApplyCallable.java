package javabean.newcredit.creditreport.util;

import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;

/**
  * @ClassName: CreditApplyCallBack
  * @Description: ������������ �̻߳ص�����
  * @author Limz
  * @date 2017-10-25 ����3:26:10
  *
 */
public interface CreditApplyCallable {

    /**
      *  ��ȡ��һ��
      * @return
      * @author Limz
     */
    CreditApplyParameter next();
    
    /**
      * callBack ��������ͻ�ع�
      * @param detail
      * @author Limz
     */
    void callBack(TCreditBatchApplyDetail detail);
    
    
}
