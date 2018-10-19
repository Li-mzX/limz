package javabean.newcredit.creditbank.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import javabean.basicinfo.fileupload.FileProperty;
import javabean.newcredit.creditbank.po.TBankCreditRankInfoNewBatch;

public interface TBankCreditRankInfoNewBatchService {

    /**
      * importBankCreditRankInfo(excel批量导入银行评级信息)
      * @param version
      * @param file
      * @param userId 
      * @param userName 
      * @return success/错误日志
      * @author Limz
     */
    String importBankCreditRankInfo(String version, String filePath, String userName, String userId);

    /**
      * getRankInfo(根据条件查询最新的一条评级信息)
      * @param bvdId
      * @return
      * @author Limz
     */
    List<TBankCreditRankInfoNewBatch> getRankInfo(String bvdId);

    /**
      * uploadFile(上传文件)
      * @param request
      * @return
      * @author Limz
     */
    FileProperty uploadFile(HttpServletRequest request);

    
}
