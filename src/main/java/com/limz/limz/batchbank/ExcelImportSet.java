package javabean.newcredit.creditbank.thread;

/**
  * @ClassName: ExcelImportSet��������������--OBF������Ϣ�������롿
  * @Description: excel����������
  * @author Limz
  * @date 2017-7-5 ����9:42:38
  *
 */
public class ExcelImportSet {
    
    private Type type;
    
    private int excelIndex;
    
    private int paramIndex;
    
    private String fieldName;
    
    private int len;

    /**
      * ����һ���µ�ʵ�� ExcelImportSet. 
      * @param fieldName �ֶ�����
      * @param excelIndex excel�е�λ��
      * @param paramIndex �����е�λ��
      * @param type ��������
      * @param len ��������
     */
    public ExcelImportSet(String fieldName, int excelIndex, int paramIndex, Type type, int len) {
        this.type = type;
        this.excelIndex = excelIndex;
        this.paramIndex = paramIndex;
        this.fieldName = fieldName;
        this.len = len;
    }

    /**
     * @return �ֶγ���
     */
    public int getLen() {
        return len;
    }

    /**
     * @return �ֶ�����
     */
    public Type getType() {
        return type;
    }

    /**
     * @return excel �е�����
     */
    public int getExcelIndex() {
        return excelIndex;
    }

    /**
     * @return sql����в�������
     */
    public int getParamIndex() {
        return paramIndex;
    }

    /**
     * @return �ֶ���
     */
    public String getFieldName() {
        return fieldName;
    }
}
