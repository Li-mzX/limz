package javabean.newcredit.creditdreport.util.pdf;

import java.io.IOException;

public class Test {

    protected String name;
    
    public static void main(String[] args) throws IOException{
        
        
        String path ="C:\\Users\\zhaojz\\Desktop\\����\\�ʺ缯�ŵ��ӹɷ����޹�˾.pdf";

        PDFTextLocalStripper stripper = new PDFTextLocalStripper();
        stripper.execute(path);
        
    }
        
}
