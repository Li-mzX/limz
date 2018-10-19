package javabean.newcredit.creditbank.thread;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javabean.function.database.DbConnection;
import javabean.newcredit.creditbank.dao.TBankCreditRankInfoNewBatchDao;

/**
  * @ClassName: CreditRankWriteThread
  * @Description: ��������������--OBF������Ϣ�������롿 ���ڴӶ�����ȡ�����ݴ������ݿ���߳�
  * @author Limz
  * @date 2017-7-5 ����9:30:15
  *
 */
public class CreditRankWriteThread extends Thread {

    /**
     * ���ںͶ�ȡexcel���̼߳��ͨѶ
     */
    private volatile RatingsImportRecord record;
    private TBankCreditRankInfoNewBatchDao tBankCreditRankInfoNewBatchDao;
    private String version;
    private String userName;
    private String userId;

    /**
      * ����һ���µ�ʵ�� CreditRankWriteThread. 
      * @param �߳�ͬ������
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
        System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳��Ѵ���-----");
    }

    @Override
    public void run() {
        System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳̿�ʼ����-----");
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
            System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�̳߳����쳣,׼��ֹͣ-----");
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
            System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳��������-----");
        }
    }
}