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
     * @Description: 根据文件路径，将excel中的信息导入到数据库表
     * @param filePath 文件路径
     * @param userName 用户名
     * @param userId    用户id
     * @return success   或者 错误日志
     * @author Limz
     */
    @Override
    public String importBankCreditRankInfo(String version, String filePath, String userName, String userId) {

        String resultMsg = "success";
        //此对象作为读写线程的同步锁对象,并记录线程读取状态以及结果
        RatingsImportRecord record = new RatingsImportRecord();
        CreditRankReadThread read = new CreditRankReadThread(record, filePath);
        CreditRankWriteThread write = new CreditRankWriteThread(record, tBankCreditRankInfoNewBatchDao, version, userName, userId);
        read.setPriority(10);
        write.setPriority(10);
        read.start();
        write.start();
        while (!record.isWriteEnd() || !record.isReadEnd()) {
            //等待线程运行结束/
        }
        if (!record.isSuccess()) {
            System.out.println("【导入银行资信--OBF评级信息批量导入】 导入结束，数据异常，准备回滚！");
            tBankCreditRankInfoNewBatchDao.deleteByVersion(version);
            resultMsg = record.getErrorLogs();
        }
        return resultMsg;
    }

    /**
     * uploadFile(上传文件)
     * @param request
     * @return 文件路径
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
                throw new UploadException("上传文件出错", e);
            }
            throw new UploadException("上传文件出错", e);
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
