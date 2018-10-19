package javabean.newcredit.creditbank.thread;


/**
  * @ClassName: RatingsImportRecord ��������������--OBF������Ϣ�������롿 ��������������
  * @Description: ����ʵʱ��¼�����������  ���̺߳�д�̼߳��ͬ��ͨѶ
  * @author Limz
  * @date 2017-7-4 ����5:30:39
  *
 */
public class RatingsImportRecord{


    //������־
    private volatile StringBuffer errorLogs;
    //�Ƿ�ɹ���Ĭ�ϳɹ�
    private volatile boolean success;
    //��������
    private volatile CreditRankImportQueue<Object[]> queue;
    //������ʶ
    private volatile boolean readEnd;
    private volatile boolean writeEnd;
    
    /**
      * ����һ���µ�ʵ�� RatingsImportRecord. 
     */
    public RatingsImportRecord() {
        super();
        this.errorLogs = new StringBuffer();
        this.success = true;
        this.queue = CreditRankImportQueue.buildQueue();
        this.readEnd = false;
        this.writeEnd = false;
    }
    
    /**
     * isEnd(�Ƿ����д��)
     * @return
     * @author Limz
    */
   public synchronized boolean isWriteEnd() {
       return writeEnd;
   }


   /**
     * readEnd(����д��)
     * @author Limz
    */
   public synchronized void writeEnd() {
       this.writeEnd = true;
   }
   
   /**
    * isEnd(�Ƿ������ȡ)
    * @return
    * @author Limz
   */
  public synchronized boolean isReadEnd() {
      return readEnd;
  }

  /**
    * readEnd(������ȡ)
    * @author Limz
   */
  public synchronized void readEnd() {
      this.readEnd = true;
  }

    /**
      * getErrorLogs(����������־)
      * @return
      * @author Limz
     */
    public synchronized String getErrorLogs() {
        return this.errorLogs.toString();
    }
    
    /**
      * isSuccess(�Ƿ���֤�ɹ�����һ��ʧ�ܾ���false  ȫ���ɹ�����true)
      * @return
      * @author Limz
     */
    public synchronized boolean isSuccess() {
        return this.success;
    }
    
    /**
      * addRow(����һ��)
      * @param row
      * @author Limz
     * @throws InterruptedException 
     */
    public synchronized boolean addRow(Object[] row){
        while (this.isSuccess() && !this.queue.push(row)) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.notifyAll();
        return true;
    }
    
    /**
      * nextRow(ȡ��һ��)
      * @param row
      * @return
      * @author Limz
     * @throws InterruptedException 
     */
    public synchronized Object[] nextRow(){
        Object[] pop = null;
        while (this.isSuccess() && (pop = this.queue.pop()) == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.notifyAll();
        return pop;
    }
    
    /**
      * hasNext(������)
      * @return
      * @author Limz
     */
    public synchronized boolean hasNext(){
        return this.queue.hasNext();
    }
    
    /**
      * addErrorLogs(���Ӵ�����־)
      * @param string
      * @author Limz
     */
    public synchronized void addErrorLogs(String string) {
        this.errorLogs.append(string);
    }
    /**
      * setFail(��֤ʧ��)
      * @param b
      * @author Limz
     */
    public synchronized void setFail() {
        if (this.success) {
            System.out.println("��������������--OBF������Ϣ�������롿����ʧ�ܣ�");
            this.success = false;
        }
    }
    /**
      * clear(��ȡ�����������)
      * @author Limz
     */
    public synchronized void clear() {
        this.queue.clear();
    }

    /**
      * setError(�����쳣��Ϣ)
      * @param e
      * @author Limz
     */
    public synchronized void setError(Exception e) {
        this.setFail();
        this.errorLogs = new StringBuffer("�����쳣 ,����ϵ����Ա : " + e.getMessage()+"\n").append(this.errorLogs);
        
    }
}
