package javabean.newcredit.creditreport.util;

/**
  * @ClassName: CreditBatchApplyException
  * @Description: �������������쳣
  * @author Limz
  * @date 2017-10-18 ����2:54:48
  *
 */
public class CreditBatchApplyException extends RuntimeException{

    /**
      * @Fields serialVersionUID : TODO
      */
    private static final long serialVersionUID = -1386178096536797796L;

    /**
      * ����һ���µ�ʵ�� CreditBatchApplyException. 
      * <p>Description: </p>
      * @param msg
     */
    public CreditBatchApplyException(String msg){
        
        super(msg);
    }
}
