package javabean.newcredit.creditbank.thread;


/**
  * @ClassName: RatingsImportRecord 【导入银行资信--OBF评级信息批量导入】 数据容器操作类
  * @Description: 用于实时记录评级导入情况  读线程和写线程间的同步通讯
  * @author Limz
  * @date 2017-7-4 下午5:30:39
  *
 */
public class RatingsImportRecord{


    //错误日志
    private volatile StringBuffer errorLogs;
    //是否成功，默认成功
    private volatile boolean success;
    //容器队列
    private volatile CreditRankImportQueue<Object[]> queue;
    //结束标识
    private volatile boolean readEnd;
    private volatile boolean writeEnd;
    
    /**
      * 创建一个新的实例 RatingsImportRecord. 
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
     * isEnd(是否结束写入)
     * @return
     * @author Limz
    */
   public synchronized boolean isWriteEnd() {
       return writeEnd;
   }


   /**
     * readEnd(结束写入)
     * @author Limz
    */
   public synchronized void writeEnd() {
       this.writeEnd = true;
   }
   
   /**
    * isEnd(是否结束读取)
    * @return
    * @author Limz
   */
  public synchronized boolean isReadEnd() {
      return readEnd;
  }

  /**
    * readEnd(结束读取)
    * @author Limz
   */
  public synchronized void readEnd() {
      this.readEnd = true;
  }

    /**
      * getErrorLogs(错误描述日志)
      * @return
      * @author Limz
     */
    public synchronized String getErrorLogs() {
        return this.errorLogs.toString();
    }
    
    /**
      * isSuccess(是否验证成功，有一条失败就是false  全部成功就是true)
      * @return
      * @author Limz
     */
    public synchronized boolean isSuccess() {
        return this.success;
    }
    
    /**
      * addRow(存入一行)
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
      * nextRow(取出一行)
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
      * hasNext(还有吗)
      * @return
      * @author Limz
     */
    public synchronized boolean hasNext(){
        return this.queue.hasNext();
    }
    
    /**
      * addErrorLogs(增加错误日志)
      * @param string
      * @author Limz
     */
    public synchronized void addErrorLogs(String string) {
        this.errorLogs.append(string);
    }
    /**
      * setFail(验证失败)
      * @param b
      * @author Limz
     */
    public synchronized void setFail() {
        if (this.success) {
            System.out.println("【导入银行资信--OBF评级信息批量导入】导入失败！");
            this.success = false;
        }
    }
    /**
      * clear(读取完毕清理容器)
      * @author Limz
     */
    public synchronized void clear() {
        this.queue.clear();
    }

    /**
      * setError(出现异常信息)
      * @param e
      * @author Limz
     */
    public synchronized void setError(Exception e) {
        this.setFail();
        this.errorLogs = new StringBuffer("功能异常 ,请联系管理员 : " + e.getMessage()+"\n").append(this.errorLogs);
        
    }
}
