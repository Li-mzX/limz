package javabean.newcredit.creditdreport.util.pdf;

import java.util.List;

import org.apache.pdfbox.util.TextPosition;


/**
  * @Description: ���ڴ���pdf�еĵ����ַ�
  * @author Limz
  * @date 2017-9-8 ����1:31:32
  *
 */
public interface SingleCharHandle {

    public void composition(TextPosition text);
    
    public List<Object> getResult();
}
