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
  * @Description: ��������������--OBF������Ϣ�������롿���ڶ�ȡexcel ��������е��߳�
  * @author Limz
  * @date 2017-7-5 ����9:39:01
  *
 */
public class CreditRankReadThread extends Thread {

    /**
     * ���ں�д�����ݿ���̼߳��ͨѶ
     */
    private volatile RatingsImportRecord record;
    private String fileName;

    /**
      * ����һ���µ�ʵ�� CreditRankReadThread. 
      * @param �߳�ͬ������
      * @param �ļ�·��
     */
    public CreditRankReadThread(RatingsImportRecord result, String fileName) {
        super();
        this.record = result;
        this.fileName = fileName;
        this.setName("CreditRankReadThread"+UUID.randomUUID().toString()+"  in "+Thread.currentThread().getName());
        System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳��Ѵ���-----");
    }

    @Override
    public void run() {
        System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳̿�ʼ����-----");
        try {
            //poi���¼�ģʽ��ȡexcel
            OPCPackage p = OPCPackage.open(fileName);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(p);
            org.apache.poi.xssf.eventusermodel.XSSFReader xssfReader = new org.apache.poi.xssf.eventusermodel.XSSFReader(p);
            StylesTable styles = xssfReader.getStylesTable();
            org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator iter = (org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator) xssfReader.getSheetsData();
            InputStream stream = iter.next();

            InputSource sheetSource = new InputSource(stream);
            XMLReader sheetParser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            //�˶������ڽ�����д�����
            RatingsImportXlsxHandler handler = new RatingsImportXlsxHandler(styles, strings, record);

            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
            stream.close();
            p.close();
        } catch (Exception e) {
            System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�̳߳����쳣,׼��ֹͣ-----");
            record.setError(e);
            synchronized (record) {
                record.notifyAll();
            }
        } finally {
            record.readEnd();
            System.out.println("��������������--OBF������Ϣ�������롿"+this.getName()+"�߳��������-----");
        }
    }

}