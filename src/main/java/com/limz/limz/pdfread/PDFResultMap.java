package javabean.newcredit.creditdreport.util.pdf;

import java.util.ArrayList;
import java.util.List;

/**
  * @ClassName: PDFResultMap
  * @Description: �����Ԫ�أ� ������ȡPDF
  * @author Limz
  * @date 2017-9-14 ����9:58:17
  *
 */
public class PDFResultMap{

    /**
     * ��񼯺�
     */
    private List<Table> result = new ArrayList<Table>();
    
    /**
     * �Ƿ�����
     */
    private boolean neat = false;

    /**
     * ����һ���µı��
     * @param name
     * @return 
     */
    public Table buildTable(String name) {
        Table table = new Table(name,this);
        this.result.add(table);
        return table;
    }

    /**
     * @return �����
     */
    public List<Object> getResult() {
        if (!neat) {
            hackle();
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.size(); i++) {
            sb.append(result.get(i).toString());
        }
        return sb.toString();
    }

    /**
      * hackle ����һ�½������ �����հ׵�Ԫ�����������
      * @author Limz
     */
    public void hackle() {

        for (int i = 0; i < result.size(); i++) {
            Table t = result.get(i);
            t.hackle();
        }
        this.neat = true;
    }
}