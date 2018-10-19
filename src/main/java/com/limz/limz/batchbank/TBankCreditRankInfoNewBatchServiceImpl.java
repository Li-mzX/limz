package javabean.newcredit.creditbank.service.serviceimpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javabean.basicinfo.fileupload.AttrachementInfo;
import javabean.basicinfo.fileupload.FileProperty;
import javabean.basicinfo.fileupload.FileUploadFactory;
import javabean.basicinfo.fileupload.UploadException;
import javabean.basicinfo.fileupload.impl.FileUploadImpl;
import javabean.function.database.DbConnection;
import javabean.newcredit.creditbank.dao.TBankCreditRankInfoNewBatchDao;
import javabean.newcredit.creditbank.po.TBankCreditRankInfoNewBatch;
import javabean.newcredit.creditbank.service.TBankCreditRankInfoNewBatchService;
import javabean.newcredit.creditbank.thread.CreditRankReadThread;
import javabean.newcredit.creditbank.thread.CreditRankWriteThread;
import javabean.newcredit.creditbank.thread.RatingsImportRecord;

public class TBankCreditRankInfoNewBatchServiceImpl implements TBankCreditRankInfoNewBatchService {

    private TBankCreditRankInfoNewBatchDao tBankCreditRankInfoNewBatchDao;

    /**
     * @param tBankCreditRankInfoNewBatchDao
     *            the tBankCreditRankInfoNewBatchDao to set
     */
    public void settBankCreditRankInfoNewBatchDao(TBankCreditRankInfoNewBatchDao tBankCreditRankInfoNewBatchDao) {
        this.tBankCreditRankInfoNewBatchDao = tBankCreditRankInfoNewBatchDao;
    }

    /**
     * importBankCreditRankInfo
     * @Description: �����ļ�·������excel�е���Ϣ���뵽���ݿ��
     * @param filePath �ļ�·��
     * @param userName �û���
     * @param userId    �û�id
     * @return success   ���� ������־
     * @author Limz
     */
    @Override
    public String importBankCreditRankInfo(String version, String filePath, String userName, String userId) {

        String resultMsg = "success";
        //�˶�����Ϊ��д�̵߳�ͬ��������,����¼�̶߳�ȡ״̬�Լ����
        RatingsImportRecord record = new RatingsImportRecord();
        CreditRankReadThread read = new CreditRankReadThread(record, filePath);
        CreditRankWriteThread write = new CreditRankWriteThread(record, tBankCreditRankInfoNewBatchDao, version, userName, userId);
        read.setPriority(10);
        write.setPriority(10);
        read.start();
        write.start();
        while (!record.isWriteEnd() || !record.isReadEnd()) {
            //�ȴ��߳����н���/
        }
        if (!record.isSuccess()) {
            System.out.println("��������������--OBF������Ϣ�������롿 ��������������쳣��׼���ع���");
            tBankCreditRankInfoNewBatchDao.deleteByVersion(version);
            resultMsg = record.getErrorLogs();
        }
        return resultMsg;
    }

    /**
     * uploadFile(�ϴ��ļ�)
     * @param request
     * @return �ļ�·��
     * @author Limz
     */
    @Override
    public FileProperty uploadFile(HttpServletRequest request) {

        DbConnection dbConnection = new DbConnection();
        Connection con = dbConnection.connectByIplanet();

        try {

            FileUploadFactory fileUploadFactory = new FileUploadImpl(con);
            AttrachementInfo attrachementInfo = fileUploadFactory.upload(request, FileUploadFactory.TYPE_CREDIT_BANK);

            @SuppressWarnings("rawtypes")
            List keyList = attrachementInfo.getList();

            if (keyList != null && !keyList.isEmpty()) {
                FileProperty fileProperty = fileUploadFactory.getFileProp(keyList.get(0).toString());
                
                return fileProperty;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new UploadException("�ϴ��ļ�����", e);
            }
            throw new UploadException("�ϴ��ļ�����", e);
        } finally {
            dbConnection.disconnect(con);
        }
        return null;
    }

    @Override
    public List<TBankCreditRankInfoNewBatch> getRankInfo(String bvdId) {
        return this.tBankCreditRankInfoNewBatchDao.getRankInfo(bvdId);
    }
}
