package javabean.newcredit.creditbank.thread;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javabean.function.database.DbConnection;
import javabean.newcredit.creditbank.dao.TBankCreditRankInfoNewBatchDao;

/**
  * @ClassName: CreditRankWriteThread
  * @Description: 【导入银行资信--OBF评级信息批量导入】 用于从队列中取出数据存入数据库的线程
  * @author Limz
  * @date 2017-7-5 上午9:30:15
  *
 */
public class CreditRankWriteThread extends Thread {

    /**
     * 用于和读取excel的线程间的通讯
     */
    private volatile RatingsImportRecord record;
    private TBankCreditRankInfoNewBatchDao tBankCreditRankInfoNewBatchDao;
    private String version;
    private String userName;
    private String userId;

    /**
      * 创建一个新的实例 CreditRankWriteThread. 
      * @param 线程同步对象
      * @param tBankCreditRankInfoNewBatchDao
      * @param version
      * @param userName
      * @param userId
     */
    public CreditRankWriteThread(RatingsImportRecord record,
            TBankCreditRankInfoNewBatchDao tBankCreditRankInfoNewBatchDao, String version, String userName,
            String userId) {
        super();
        this.record = record;
        this.tBankCreditRankInfoNewBatchDao = tBankCreditRankInfoNewBatchDao;
        this.version = version;
        this.userName = userName;
        this.userId = userId;
        this.setName("CreditRankWriteThread" + UUID.randomUUID().toString() + " in " + Thread.currentThread().getName());
        System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程已创建-----");
    }

    @Override
    public void run() {
        System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程开始运行-----");
        Connection conn = null;
        DbConnection db = new DbConnection();
        try {
            List<Object[]> list = new ArrayList<Object[]>(2002);
            int size = 0;
            conn = db.connectByIplanet();
            while (!record.isReadEnd() || record.hasNext()) {
                if (!record.isSuccess()) {
                    break;
                }
                while (record.hasNext()) {
                    Object[] row = null;
                    row = record.nextRow();
                    list.add(size, row);
                    size++;
                    if (size >= 2000) {
                        tBankCreditRankInfoNewBatchDao.saveBatch(conn, userId, userName, list, version);
                        list.clear();
                        size = 0;
                    }
                }
            }
            if (record.isSuccess()) {
                tBankCreditRankInfoNewBatchDao.saveBatch(conn, userId, userName, list, version);
            }
            list = null;
        } catch (Exception e) {
            System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程出现异常,准备停止-----");
            record.setError(e);
            synchronized (record) {
                record.notifyAll();
            }
        } finally {
            record.clear();
            record.writeEnd();
            if (conn != null) {
                db.disconnect(conn);
            }
            System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程运行完毕-----");
        }
    }
}