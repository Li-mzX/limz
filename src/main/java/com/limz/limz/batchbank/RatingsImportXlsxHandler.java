package javabean.newcredit.creditbank.thread;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
  * @ClassName: RatingsImportXlsxHandler
  * @Description: 【导入银行资信--OBF评级信息批量导入】用于读取excel 并存入队列的操作类
  * @author Limz
  * @date 2017-7-5 上午9:41:46
  *
 */
public class RatingsImportXlsxHandler extends DefaultHandler {

    /**
     * 数据值的类型由单元格上的属性指示。这个
     * 值通常在单元格中的“V”元素中。
     */
    static enum xssfDataType {
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
    }

    private RatingsImportRecord record;

    //表格样式
    private StylesTable stylesTable;

    // 具有唯一字符串的表
    private ReadOnlySharedStringsTable sharedStringsTable;

    //总列数
    private final int colCount = 97;

    //读取参数
    private boolean vIsOpen;
    //读取参数，数据类型
    private xssfDataType nextDataType;

    // 用于格式化数值单元格值。
    private short formatIndex;
    private String formatString;
    //当前列数
    private int thisColumn = -1;
    //存值
    private StringBuffer value;
    private Object[] row;

    //阅读起始行（从0 开始计算）
    private final int startRow = 4;

    // 当前行
    private int curRow = 0;

    private Object bankName;
    private Object indexNo;
    private Object BVDId;

    /**
     * excel导入模板
     */
    public static ExcelImportSet[] template = new ExcelImportSet[97];

    static {
        ExcelImportSet set1 = new ExcelImportSet("bankName", 1, 1, Type.str, 400);
        template[0] = set1;
        ExcelImportSet set2 = new ExcelImportSet("indexNumber", 2, 2, Type.str, 20);
        template[1] = set2;
        ExcelImportSet set3 = new ExcelImportSet("BVDIdNumber", 3, 3, Type.str, 100);
        template[2] = set3;
        ExcelImportSet set4 = new ExcelImportSet("frlidrRating", 4, 4, Type.str, 20);
        template[3] = set4;
        ExcelImportSet set5 = new ExcelImportSet("frlidrAction", 5, 5, Type.str, 100);
        template[4] = set5;
        ExcelImportSet set6 = new ExcelImportSet("frlidrChangeDate", 6, 6, Type.date, 7);
        template[5] = set6;
        ExcelImportSet set7 = new ExcelImportSet("frlidrOutlook", 7, 7, Type.str, 20);
        template[6] = set7;
        ExcelImportSet set8 = new ExcelImportSet("frlidrWatchlist", 8, 8, Type.str, 100);
        template[7] = set8;
        ExcelImportSet set9 = new ExcelImportSet("frltrRating", 9, 9, Type.str, 20);
        template[8] = set9;
        ExcelImportSet set10 = new ExcelImportSet("frltrAction", 10, 10, Type.str, 100);
        template[9] = set10;
        ExcelImportSet set11 = new ExcelImportSet("frltrChangeDate", 11, 11, Type.date, 7);
        template[10] = set11;
        ExcelImportSet set12 = new ExcelImportSet("frltrOutlook", 12, 12, Type.str, 20);
        template[11] = set12;
        ExcelImportSet set13 = new ExcelImportSet("frltrWatchlist", 13, 13, Type.str, 100);
        template[12] = set13;
        ExcelImportSet set14 = new ExcelImportSet("frsidrRating", 14, 14, Type.str, 20);
        template[13] = set14;
        ExcelImportSet set15 = new ExcelImportSet("frsidrAction", 15, 15, Type.str, 100);
        template[14] = set15;
        ExcelImportSet set16 = new ExcelImportSet("frsidrChangeDate", 16, 16, Type.date, 7);
        template[15] = set16;
        ExcelImportSet set17 = new ExcelImportSet("frsidrOutlook", 17, 17, Type.str, 20);
        template[16] = set17;
        ExcelImportSet set18 = new ExcelImportSet("frsidrWatchlist", 18, 18, Type.str, 100);
        template[17] = set18;
        ExcelImportSet set19 = new ExcelImportSet("frstrRating", 19, 19, Type.str, 20);
        template[18] = set19;
        ExcelImportSet set20 = new ExcelImportSet("frstrAction", 20, 20, Type.str, 100);
        template[19] = set20;
        ExcelImportSet set21 = new ExcelImportSet("frstrChangeDate", 21, 21, Type.date, 7);
        template[20] = set21;
        ExcelImportSet set22 = new ExcelImportSet("frstrOutlook", 22, 22, Type.str, 20);
        template[21] = set22;
        ExcelImportSet set23 = new ExcelImportSet("frstrWatchlist", 23, 23, Type.str, 100);
        template[22] = set23;
        ExcelImportSet set24 = new ExcelImportSet("frvrRating", 24, 24, Type.str, 20);
        template[23] = set24;
        ExcelImportSet set25 = new ExcelImportSet("frvrAction", 25, 25, Type.str, 100);
        template[24] = set25;
        ExcelImportSet set26 = new ExcelImportSet("frvrChangeDate", 26, 26, Type.date, 7);
        template[25] = set26;
        ExcelImportSet set27 = new ExcelImportSet("frvrOutlook", 27, 27, Type.str, 20);
        template[26] = set27;
        ExcelImportSet set28 = new ExcelImportSet("frvrWatchlist", 28, 28, Type.str, 100);
        template[27] = set28;
        ExcelImportSet set29 = new ExcelImportSet("frsrRating", 29, 29, Type.str, 20);
        template[28] = set29;
        ExcelImportSet set30 = new ExcelImportSet("frsrAction", 30, 30, Type.str, 100);
        template[29] = set30;
        ExcelImportSet set31 = new ExcelImportSet("frsrChangeDate", 31, 31, Type.date, 7);
        template[30] = set31;
        ExcelImportSet set32 = new ExcelImportSet("frsrOutlook", 32, 32, Type.str, 20);
        template[31] = set32;
        ExcelImportSet set33 = new ExcelImportSet("frsrWatchlist", 33, 33, Type.str, 100);
        template[32] = set33;
        ExcelImportSet set34 = new ExcelImportSet("mdsltrRating", 34, 34, Type.str, 20);
        template[33] = set34;
        ExcelImportSet set35 = new ExcelImportSet("mdsltrAction", 35, 35, Type.str, 100);
        template[34] = set35;
        ExcelImportSet set36 = new ExcelImportSet("mdsltrDate", 36, 36, Type.date, 7);
        template[35] = set36;
        ExcelImportSet set37 = new ExcelImportSet("mdsltrWatchlist", 37, 37, Type.str, 100);
        template[36] = set37;
        ExcelImportSet set38 = new ExcelImportSet("mdsltrWatchlistDate", 38, 38, Type.date, 7);
        template[37] = set38;
        ExcelImportSet set39 = new ExcelImportSet("mdsltrDetail", 39, 39, Type.str, 300);
        template[38] = set39;
        ExcelImportSet set40 = new ExcelImportSet("mdsstrRating", 40, 40, Type.str, 20);
        template[39] = set40;
        ExcelImportSet set41 = new ExcelImportSet("mdsstrAction", 41, 41, Type.str, 100);
        template[40] = set41;
        ExcelImportSet set42 = new ExcelImportSet("mdsstrDate", 42, 42, Type.date, 7);
        template[41] = set42;
        ExcelImportSet set43 = new ExcelImportSet("mdsstrWatchlist", 43, 43, Type.str, 100);
        template[42] = set43;
        ExcelImportSet set44 = new ExcelImportSet("mdsstrWatchlistDate", 44, 44, Type.date, 7);
        template[43] = set44;
        ExcelImportSet set45 = new ExcelImportSet("mdsstrDetail", 45, 45, Type.str, 300);
        template[44] = set45;
        ExcelImportSet set46 = new ExcelImportSet("mdssuRating", 46, 46, Type.str, 20);
        template[45] = set46;
        ExcelImportSet set47 = new ExcelImportSet("mdssuAction", 47, 47, Type.str, 100);
        template[46] = set47;
        ExcelImportSet set48 = new ExcelImportSet("mdssuDate", 48, 48, Type.date, 7);
        template[47] = set48;
        ExcelImportSet set49 = new ExcelImportSet("mdssuWatchlist", 49, 49, Type.str, 100);
        template[48] = set49;
        ExcelImportSet set50 = new ExcelImportSet("mdssuWatchlistDate", 50, 50, Type.date, 7);
        template[49] = set50;
        ExcelImportSet set51 = new ExcelImportSet("mdssuDetail", 51, 51, Type.str, 300);
        template[50] = set51;
        ExcelImportSet set52 = new ExcelImportSet("mdsOutlook", 52, 52, Type.str, 20);
        template[51] = set52;
        ExcelImportSet set53 = new ExcelImportSet("mdsOutlookDate", 53, 53, Type.date, 7);
        template[52] = set53;
        ExcelImportSet set54 = new ExcelImportSet("mdsbcaRating", 54, 54, Type.str, 20);
        template[53] = set54;
        ExcelImportSet set55 = new ExcelImportSet("mdsbcaAction", 55, 55, Type.str, 100);
        template[54] = set55;
        ExcelImportSet set56 = new ExcelImportSet("mdsbcaDate", 56, 56, Type.date, 7);
        template[55] = set56;
        ExcelImportSet set57 = new ExcelImportSet("mdsbcaWatchlist", 57, 57, Type.str, 100);
        template[56] = set57;
        ExcelImportSet set58 = new ExcelImportSet("mdsbcaWatchlistDate", 58, 58, Type.date, 7);
        template[57] = set58;
        ExcelImportSet set59 = new ExcelImportSet("mdsabcaRating", 59, 59, Type.str, 20);
        template[58] = set59;
        ExcelImportSet set60 = new ExcelImportSet("mdsabcaAction", 60, 60, Type.str, 100);
        template[59] = set60;
        ExcelImportSet set61 = new ExcelImportSet("mdsabcaDate", 61, 61, Type.date, 7);
        template[60] = set61;
        ExcelImportSet set62 = new ExcelImportSet("mdsabcaWatchlist", 62, 62, Type.str, 100);
        template[61] = set62;
        ExcelImportSet set63 = new ExcelImportSet("mdsabcaWatchlistDate", 63, 63, Type.date, 7);
        template[62] = set63;
        ExcelImportSet set64 = new ExcelImportSet("mdscralRating", 64, 64, Type.str, 20);
        template[63] = set64;
        ExcelImportSet set65 = new ExcelImportSet("mdscralAction", 65, 65, Type.str, 100);
        template[64] = set65;
        ExcelImportSet set66 = new ExcelImportSet("mdscralDate", 66, 66, Type.date, 7);
        template[65] = set66;
        ExcelImportSet set67 = new ExcelImportSet("mdscralWatchlist", 67, 67, Type.str, 100);
        template[66] = set67;
        ExcelImportSet set68 = new ExcelImportSet("mdscralWatchlistDate", 68, 68, Type.date, 7);
        template[67] = set68;
        ExcelImportSet set69 = new ExcelImportSet("mdscrasRating", 69, 69, Type.str, 20);
        template[68] = set69;
        ExcelImportSet set70 = new ExcelImportSet("mdscrasAction", 70, 70, Type.str, 100);
        template[69] = set70;
        ExcelImportSet set71 = new ExcelImportSet("mdscrasDate", 71, 71, Type.date, 7);
        template[70] = set71;
        ExcelImportSet set72 = new ExcelImportSet("mdscrasWatchlist", 72, 72, Type.str, 100);
        template[71] = set72;
        ExcelImportSet set73 = new ExcelImportSet("mdscrasWatchlistDate", 73, 73, Type.date, 7);
        template[72] = set73;
        ExcelImportSet set74 = new ExcelImportSet("spiflRating", 74, 74, Type.str, 20);
        template[73] = set74;
        ExcelImportSet set75 = new ExcelImportSet("spiflDate", 75, 75, Type.date, 7);
        template[74] = set75;
        ExcelImportSet set76 = new ExcelImportSet("spiflOutlook", 76, 76, Type.str, 20);
        template[75] = set76;
        ExcelImportSet set77 = new ExcelImportSet("spiflOutlookDate", 77, 77, Type.date, 7);
        template[76] = set77;
        ExcelImportSet set78 = new ExcelImportSet("spiflCreditWatch", 78, 78, Type.str, 100);
        template[77] = set78;
        ExcelImportSet set79 = new ExcelImportSet("spiflCreditWatchDate", 79, 79, Type.date, 7);
        template[78] = set79;
        ExcelImportSet set80 = new ExcelImportSet("spillRating", 80, 80, Type.str, 20);
        template[79] = set80;
        ExcelImportSet set81 = new ExcelImportSet("spillDate", 81, 81, Type.date, 7);
        template[80] = set81;
        ExcelImportSet set82 = new ExcelImportSet("spillOutlook", 82, 82, Type.str, 20);
        template[81] = set82;
        ExcelImportSet set83 = new ExcelImportSet("spillOutlookDate", 83, 83, Type.date, 7);
        template[82] = set83;
        ExcelImportSet set84 = new ExcelImportSet("spillCreditWatch", 84, 84, Type.str, 100);
        template[83] = set84;
        ExcelImportSet set85 = new ExcelImportSet("spillCreditWatchDate", 85, 85, Type.date, 7);
        template[84] = set85;
        ExcelImportSet set86 = new ExcelImportSet("spifsRating", 86, 86, Type.str, 20);
        template[85] = set86;
        ExcelImportSet set87 = new ExcelImportSet("spifsDate", 87, 87, Type.date, 7);
        template[86] = set87;
        ExcelImportSet set88 = new ExcelImportSet("spifsOutlook", 88, 88, Type.str, 20);
        template[87] = set88;
        ExcelImportSet set89 = new ExcelImportSet("spifsOutlookDate", 89, 89, Type.date, 7);
        template[88] = set89;
        ExcelImportSet set90 = new ExcelImportSet("spifsCreditWatch", 90, 90, Type.str, 100);
        template[89] = set90;
        ExcelImportSet set91 = new ExcelImportSet("spifsCreditWatchDate", 91, 91, Type.date, 7);
        template[90] = set91;
        ExcelImportSet set92 = new ExcelImportSet("spilsRating", 92, 92, Type.str, 20);
        template[91] = set92;
        ExcelImportSet set93 = new ExcelImportSet("spilsDate", 93, 93, Type.date, 7);
        template[92] = set93;
        ExcelImportSet set94 = new ExcelImportSet("spilsOutlook", 94, 94, Type.str, 20);
        template[93] = set94;
        ExcelImportSet set95 = new ExcelImportSet("spilsOutlookDate", 95, 95, Type.date, 7);
        template[94] = set95;
        ExcelImportSet set96 = new ExcelImportSet("spilsCreditWatch", 96, 96, Type.str, 100);
        template[95] = set96;
        ExcelImportSet set97 = new ExcelImportSet("spilsCreditWatchDate", 97, 97, Type.date, 7);
        template[96] = set97;
    }

    /**
      * 创建一个新的实例 接受解析时需要的对象。
      * @param 表格样式
      * @param 共享字符串
      * @param 数据容器操作对象
     */
    RatingsImportXlsxHandler(StylesTable styles, ReadOnlySharedStringsTable strings, RatingsImportRecord result) {
        this.record = result;
        this.stylesTable = styles;
        this.sharedStringsTable = strings;
        this.value = new StringBuffer();
        this.nextDataType = xssfDataType.NUMBER;
        this.row = new Object[this.colCount];
    }

    /**
     * startElement
     * 读取一个元素前,判断元素和元素内的数据类型
     * (non-Javadoc)
     * @see
     * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * @param uri
     * @param localName
     * @param name
     * @param attributes
     * @throws SAXException
     * @author Limz
     */
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

        if ("inlineStr".equals(name) || "v".equals(name)) {
            this.vIsOpen = true;
            // 清除内容缓存
            this.value.setLength(0);
        }
        // c => cell
        else if ("c".equals(name)) {
            // 获取单元格引用
            String r = attributes.getValue("r");
            int firstDigit = -1;
            for (int c = 0; c < r.length(); ++c) {
                if (Character.isDigit(r.charAt(c))) {
                    firstDigit = c;
                    break;
                }
            }
            this.thisColumn = nameToColumn(r.substring(0, firstDigit));

            // 设置默认值。
            this.nextDataType = xssfDataType.NUMBER;
            this.formatIndex = -1;
            this.formatString = null;
            String cellType = attributes.getValue("t");
            String cellStyleStr = attributes.getValue("s");
            if ("b".equals(cellType))
                this.nextDataType = xssfDataType.BOOL;
            else if ("e".equals(cellType))
                this.nextDataType = xssfDataType.ERROR;
            else if ("inlineStr".equals(cellType))
                this.nextDataType = xssfDataType.INLINESTR;
            else if ("s".equals(cellType))
                this.nextDataType = xssfDataType.SSTINDEX;
            else if ("str".equals(cellType))
                this.nextDataType = xssfDataType.FORMULA;
            else if (cellStyleStr != null) {
                // 这是一个数字，但几乎可以肯定具有特殊风格或格式
                int styleIndex = Integer.parseInt(cellStyleStr);
                XSSFCellStyle style = this.stylesTable.getStyleAt(styleIndex);
                this.formatIndex = style.getDataFormat();
                this.formatString = style.getDataFormatString();
                if (this.formatString == null)
                    this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
            }
        }

    }

    /**
     * endElement
     * (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     * java.lang.String, java.lang.String)
     * @Description: 读取一个元素后,处理元素中的数据
     * @param uri
     * @param localName
     * @param name
     * @throws SAXException
     * @author Limz
     */
    public void endElement(String uri, String localName, String name) throws SAXException {

        String thisStr = null;

        // v => contents of a cell
        if ("v".equals(name)) {
            if (this.curRow + 1 > this.startRow) {
                String n = this.value.toString();
                n = n.trim();
                n = n.equals("") ? null : n;
                if (template[this.thisColumn].getType() == Type.date) {

                    // 判断是否是日期格式
                    if (this.formatString != null) {
                        thisStr = n;
                    } else
                        thisStr = new XSSFRichTextString(this.sharedStringsTable.getEntryAt(Integer.parseInt(n)))
                                .toString();
                } else if (template[this.thisColumn].getType() == Type.str) {
                    int idx;
                    try {
                        if (this.nextDataType == xssfDataType.SSTINDEX) {
                            idx = Integer.parseInt(n);
                            thisStr = new XSSFRichTextString(this.sharedStringsTable.getEntryAt(idx)).toString();
                        } else {
                            thisStr = n;
                        }
                    } catch (Exception ex) {
                        thisStr = n;
                    }
                } else {
                    thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                }
                validate(thisStr);
            }
        } else if ("row".equals(name)) {
            this.curRow++;
            if (this.curRow > this.startRow) {
                if (this.row[0] == null) {
                    this.row[0] = bankName;
                    this.row[1] = indexNo;
                    this.row[2] = BVDId;
                } else {
                    bankName = this.row[0];
                    indexNo = this.row[1];
                    BVDId = this.row[2];
                    if (indexNo == null || BVDId == null) {
                        this.record.addErrorLogs("第" + this.curRow + "行 ：BVDId 或 indexNo 为空 \n");
                        this.record.setFail();
                    }
                }
                if (this.record.isSuccess()) {
                    Object[] clone = this.row.clone();
                    this.record.addRow(clone);
                }
            }
            for (int i = 0; i < this.row.length; i++) {
                this.row[i] = null;
            }
        }

    }

    /**
     * validate(验证是否符合规则并存值)
     * 
     * @param str
     * @author Limz
     */
    public void validate(String str) {

        Object obj = null;
        try {
            obj = template[this.thisColumn].getType().getValue(str);
        } catch (Exception e) {
            this.record.addErrorLogs("第" + (this.curRow + 1) + "行 第" + excelColIndexToStr(this.thisColumn + 1) + "列："
                    + str + " 日期格式错误 \n");
            this.record.setFail();
        }
        if (template[thisColumn].getType() == Type.str) {
            if (str.length() > template[thisColumn].getLen()) {
                this.record.addErrorLogs("第" + (this.curRow + 1) + "行 第" + excelColIndexToStr(this.thisColumn + 1)
                        + "列：" + str + " 内容超出长度 " + template[this.thisColumn].getLen() + " \n");
                this.record.setFail();
            }
        }
        this.row[this.thisColumn] = obj;
    }

    //得到列索引，每一列c元素的r属性构成为字母加数字的形式，字母组合为列索引，数字组合为行索引，  
    //如AB45,表示为第（A-A+1）*26+（B-A+1）*26列，45行 
    @SuppressWarnings("unused")
    private int getRowIndex(String rowStr) {
        rowStr = rowStr.replaceAll("[^A-Z]", "");
        byte[] rowAbc = rowStr.getBytes();
        int len = rowAbc.length;
        float num = 0;
        for (int i = 0; i < len; i++) {
            num += (rowAbc[i] - 'A' + 1) * Math.pow(26, len - i - 1);
        }
        return (int) num;
    }

    /**
     * 仅在适当的元素打开时捕获字符。原来
     * 只是“V”；延长inlinestr。
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.vIsOpen)
            this.value.append(ch, start, length);
    }

    /**
     * excelColIndexToStr(列数字转换字母)
     * 
     * @param columnIndex
     * @return
     * @author Limz
     */
    private String excelColIndexToStr(int index) {
        String rs = "";
        do {
            index--;
            rs = ((char) (index % 26 + (int) 'A')) + rs;
            index = (int) ((index - index % 26) / 26);
        } while (index > 0);
        return rs;
    }

    /**
     * 将Excel列名称如“C”转换为基于一零的索引。
     * 
     * @param name
     * @return 指定名称对应的索引
     */
    private int nameToColumn(String name) {
        int column = -1;
        for (int i = 0; i < name.length(); ++i) {
            int c = name.charAt(i);
            column = (column + 1) * 26 + c - 'A';
        }
        return column;
    }
}
