package javabean.newcredit.creditdreport.util.pdf;

/**
  * @ClassName: Cell
  * @Description: 表格元素， 用来抽取PDF
  * @author Limz
  * @date 2017-9-14 上午10:09:22
  *
 */
public class Cell{

    /**
     * 序列号（在行中的位置）
     */
    private int index;
    
    /**
     * 顶
     */
    private float top;
    /**
     * 底
     */
    private float bottom;
    /**
     * 左
     */
    private float left;
    /**
     * 右
     */
    private float right;
    /**
     * 所属行
     */
    private Row row;
    /**
     * 内容
     */
    private StringBuffer value = new StringBuffer();
    /**
      * 创建一个新的实例 Cell. 
      * <p>Description: </p>
      * @param x
      * @param y
      * @param inx
      * @param row
     */
    public Cell(float x, float y,int inx, Row row) {
        this.top = y;
        this.bottom = y + 8F;
        this.left = x;
        this.right = x + 6F;
        this.index = inx;
        this.row = row;
    }
    /**
      * getPreCell
      * @return 上一格 / null
      * @author Limz
     */
    public Cell getPreCell(){
        Cell cell = null;
        if (index > 1) {
            cell = this.row.getCell(index-1);
        }
        return cell;
    }

    /**
     * @return 单元格中的内容
     */
    public String getValue() {
        return value.toString();
    }

    /**
      * addValue 向单元格中添加一个字符
      * @param value 字符
      * @param x 
      * @param y
      * @author Limz
     */
    public void addValue(String value,float x,float y) {
        this.value.append(value);
        if (!"".equals(value)) {
            this.adjust(x, y);
        }
    }

    /**
      *  调整单元格位置、大小
      * @param x
      * @param y
      * @author Limz
     */
    public void adjust(float x,float y){

        if (y < top && top - y > 600) {
            bottom = bottom + y;
        }else if (y > bottom) {
            bottom = y;
        }else if (y < top ) {
            top = y;
        }
        if (x < left) {
            left = x;
        }else if (x > right) {
            right = x;
        }
        
        this.row.adjust(y);
    }
    
    /**
     * @return 所属行
     */
    public Row getRow() {
        return row;
    }

    /**
     * @return 序列号
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * @return 顶
     */
    public float getTop() {
        return top;
    }


    /**
     * @return 底
     */
    public float getBottom() {
        return bottom;
    }


    /**
     * @return 左
     */
    public float getLeft() {
        return left;
    }


    /**
     * @return 右
     */
    public float getRight() {
        return right;
    }


    @Override
    public String toString() {
        return "*" + this.getValue()+"\t";
    }
}
