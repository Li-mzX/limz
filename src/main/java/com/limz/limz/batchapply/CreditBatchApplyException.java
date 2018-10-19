package javabean.newcredit.creditreport.util;

/**
  * @ClassName: CreditBatchApplyException
  * @Description: 批量资信申请异常
  * @author Limz
  * @date 2017-10-18 下午2:54:48
  *
 */
public class CreditBatchApplyException extends RuntimeException{

    /**
      * @Fields serialVersionUID : TODO
      */
    private static final long serialVersionUID = -1386178096536797796L;

    /**
      * 创建一个新的实例 CreditBatchApplyException. 
      * <p>Description: </p>
      * @param msg
     */
    public CreditBatchApplyException(String msg){
        
        super(msg);
    }
}
