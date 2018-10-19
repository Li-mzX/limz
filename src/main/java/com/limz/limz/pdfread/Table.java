package javabean.newcredit.creditdreport.util.pdf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
  * @ClassName: Table
  * @Description: 表格元素， 用来抽取PDF
  * @author Limz
  * @date 2017-9-14 上午10:02:54
  *
 */
public class Table{

    /**
     * 表格名字
     */
    protected String name;
    /**
     * 游标
     */
    protected int cursor = 1;
    /**
     * 结果集
     */
    private PDFResultMap resultMap;
    /**
     * 行集合
     */
    protected Map<Integer, Row> rows = new HashMap<Integer, Row>();
    /**
     * 行长度
     */
    protected int rSize = 0;
    
    /**
     * 子表格
     */
    private Map<String, ChildTable> childs = new HashMap<String, ChildTable>();

    /**
      * 创建一个新的实例 Table. 
      * <p>Description: </p>
      * @param name
      * @param resultMap
     */
    public Table(String name,PDFResultMap resultMap) {
        this.name = name;
        this.resultMap = resultMap;
    }

    /**
      * buildRow 新建一行
      * @param 初始 y 坐标
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
      * 创建一个子表格
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
     * 获取指定行    1~ rsize
     * @return the rows
     */
    public Row getRow(Integer index) {
        return rows.get(index);
    }
    
    /**
     * 获取所有行
     * @return the rows
     */
    public Map<Integer, Row> getRows() {
        return rows;
    }

    /**
     * 获取表格的行数
     * @return the rSize
     */
    public int getRSize() {
        return rSize;
    }

    /**
     * 获取表格名字
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     *  获取整个结果集
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
      * 从指定位置移除一行
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
      * 调整表格
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
            //这行的第一格 比 顶行第一格靠左一点，视为二级标题
            if (!begin && nowRow.getCell(1).getLeft() < firstRow.getCell(1).getLeft()-2F) {
                //将之后的row 放入一个子表格
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
                //将第一个长度大于1的行,当做顶行(单元格最全的一行)
            }else if (begin && cSize > 1) {
                firstRow = nowRow;
                begin = false;
            //如果出现一行比顶行单元格多 并且顶行的每一个单元格都能在这行找到与之对齐的，那就给顶行补齐空白单元格
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
        // 如果firstRow 为 null  说明此表格所有行都只有一格，不需要整理
        if (firstRow != null) {
            for (int i = 1; i < cursor; i++) {
                nowRow = rows.get(i);
                cSize = nowRow.getCSize();
                if (begin) {
                    if (cSize == 1 && (nowRow.getCell(1).getLeft() < firstRow.getCell(1).getLeft() - 3F || nowRow.getCell(1).getLeft() > firstRow.getCell(1).getLeft() + 3F)) {
                        //TODO: 标题之后的一些乱七八糟东西 第一个字符和顶行的第一个字符不对齐
                        continue;
                    } else {
                        //出现一个， 第一个字符和顶行对齐的  就把顶行当做这行的上一行  开始整理
                        preRow = firstRow;
                        begin = false;
                    }
                }
                if (!begin) {
                    //开始调整

                    //如果这行的第一个单元格,在 顶行 的第一个单元格之后
                    //并且这行的所有单元格都能在 上一行 找到一个与之对齐的单元格，就把内容加入那个单元格
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
                            //移除该行
                            this.removeRow(i);
                            i--;
                            continue;
                        }
                        //如果这行的第一个单元格和顶行的第一个单元格对齐，并且单元格总数比顶行少，那就在相应的位置补齐空白单元格
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
