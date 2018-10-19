package javabean.newcredit.creditdreport.util.pdf;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.util.TextPosition;

/**
  * @ClassName: SingleCharLimzHandle
  * @Description: ����PDF�е�ÿһ���ַ�
  * @author Limz
  * @date 2017-9-14 ����10:13:43
  *
 */
public class SingleCharLimzHandle implements SingleCharHandle {

    /**
     * �����
     */
    private PDFResultMap resultMap;
    /**
     * ��ǰ���
     */
    private Table nowTable;
    /**
     * ��ǰ��
     */
    private Row nowRow;
    /**
     * ��ǰ��Ԫ��
     */
    private Cell nowCell;
    /**
     * ��ǰ�ַ�
     */
    private String nowChar = "";
    /**
     * ��һ�ַ�
     */
    private String preChar = "";
    /**
     * ���ı���
     */
    private StringBuffer title = new StringBuffer();
    /**
     * ��һ TextPosition ����
     */
    private TextPosition preText;
    /**
     * ��ǰ TextPosition ����
     */
    private TextPosition nowText;
    /**
     * ������
     */
    private Cell dumps = new Cell(0, 0, 0, new Row(0F, null, 0));
    /**
     * �Ƿ���ֱ���
     */
    private boolean isTitle = false;
    
    /**
     * ������ʽ
     */
    private final String EN_REG = "[a-zA-Z0-9\u0000-\u00FF]";            //���֡�26��Ӣ����ĸ ���ֽڷ���
    private final String CN_REG = "[\u4E00-\u9FA5\uFE30-\uFFA0]";   //˫�ֽ��ַ� ����
    /**
     * Ӣ��������� Ӣ�ġ����֡����ֽ��ַ�
     */
    private Pattern enPat = Pattern.compile(EN_REG);
    /**
     * ����������� ���֡�˫�ֽ��ַ�
     */
    private Pattern cnPat = Pattern.compile(CN_REG);
    /**
     * �Ƿ���ֿո�
     */
    private boolean isSpace = false;
    /**
      * ����һ���µ�ʵ�� SingleCharLimzHandle. 
      * <p>Description: </p>
     */
    public SingleCharLimzHandle() {
        resultMap = new PDFResultMap();
    }
    
    /**
     * composition
     * ʵ�ֽӿ��� ���ڴ���ÿһ���ַ��ķ���
     * @param text
     * @author Limz
     */
    @Override
    public void composition(TextPosition text) {
        
        //   9.0 ҳ��
        if (text.getFontSize() == 9.0F) {
            return;
        }
        //13.56 ����
        if (text.getFontSize() == 13.56F && !" ".equals(text.toString())) {
            title.append(text.toString());
            isTitle = true;
            return;
        }
        //һЩ���õĿո� �� ���� 
        if (text.getFontSize() == 26.04F || text.getFontSize() == 15.0F || text.getFontSize() == 24.0F) {
            return;
        }
        
        // 12.0 ����  15.96 �������ļ�Ŀո�   10.56  �ո�
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
      *  ��ȡ�����ַ��ı��
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
      * ��ȡ�����ַ�����
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
      * ��ȡ�����ַ��ĵ�Ԫ��
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
      * isNextCell �Ƿ���һ����Ԫ��
      * @param text
      * @return
      * @author Limz
     */
    private boolean isNextCell() {
        
        //��ʼ��
        if (nowCell == null) {
            return true;
        }
        //���ֿո�
        if (" ".equals(nowChar)) {
            isSpace = true;
            nowChar = "";
            return false;
            //����ո�֮����ַ�
        } else if (isSpace) {
            isSpace = false;
            if (preChar.length() > 0) {
                Matcher match = enPat.matcher(preChar);
                //�ո�֮ǰ���ַ�ΪӢ��
                if (match.matches()) {
                    Matcher match1 = cnPat.matcher(nowChar);
                    //�ո�֮����ַ�Ϊ����
                    if (match1.matches()) {
                        return true;
                    } else {
                        Matcher match2 = enPat.matcher(nowChar);
                        //�ո�֮����ַ�ΪӢ��
                        if (match2.matches()) {
                            // Ӣ����ĸͬһ��
                            if (nowText.getX() > preText.getX() && nowText.getY() == preText.getY() && nowText.getX() <preText.getX()+23F) {
                                nowChar = " " + nowChar;
                                return false;
                            //Ӣ����ĸ ����һ��
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
                    //�ո�֮ǰ���ַ������� ������һ�仰����
                    if (match1.matches() || preChar.equals("��")) {
                        Matcher match2 = cnPat.matcher(nowChar);
                        //�ո�֮����ַ�Ҳ������
                        if (match2.matches()) {
                            //������2�������ȵļ����������ͬһ��
                            if (nowText.getX() > preText.getX() && nowText.getX()-preText.getX() < 23F && nowText.getY() == preText.getY()) {
                                Map<Integer, Cell> cells = nowRow.getPreRow().getCells();
                                Iterator<Integer> iterator = cells.keySet().iterator();
                                while (iterator.hasNext()) {
                                    Integer key = (Integer) iterator.next();
                                    //��һ�����λ����һ����Ԫ��
                                    if (cells.get(key).getLeft() == nowText.getX()) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }
                    }
                }
                //��ʼ״̬
            }else {
                return true;
            }
            //û�г��ֿո�
        }else {
            return false;
        }
        //����δ֪���
        return true;
    }

    /**
      * isNextRow �Ƿ���
      * @param text
      * @return
      * @author Limz
     */
    private boolean isNextRow() {
        //��ʼ��
        if (nowRow == null) {
            return true;
        }
        //��ǰ�ַ�y����С������͵㣬������ͬһҳ  TODO �˴��ݶ�200
        if (nowText.getY() <= nowCell.getBottom() && nowCell.getBottom() - nowText.getY() < 200) {
            return false;
        }
        //��ǰ�ַ���y���� ��������ߵ��ȥһ������߶ȵ�λ�� ���� ��ǰ�ַ���y����С������ߵ����һ������߶ȵ�λ��
        if (nowText.getY() >= nowRow.getTop()-nowText.getHeight() && nowText.getY() <= nowRow.getTop()+nowText.getHeight()) {
            return false;
        }
        //�������
        return true;
    }

    /**
     * ���ؽ����Ľ������ʱû��÷���ʲô
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
