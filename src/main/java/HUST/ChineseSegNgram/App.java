package HUST.ChineseSegNgram;


import java.util.List;

import opennlp.tools.ngram.NGramGenerator;

public class App 
{
    public static void main( String[] args )
    {
   		String string = "我爱自然语言处理。";
   		
   		
   		char[] ch = string.toCharArray();

    	List<String> trigram = NGramGenerator.generate(ch, 3, ""); 
//    	StringList sl = new StringList(trigram);
//    	System.out.println(sl);
    	System.out.println(Math.log(100)/Math.log(10));
    	
    	
    }
}
