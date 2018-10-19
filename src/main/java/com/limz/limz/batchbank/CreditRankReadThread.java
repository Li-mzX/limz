package javabean.newcredit.creditbank.thread;

import java.io.InputStream;
import java.util.UUID;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
  * @ClassName: CreditRankReadThread
  * @Description: 【导入银行资信--OBF评级信息批量导入】用于读取excel 并放入队列的线程
  * @author Limz
  * @date 2017-7-5 上午9:39:01
  *
 */
public class CreditRankReadThread extends Thread {

    /**
     * 用于和写入数据库的线程间的通讯
     */
    private volatile RatingsImportRecord record;
    private String fileName;

    /**
      * 创建一个新的实例 CreditRankReadThread. 
      * @param 线程同步对象
      * @param 文件路径
     */
    public CreditRankReadThread(RatingsImportRecord result, String fileName) {
        super();
        this.record = result;
        this.fileName = fileName;
        this.setName("CreditRankReadThread"+UUID.randomUUID().toString()+"  in "+Thread.currentThread().getName());
        System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程已创建-----");
    }

    @Override
    public void run() {
        System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程开始运行-----");
        try {
            //poi的事件模式读取excel
            OPCPackage p = OPCPackage.open(fileName);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(p);
            org.apache.poi.xssf.eventusermodel.XSSFReader xssfReader = new org.apache.poi.xssf.eventusermodel.XSSFReader(p);
            StylesTable styles = xssfReader.getStylesTable();
            org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator iter = (org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator) xssfReader.getSheetsData();
            InputStream stream = iter.next();

            InputSource sheetSource = new InputSource(stream);
            XMLReader sheetParser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            //此对象用于将数据写入队列
            RatingsImportXlsxHandler handler = new RatingsImportXlsxHandler(styles, strings, record);

            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
            stream.close();
            p.close();
        } catch (Exception e) {
            System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程出现异常,准备停止-----");
            record.setError(e);
            synchronized (record) {
                record.notifyAll();
            }
        } finally {
            record.readEnd();
            System.out.println("【导入银行资信--OBF评级信息批量导入】"+this.getName()+"线程运行完毕-----");
        }
    }

}