package javabean.newcredit.creditbank.thread;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
/**
  * @ClassName: Type 【导入银行资信--OBF评级信息批量导入】 数据类型
  * @Description: 日期和字符串两种类型
  * @author Limz
  * @date 2017-7-5 上午9:51:44
  *
 */
public enum Type {
    date(1),str(2);
    Type(int ty){
        this.ty = ty;
    }
    int ty;
    /**
      * getValue(根据数据类型将数据转换为实际的值)
      * @param 数据
      * @return 转换后的值
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
