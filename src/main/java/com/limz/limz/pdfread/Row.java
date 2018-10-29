package javabean.newcredit.creditdreport.util.pdf;

import java.util.HashMap;
import java.util.Map;

/**
  * @ClassName: Row
  * @Description: ���Ԫ�أ� ������ȡPDF
  * @author Limz
  * @date 2017-9-14 ����10:03:50
  *
 */
public class Row{

    /**
     * �α�
     */
    private int cursor = 1;
    /**
     * ���кţ��ڱ���е�λ�ã�
     */
    private int index;
    /**
     * ��ߵ�����
     */
    private float top;
    /**
     * ��͵�����
     */
    private float bottom;
    /**
     * ��Ԫ�񼯺�
     */
    private Map<Integer,Cell> cells = new HashMap<Integer, Cell>();
    /**
     * ����
     */
    private int cSize = 0;
    /**
     * ���ڵı��
     */
    private Table table;
    
    /**
      * buildCell ����һ���µĵ�Ԫ��
      * @param ��ʼ x ����
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
      * ����һ���µ�ʵ�� Row. 
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
      * @return ��һ�У�������ڣ�
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
      * �����еĸ߶�
      * @param �µ� y ����
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
     * @return ��ߵ�����
     */
    public float getTop() {
        return top;
    }
    
    /**
     * @return ��͵�����
     */
    public float getBottom() {
        return bottom;
    }
    
    /**
     * @return ��Ԫ�񼯺�
     */
    public Map<Integer, Cell> getCells() {
        return cells;
    }
    
    /**
     * @return ��Ԫ��
     * @param ��Ԫ�����к� 1~csize
     */
    public Cell getCell(Integer index) {
        return cells.get(index);
    }

    /**
     * @return ����
     */
    public int getCSize() {
        return cSize;
    }

    /**
     * @return �������
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
      * �������кź��������
      * @param cursor
      * @param table
      * @author Limz
     */
    public void setIndexAndTable(int cursor, Table table) {
        this.index = cursor;
        this.table = table;
    }
    
    /**
      * ��ָ��λ�����һ���հ׵�Ԫ��
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
     * ��ָ��λ���Ƴ�һ��
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
