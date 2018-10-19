package javabean.newcredit.creditbank.thread;

/**
  * @ClassName: ExcelImportSet【导入银行资信--OBF评级信息批量导入】
  * @Description: excel导入设置类
  * @author Limz
  * @date 2017-7-5 上午9:42:38
  *
 */
public class ExcelImportSet {
    
    private Type type;
    
    private int excelIndex;
    
    private int paramIndex;
    
    private String fieldName;
    
    private int len;

    /**
      * 创建一个新的实例 ExcelImportSet. 
      * @param fieldName 字段名称
      * @param excelIndex excel中的位置
      * @param paramIndex 参数中的位置
      * @param type 数据类型
      * @param len 长度限制
     */
    public ExcelImportSet(String fieldName, int excelIndex, int paramIndex, Type type, int len) {
        this.type = type;
        this.excelIndex = excelIndex;
        this.paramIndex = paramIndex;
        this.fieldName = fieldName;
        this.len = len;
    }

    /**
     * @return 字段长度
     */
    public int getLen() {
        return len;
    }

    /**
     * @return 字段类型
     */
    public Type getType() {
        return type;
    }

    /**
     * @return excel 中的列数
     */
    public int getExcelIndex() {
        return excelIndex;
    }

    /**
     * @return sql语句中参数序列
     */
    public int getParamIndex() {
        return paramIndex;
    }

    /**
     * @return 字段名
     */
    public String getFieldName() {
        return fieldName;
    }
}
