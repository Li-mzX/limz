package javabean.newcredit.creditdreport.util.pdf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
  * @ClassName: Table
  * @Description: ���Ԫ�أ� ������ȡPDF
  * @author Limz
  * @date 2017-9-14 ����10:02:54
  *
 */
public class Table{

    /**
     * �������
     */
    protected String name;
    /**
     * �α�
     */
    protected int cursor = 1;
    /**
     * �����
     */
    private PDFResultMap resultMap;
    /**
     * �м���
     */
    protected Map<Integer, Row> rows = new HashMap<Integer, Row>();
    /**
     * �г���
     */
    protected int rSize = 0;
    
    /**
     * �ӱ��
     */
    private Map<String, ChildTable> childs = new HashMap<String, ChildTable>();

    /**
      * ����һ���µ�ʵ�� Table. 
      * <p>Description: </p>
      * @param name
      * @param resultMap
     */
    public Table(String name,PDFResultMap resultMap) {
        this.name = name;
        this.resultMap = resultMap;
    }

    /**
      * buildRow �½�һ��
      * @param ��ʼ y ����
      * @return
      * @author Limz
     */
    public Row buildRow(float y){
        Row row = new Row(y, this, cursor);
        this.rows.put(cursor, row);
        cursor++;
        rSize++;
        return row;
    }
    
    /**
      * ����һ���ӱ��
      * @param childName
      * @return
      * @author Limz
     */
    public ChildTable buildChildTable(String childName){
        ChildTable child = new ChildTable(name+ " >> " + childName, this);
        this.childs.put(childName, child);
        return child;
    }

    /**
     * ��ȡָ����    1~ rsize
     * @return the rows
     */
    public Row getRow(Integer index) {
        return rows.get(index);
    }
    
    /**
     * ��ȡ������
     * @return the rows
     */
    public Map<Integer, Row> getRows() {
        return rows;
    }

    /**
     * ��ȡ��������
     * @return the rSize
     */
    public int getRSize() {
        return rSize;
    }

    /**
     * ��ȡ�������
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     *  ��ȡ���������
     * @return the resultMap
     */
    public PDFResultMap getResultMap() {
        return resultMap;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + "\n");
        for (int i = 1; i < cursor; i++) {
            if (rows.containsKey(i)) {
                sb.append(rows.get(i).toString());
            }
        }
        Iterator<String> iterator = childs.keySet().iterator();
        while (iterator.hasNext()) {
            ChildTable child = childs.get(iterator.next());
            sb.append(child.toString());
        }
        return sb.toString();
    }

    /**
      * ��ָ��λ���Ƴ�һ��
      * @param index
      * @author Limz
     */
    private void removeRow(int index){
        rows.remove(index);
        Row row;
        for (int i = index+1; i < cursor; i++) {
            row = rows.remove(i);
            rows.put(i-1, row);
        }
        cursor--;
        rSize--;
    }
    
    /**
      * �������
      * @author Limz
     */
    public void hackle() {
        int cSize = 0;
        int preCSize = 0;
        Row nowRow = null;
        Row preRow = null;
        boolean begin = true;
        boolean isChild = false;
        Row firstRow = null;
        ChildTable child = null;
        for (int i = 1; i < cursor; i++) {
            nowRow = rows.get(i);
            cSize = nowRow.getCSize();
            if (begin && preCSize == 0 && cSize == 1) {
                continue;
            }
            //���еĵ�һ�� �� ���е�һ����һ�㣬��Ϊ��������
            if (!begin && nowRow.getCell(1).getLeft() < firstRow.getCell(1).getLeft()-2F) {
                //��֮���row ����һ���ӱ��
//                if (nowRow.getCell(1).getValue().length() == 4) {
                    
                    child = buildChildTable(nowRow.toString().replace("*", "").replace("\t", ""));
                    isChild = true;
                    this.removeRow(i);
                    i--;
//                }
            }else if (isChild) {
                child.addRow(nowRow);
                this.removeRow(i);
                i--;
                //����һ�����ȴ���1����,��������(��Ԫ����ȫ��һ��)
            }else if (begin && cSize > 1) {
                firstRow = nowRow;
                begin = false;
            //�������һ�бȶ��е�Ԫ��� ���Ҷ��е�ÿһ����Ԫ�����������ҵ���֮����ģ��Ǿ͸����в���հ׵�Ԫ��
            } else if (!begin && cSize > firstRow.getCSize()) {
                boolean flag1 = false;
                boolean flag2 = true;
                Integer[] tars = new Integer[firstRow.getCSize()];
                for (int k = 1; k <= firstRow.getCSize(); k++) {
                    Cell cy = firstRow.getCell(k);
                    int ks = k > 1 ? tars[k-2] : k;
                    for (int j = ks; j <= nowRow.getCSize(); j++) {
                        Cell cx = nowRow.getCell(j);
                        if (cx.getLeft() - 6F <= cy.getLeft() && cx.getLeft() + 6F >= cy.getLeft()) {
                            tars[k-1] = j;
                            flag1 = true;
                            break;
                        }
                    }
                    if (!flag1) {
                        flag2 = false;
                        break;
                    }
                    flag1 = false;
                }
                if (flag2) {
                    List<Integer> tarList = Arrays.asList(tars);
                    for (int k = 1; k <= nowRow.getCSize(); k++) {
                        if (!tarList.contains(k)) {
                            Cell cy = nowRow.getCell(k);
                            firstRow.addEmpty(cy.getLeft(), cy.getTop(), k);
                        }
                    }
                }
            
            }
            preCSize = cSize;
        }
        
        cSize = 0;
        preCSize = 0;
        nowRow = null;
        begin = true;
        // ���firstRow Ϊ null  ˵���˱�������ж�ֻ��һ�񣬲���Ҫ����
        if (firstRow != null) {
            for (int i = 1; i < cursor; i++) {
                nowRow = rows.get(i);
                cSize = nowRow.getCSize();
                if (begin) {
                    if (cSize == 1 && (nowRow.getCell(1).getLeft() < firstRow.getCell(1).getLeft() - 3F || nowRow.getCell(1).getLeft() > firstRow.getCell(1).getLeft() + 3F)) {
                        //TODO: ����֮���һЩ���߰��㶫�� ��һ���ַ��Ͷ��еĵ�һ���ַ�������
                        continue;
                    } else {
                        //����һ���� ��һ���ַ��Ͷ��ж����  �ͰѶ��е������е���һ��  ��ʼ����
                        preRow = firstRow;
                        begin = false;
                    }
                }
                if (!begin) {
                    //��ʼ����

                    //������еĵ�һ����Ԫ��,�� ���� �ĵ�һ����Ԫ��֮��
                    //�������е����е�Ԫ������ ��һ�� �ҵ�һ����֮����ĵ�Ԫ�񣬾Ͱ����ݼ����Ǹ���Ԫ��
                    if (nowRow.getCell(1).getLeft() > firstRow.getCell(1).getRight()) {
                        boolean flag1 = false;
                        boolean flag2 = true;
                        Integer[] tars = new Integer[nowRow.getCSize()];
                        for (int k = 1; k <= nowRow.getCSize(); k++) {
                            Cell cy = nowRow.getCell(k);
                            int ks = k > 1 ? tars[k - 2] : k;
                            for (int j = ks + 1; j <= preRow.getCSize(); j++) {
                                Cell cx = preRow.getCell(j);
                                if (cx.getLeft() - 6F <= cy.getLeft() && cx.getLeft() + 6F >= cy.getLeft()) {
                                    tars[k - 1] = j;
                                    flag1 = true;
                                    break;
                                }
                            }
                            if (!flag1) {
                                flag2 = false;
                                break;
                            }
                            flag1 = false;
                        }
                        if (flag2) {
                            for (int k = 1; k <= nowRow.getCSize(); k++) {
                                Cell cy = nowRow.getCell(k);
                                Cell cx = preRow.getCell(tars[k - 1]);
                                if (cx.getLeft() - 6F <= cy.getLeft() && cx.getLeft() + 6F >= cy.getLeft()) {
                                    cx.addValue(" " + cy.getValue(), cy.getLeft(), cy.getBottom());
                                }
                            }
                            //�Ƴ�����
                            this.removeRow(i);
                            i--;
                            continue;
                        }
                        //������еĵ�һ����Ԫ��Ͷ��еĵ�һ����Ԫ����룬���ҵ�Ԫ�������ȶ����٣��Ǿ�����Ӧ��λ�ò���հ׵�Ԫ��
                    } else if (cSize < firstRow.getCSize() && (nowRow.getCell(1).getLeft() > firstRow.getCell(1).getLeft() - 3F || nowRow.getCell(1).getLeft() < firstRow.getCell(1).getLeft() + 3F)) {

                        boolean flag1 = false;
                        boolean flag2 = true;
                        Integer[] tars = new Integer[nowRow.getCSize()];

                        for (int k = 1; k <= nowRow.getCSize(); k++) {
                            Cell cy = nowRow.getCell(k);
                            int ks = k > 1 ? tars[k - 2] : k;
                            for (int j = ks; j <= firstRow.getCSize(); j++) {
                                Cell cx = firstRow.getCell(j);
                                if (cx.getLeft() - 6F <= cy.getLeft() && cx.getLeft() + 6F >= cy.getLeft()) {
                                    tars[k - 1] = j;
                                    flag1 = true;
                                    break;
                                }
                            }
                            if (!flag1) {
                                flag2 = false;
                                break;
                            }
                            flag1 = false;
                        }
                        if (flag2) {
                            List<Integer> tarList = Arrays.asList(tars);
                            for (int k = 1; k <= firstRow.getCSize(); k++) {
                                if (!tarList.contains(k)) {
                                    Cell cy = firstRow.getCell(k);
                                    nowRow.addEmpty(cy.getLeft(), cy.getTop(), k);
                                }
                            }
                        }
                    } else if (isChild) {

                    }
                }
                preCSize = cSize;
                preRow = nowRow;
            }
        }
        Iterator<String> iterator = childs.keySet().iterator();
        while (iterator.hasNext()) {
            childs.get(iterator.next()).hackle();
        }
        
    }
}
