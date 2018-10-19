package javabean.newcredit.creditdreport.util.pdf;

/**
  * @ClassName: Cell
  * @Description: ���Ԫ�أ� ������ȡPDF
  * @author Limz
  * @date 2017-9-14 ����10:09:22
  *
 */
public class Cell{

    /**
     * ���кţ������е�λ�ã�
     */
    private int index;
    
    /**
     * ��
     */
    private float top;
    /**
     * ��
     */
    private float bottom;
    /**
     * ��
     */
    private float left;
    /**
     * ��
     */
    private float right;
    /**
     * ������
     */
    private Row row;
    /**
     * ����
     */
    private StringBuffer value = new StringBuffer();
    /**
      * ����һ���µ�ʵ�� Cell. 
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
      * @return ��һ�� / null
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
     * @return ��Ԫ���е�����
     */
    public String getValue() {
        return value.toString();
    }

    /**
      * addValue ��Ԫ�������һ���ַ�
      * @param value �ַ�
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
      *  ������Ԫ��λ�á���С
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
     * @return ������
     */
    public Row getRow() {
        return row;
    }

    /**
     * @return ���к�
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * @return ��
     */
    public float getTop() {
        return top;
    }


    /**
     * @return ��
     */
    public float getBottom() {
        return bottom;
    }


    /**
     * @return ��
     */
    public float getLeft() {
        return left;
    }


    /**
     * @return ��
     */
    public float getRight() {
        return right;
    }


    @Override
    public String toString() {
        return "*" + this.getValue()+"\t";
    }
}
