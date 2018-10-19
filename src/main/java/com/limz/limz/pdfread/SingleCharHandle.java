package javabean.newcredit.creditdreport.util.pdf;

import java.util.List;

import org.apache.pdfbox.util.TextPosition;


/**
  * @Description: 用于处理pdf中的单个字符
  * @author Limz
  * @date 2017-9-8 下午1:31:32
  *
 */
public interface SingleCharHandle {

    public void composition(TextPosition text);
    
    public List<Object> getResult();
}
