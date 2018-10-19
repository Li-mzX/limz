package javabean.newcredit.creditdreport.util.pdf;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
  * @ClassName: PDFTextLocalStripper
  * @Description: 用于解析PDF
  * @author Limz
  * @date 2017-9-8 下午1:22:04
 */
public class PDFTextLocalStripper extends PDFTextStripper{

    private SingleCharHandle textHandle;
    private PDDocument document;

    /**
      * 创建一个新的实例 PDFTextLocalStripper. 用来解析PDF
      * @throws IOException
     */
    public PDFTextLocalStripper() throws IOException
    {
        super();
        setPageSeparator( "" );
        this.textHandle = new SingleCharLimzHandle();
    }
    /**
     * 创建一个新的实例 PDFTextLocalStripper. 用来解析PDF
     * @param textHandle 用于定义每个字符的处理
     * @throws IOException
     */
    public PDFTextLocalStripper(SingleCharHandle textHandle) throws IOException
    {
        super();
        setPageSeparator( "" );
        if (textHandle == null) {
            throw new IOException("Error SingleCharHandle can not be empty !");
        }
        this.textHandle = textHandle;
    }
    
    /**
      *  解析PDF
      * @author Limz
      * @param filePath 
      * @throws IOException 
      */
    public List<Object> execute(String filePath) throws IOException{
        try {
            this.document = PDDocument.load(filePath);
        } catch (Exception e) {
            throw new IOException("Error ：File load exception！");
        }
        setStartPage(1);
        setEndPage(this.document.getNumberOfPages());
        this.getText(document);
        return this.textHandle.getResult();
    }
    /**
     * @Description: 覆写父类用于处理单个字符的方法
     * @param text
     * @author Limz
     */
    @Override
    protected void processTextPosition( TextPosition text )
    {
        this.textHandle.composition(text);
    }
}
