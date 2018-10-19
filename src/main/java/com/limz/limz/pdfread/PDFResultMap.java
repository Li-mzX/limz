package javabean.newcredit.creditdreport.util.pdf;

import java.util.ArrayList;
import java.util.List;

/**
  * @ClassName: PDFResultMap
  * @Description: 结果集元素， 用来抽取PDF
  * @author Limz
  * @date 2017-9-14 上午9:58:17
  *
 */
public class PDFResultMap{

    /**
     * 表格集合
     */
    private List<Table> result = new ArrayList<Table>();
    
    /**
     * 是否整齐
     */
    private boolean neat = false;

    /**
     * 创建一个新的表格
     * @param name
     * @return 
     */
    public Table buildTable(String name) {
        Table table = new Table(name,this);
        this.result.add(table);
        return table;
    }

    /**
     * @return 结果集
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
      * hackle 梳理一下结果集， 调整空白单元格等其他问题
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