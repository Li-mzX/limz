package javabean.newcredit.creditdreport.util.pdf;


/**
  * @ClassName: Table
  * @Description: ���Ԫ�أ� ������ȡPDF
  * @author Limz
  * @date 2017-9-14 ����10:02:54
  *
 */
public class ChildTable extends Table{

    /**
     * �������
     */
    private Table table;
    
    /**
      * ����һ���µ�ʵ�� Table. 
      * <p>Description: </p>
      * @param name
      * @param resultMap
     */
    public ChildTable(String name,Table table) {
        super(name, null);
        this.table = table;
    }

    /**
     * @return �������
     */
    public Table getTable() {
        return table;
    }
    
    /**
      * ���һ����
      * @param row
      * @return
      * @author Limz
     */
    public Row addRow(Row row){
        this.rows.put(cursor, row);
        row.setIndexAndTable(cursor,this);
        cursor++;
        rSize++;
        return row;
    }
}
