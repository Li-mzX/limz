package javabean.newcredit.creditbank.thread;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
/**
  * @ClassName: Type ��������������--OBF������Ϣ�������롿 ��������
  * @Description: ���ں��ַ�����������
  * @author Limz
  * @date 2017-7-5 ����9:51:44
  *
 */
public enum Type {
    date(1),str(2);
    Type(int ty){
        this.ty = ty;
    }
    int ty;
    /**
      * getValue(�����������ͽ�����ת��Ϊʵ�ʵ�ֵ)
      * @param ����
      * @return ת�����ֵ
      * @throws Exception
      * @author Limz
     */
    public Object getValue(String str) throws Exception{
        Object t = null;
        if (this.ty == 1) {
            if (str == null || "".equals(str.trim())) {
                return null;
            }
            try {
                Date date = HSSFDateUtil.getJavaDate(Double.valueOf(str));
                t = new java.sql.Date(date.getTime());
            } catch (NumberFormatException e) {
                throw new Exception(e);
            }
        }else {
          t = str;  
        }
        return t;
    }
}
