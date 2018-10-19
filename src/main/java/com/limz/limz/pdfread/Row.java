package javabean.newcredit.creditdreport.util.pdf;

import java.util.HashMap;
import java.util.Map;

/**
  * @ClassName: Row
  * @Description: 表格元素， 用来抽取PDF
  * @author Limz
  * @date 2017-9-14 上午10:03:50
  *
 */
public class Row{

    /**
     * 游标
     */
    private int cursor = 1;
    /**
     * 序列号（在表格中的位置）
     */
    private int index;
    /**
     * 最高点坐标
     */
    private float top;
    /**
     * 最低点坐标
     */
    private float bottom;
    /**
     * 单元格集合
     */
    private Map<Integer,Cell> cells = new HashMap<Integer, Cell>();
    /**
     * 长度
     */
    private int cSize = 0;
    /**
     * 所在的表格
     */
    private Table table;
    
    /**
      * buildCell 创建一个新的单元格
      * @param 初始 x 坐标
      * @return
      * @author Limz
     */
    public Cell buildCell(float x){
//        if (cursor == 1) {
//            Row row = this.getPreRow();
//            Map<Integer, Cell> proCells = row.getCells();
//            if (row.getCSize() > 1) {
//                if (x > proCells.get(1).getRight()) {
//                    for (int i = 2; i <= row.getCSize(); i++) {
//                        Cell cellx = proCells.get(i);
//                        if (cellx.getLeft() == x) {
//                            return cellx;
//                        }
//                    }
//                }
//            }
//        }
        
        Cell cell = new Cell(x, top, cursor, this);
        this.cells.put(cursor, cell);
        this.cSize++;
        this.cursor++;
        return cell;
    }
    
    /**
      * 创建一个新的实例 Row. 
      * <p>Description: </p>
      * @param y
      * @param table
      * @param cursor
     */
    public Row(Float y, Table table, int cursor) {
        this.top = y;
        this.bottom = y+8;
        this.table = table;
        this.index = cursor;
    }
    /**
      * getPreRow
      * @return 上一行（如果存在）
      * @author Limz
     */
    public Row getPreRow(){
        Row row = null;
        if (index > 1) {
            return this.table.getRow(index-1);
        }
        
        return row;
    }
    
    /**
      * 调整行的高度
      * @param 新的 y 坐标
      * @author Limz
     */
    public void adjust(float y){
        if (y < top && top - y > 600) {
            bottom = bottom + y;
        }else if (y > bottom) {
            bottom = y;
        }else if (y < top ) {
            top = y;
        }
        
    }
    
    /**
     * @return 最高点坐标
     */
    public float getTop() {
        return top;
    }
    
    /**
     * @return 最低点坐标
     */
    public float getBottom() {
        return bottom;
    }
    
    /**
     * @return 单元格集合
     */
    public Map<Integer, Cell> getCells() {
        return cells;
    }
    
    /**
     * @return 单元格
     * @param 单元格序列号 1~csize
     */
    public Cell getCell(Integer index) {
        return cells.get(index);
    }

    /**
     * @return 长度
     */
    public int getCSize() {
        return cSize;
    }

    /**
     * @return 所属表格
     */
    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < cursor; i++) {
            sb.append(cells.get(i).toString());
        }
        return sb.toString() +" \n";
    }

    /**
      * 更新序列号和所属表格
      * @param cursor
      * @param table
      * @author Limz
     */
    public void setIndexAndTable(int cursor, Table table) {
        this.index = cursor;
        this.table = table;
    }
    
    /**
      * 在指定位置添加一个空白单元格
      * @param index
      * @author Limz
     */
    public void addEmpty(float x, float y, int index){
        Cell cell = new Cell(x, y, index, this);
        cursor++;
        cSize++;
        for (int i = index; i < cursor; i++) {
            Cell cx = cells.remove(i);
            cells.put(i, cell);
            cell = cx;
        }
    }
    
    /**
     * 从指定位置移除一格
     * @param index
     * @author Limz
    */
   public void removeRow(int index){
       cells.remove(index);
       Cell cell;
       for (int i = index+1; i < cursor; i++) {
           cell = cells.remove(i);
           cells.put(i-1, cell);
       }
       cursor--;
       cSize--;
   }
}
