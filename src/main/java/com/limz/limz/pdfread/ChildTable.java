package javabean.newcredit.creditdreport.util.pdf;


/**
  * @ClassName: Table
  * @Description: 表格元素， 用来抽取PDF
  * @author Limz
  * @date 2017-9-14 上午10:02:54
  *
 */
public class ChildTable extends Table{

    /**
     * 所属表格
     */
    private Table table;
    
    /**
      * 创建一个新的实例 Table. 
      * <p>Description: </p>
      * @param name
      * @param resultMap
     */
    public ChildTable(String name,Table table) {
        super(name, null);
        this.table = table;
    }

    /**
     * @return 所属表格
     */
    public Table getTable() {
        return table;
    }
    
    /**
      * 添加一整行
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
