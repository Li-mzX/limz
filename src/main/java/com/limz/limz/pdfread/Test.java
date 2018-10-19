package javabean.newcredit.creditdreport.util.pdf;

import java.io.IOException;

public class Test {

    protected String name;
    
    public static void main(String[] args) throws IOException{
        
        
        String path ="C:\\Users\\zhaojz\\Desktop\\测试\\彩虹集团电子股份有限公司.pdf";

        PDFTextLocalStripper stripper = new PDFTextLocalStripper();
        stripper.execute(path);
        
    }
        
}
