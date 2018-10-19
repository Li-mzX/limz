package javabean.newcredit.creditdreport.util.pdf;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.util.TextPosition;

/**
  * @ClassName: SingleCharLimzHandle
  * @Description: 处理PDF中的每一个字符
  * @author Limz
  * @date 2017-9-14 上午10:13:43
  *
 */
public class SingleCharLimzHandle implements SingleCharHandle {

    /**
     * 结果集
     */
    private PDFResultMap resultMap;
    /**
     * 当前表格
     */
    private Table nowTable;
    /**
     * 当前行
     */
    private Row nowRow;
    /**
     * 当前单元格
     */
    private Cell nowCell;
    /**
     * 当前字符
     */
    private String nowChar = "";
    /**
     * 上一字符
     */
    private String preChar = "";
    /**
     * 表格的标题
     */
    private StringBuffer title = new StringBuffer();
    /**
     * 上一 TextPosition 对象
     */
    private TextPosition preText;
    /**
     * 当前 TextPosition 对象
     */
    private TextPosition nowText;
    /**
     * 垃圾场
     */
    private Cell dumps = new Cell(0, 0, 0, new Row(0F, null, 0));
    /**
     * 是否出现标题
     */
    private boolean isTitle = false;
    
    /**
     * 正则表达式
     */
    private final String EN_REG = "[a-zA-Z0-9\u0000-\u00FF]";            //数字、26个英文字母 单字节符号
    private final String CN_REG = "[\u4E00-\u9FA5\uFE30-\uFFA0]";   //双字节字符 汉字
    /**
     * 英文正则对象 英文、数字、单字节字符
     */
    private Pattern enPat = Pattern.compile(EN_REG);
    /**
     * 汉字正则对象 汉字、双字节字符
     */
    private Pattern cnPat = Pattern.compile(CN_REG);
    /**
     * 是否出现空格
     */
    private boolean isSpace = false;
    /**
      * 创建一个新的实例 SingleCharLimzHandle. 
      * <p>Description: </p>
     */
    public SingleCharLimzHandle() {
        resultMap = new PDFResultMap();
    }
    
    /**
     * composition
     * 实现接口中 用于处理每一个字符的方法
     * @param text
     * @author Limz
     */
    @Override
    public void composition(TextPosition text) {
        
        //   9.0 页码
        if (text.getFontSize() == 9.0F) {
            return;
        }
        //13.56 标题
        if (text.getFontSize() == 13.56F && !" ".equals(text.toString())) {
            title.append(text.toString());
            isTitle = true;
            return;
        }
        //一些无用的空格 和 文字 
        if (text.getFontSize() == 26.04F || text.getFontSize() == 15.0F || text.getFontSize() == 24.0F) {
            return;
        }
        
        // 12.0 正文  15.96 部分正文间的空格   10.56  空格
        if (text.getFontSize() == 12.0F || text.getFontSize() == 15.96F || text.getFontSize() == 10.56F) {
            
            nowText = text;
            nowChar = text.toString();
            
            getCell().addValue(nowChar,nowText.getX(),nowText.getY());
            
            preText = isSpace ? preText : nowText;
            nowText = null;
            preChar = isSpace ? preChar : nowChar;
            nowChar = "";
        }
        
        
    }
    
    /**
      *  获取接收字符的表格
      * @return table
      * @author Limz
     */
    public Table getTable(){
        if (isTitle) {
            isTitle = false;
            nowTable = resultMap.buildTable(" DomesticReport : " + title.toString());
            title = new StringBuffer();
        }else {
            if (nowTable == null) {
                nowTable = resultMap.buildTable(" DomesticReport ");
            }
        }
        return nowTable;
    }
    /**
      * 获取接收字符的行
      * @return row
      * @author Limz
     */
    public Row getRow(){
        if (isNextRow()) {
            Table table = getTable();
            nowRow = table.buildRow(nowText.getY());
        }
        return nowRow;
    }
    /**
      * 获取接收字符的单元格
      * @return cell
      * @author Limz
     */
    public Cell getCell(){
        if (" ".equals(nowChar) && (nowCell == null || nowCell.getValue().trim().length() == 0)) {
            return dumps;
        }
        if (isNextCell()) {
            Row row = getRow();
            nowCell = row.buildCell(nowText.getX());
        }
        return nowCell;
    }
    
    /**
      * isNextCell 是否下一个单元格
      * @param text
      * @return
      * @author Limz
     */
    private boolean isNextCell() {
        
        //初始化
        if (nowCell == null) {
            return true;
        }
        //出现空格
        if (" ".equals(nowChar)) {
            isSpace = true;
            nowChar = "";
            return false;
            //处理空格之后的字符
        } else if (isSpace) {
            isSpace = false;
            if (preChar.length() > 0) {
                Matcher match = enPat.matcher(preChar);
                //空格之前的字符为英文
                if (match.matches()) {
                    Matcher match1 = cnPat.matcher(nowChar);
                    //空格之后的字符为中文
                    if (match1.matches()) {
                        return true;
                    } else {
                        Matcher match2 = enPat.matcher(nowChar);
                        //空格之后的字符为英文
                        if (match2.matches()) {
                            // 英文字母同一行
                            if (nowText.getX() > preText.getX() && nowText.getY() == preText.getY() && nowText.getX() <preText.getX()+23F) {
                                nowChar = " " + nowChar;
                                return false;
                            //英文字母 另起一行
                            }else if (nowText.getX() == nowCell.getLeft() && nowText.getY() > preText.getY()) {
                                nowChar = " " + nowChar;
                                return false;
                            }else {
                                return true;
                            }
                        }
                    }
                } else {
                    Matcher match1 = cnPat.matcher(preChar);
                    //空格之前的字符是中文 或者是一句话结束
                    if (match1.matches() || preChar.equals("。")) {
                        Matcher match2 = cnPat.matcher(nowChar);
                        //空格之后的字符也是中文
                        if (match2.matches()) {
                            //相差不超过2个字体宽度的间隔，并且在同一行
                            if (nowText.getX() > preText.getX() && nowText.getX()-preText.getX() < 23F && nowText.getY() == preText.getY()) {
                                Map<Integer, Cell> cells = nowRow.getPreRow().getCells();
                                Iterator<Integer> iterator = cells.keySet().iterator();
                                while (iterator.hasNext()) {
                                    Integer key = (Integer) iterator.next();
                                    //上一行这个位置有一个单元格
                                    if (cells.get(key).getLeft() == nowText.getX()) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }
                    }
                }
                //初始状态
            }else {
                return true;
            }
            //没有出现空格
        }else {
            return false;
        }
        //其他未知情况
        return true;
    }

    /**
      * isNextRow 是否换行
      * @param text
      * @return
      * @author Limz
     */
    private boolean isNextRow() {
        //初始化
        if (nowRow == null) {
            return true;
        }
        //当前字符y坐标小于行最低点，并且是同一页  TODO 此处暂定200
        if (nowText.getY() <= nowCell.getBottom() && nowCell.getBottom() - nowText.getY() < 200) {
            return false;
        }
        //当前字符的y坐标 大于行最高点减去一个字体高度的位置 并且 当前字符的y坐标小于行最高点加上一个字体高度的位置
        if (nowText.getY() >= nowRow.getTop()-nowText.getHeight() && nowText.getY() <= nowRow.getTop()+nowText.getHeight()) {
            return false;
        }
        //其他情况
        return true;
    }

    /**
     * 返回解析的结果，暂时没想好返回什么
     * @return TODO
     * @author Limz
     */
    @Override
    public List<Object> getResult() {
        this.resultMap.getResult();
        String result = this.resultMap.toString();
        System.out.println(result);
        return null;
    }
}
