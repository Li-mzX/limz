package javabean.newcredit.creditbank.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import javabean.basicinfo.fileupload.FileProperty;
import javabean.newcredit.creditbank.po.TBankCreditRankInfoNewBatch;

public interface TBankCreditRankInfoNewBatchService {

    /**
      * importBankCreditRankInfo(excel������������������Ϣ)
      * @param version
      * @param file
      * @param userId 
      * @param userName 
      * @return success/������־
      * @author Limz
     */
    String importBankCreditRankInfo(String version, String filePath, String userName, String userId);

    /**
      * getRankInfo(����������ѯ���µ�һ��������Ϣ)
      * @param bvdId
      * @return
      * @author Limz
     */
    List<TBankCreditRankInfoNewBatch> getRankInfo(String bvdId);

    /**
      * uploadFile(�ϴ��ļ�)
      * @param request
      * @return
      * @author Limz
     */
    FileProperty uploadFile(HttpServletRequest request);

    
}
