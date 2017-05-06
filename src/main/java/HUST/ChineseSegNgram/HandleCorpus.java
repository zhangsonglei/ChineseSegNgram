package HUST.ChineseSegNgram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import opennlp.tools.ngram.NGramGenerator;

public class HandleCorpus {
	static HashSet<Character> digits=new HashSet<Character>();
	static HashSet<Character> punc = new HashSet<Character>();
	
	static{
		String digits_str="1234567890〇一二三四五六七八九十";
		for(int i=0,len=digits_str.length();i<len;i++){
			digits.add(digits_str.charAt(i));
		}
		String punc_str="!\"#$%&±'()*+,-./:·●;<=>?@[\\]∶^℃ △▲_`{|}~。？！，、；〉〈：“”‘’（）{}【】—』『…《》";
		for(int i=0,len=punc_str.length();i<len;i++){
			punc.add(punc_str.charAt(i));
		}
	}
	
	/**
	 * change the full-width format to half-width format
	 * @param str
	 * @return
	 */
	public static String fullToHalf(String str) {
		String resStr = new String();
		String reg = "[^\u4e00-\u9fa5]";
		
		if(null != str) {
			char[] ch = str.toCharArray();
			
			for (int i = 0; i < ch.length; i++) {
				if ('\u3000' == ch[i]) {
					ch[i] = ' ';
				} else if (ch[i] > '\uFF00' && ch[i] < '\uFF5F') {
					ch[i] = (char) (ch[i] - 65248);
				}
			}//end for
			
			resStr = new String(ch);
		}//end if
		
		return resStr;
	}
	
	/**
	 * replace punctuation to '\n'
	 * @param str
	 * @return
	 */
	public static String removePunc(String str) {
		char[] characters = str.toCharArray();
		
		for(int i = 0; i < characters.length; i++) {
			if(isPunc(characters[i]))
				characters[i]=' ';
		}
		
		return new String(characters);
	}
	
	/**
	 * to judge whether a character is a digit 
	 * @param ch
	 * @return
	 */
	public static boolean isDigits(char ch) {
		return digits.contains(ch);
	}
	
	/**
	 * to judge whether a character is a punctuation
	 * @param ch
	 * @return
	 */
	public static boolean isPunc(char ch) {
		return punc.contains(ch);
	}
	
	/**
	 * to judge whether a character is Chinese 
	 * @param ch
	 * @return
	 */
	public static boolean isChinese(char ch) {
		if ((ch >= '\u4e00' && ch <= '\u9fa5') || (ch >= '\uF900' && ch <= '\uFA2D'))
			return true;
		return false; 
	}
	
	/**
	 * to judge whether a string is Chinese
	 * @param str
	 * @return
	 */
	public static boolean isStrChinese(String str) {
		for(int i = 0; i < str.length(); i++) {
			if(!isChinese(str.substring(i, i + 1).toCharArray()[0]))
				return false;
		}
		
		return true;
	}
	
	/**
	 * to judge whether a character is date
	 * @param ch
	 * @return
	 */
	public static boolean isDateUnit(char ch) {
		return ch=='年'||ch=='月'||ch=='日';
	}
	
	/**
	 * split corpus to word
	 * @throws IOException
	 */
	public static void handleCorpus() throws IOException {
		
		String readPath = "Files/Train/pku_training.utf8";
		String writePath = "Files/Result/handledCorpus.utf8";
		File readFile = new File(readPath);
		File writeFile = new File(writePath);

		if(readFile.isFile() && readFile.exists()) {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(writeFile));
			
			BufferedReader reader = new BufferedReader(inputStreamReader);
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);
			
			String line = new String();
			String content = new String();
			while((line = reader.readLine()) != null) {
				if(!line.equals("")) {
					line = fullToHalf(line);
					line = removePunc(line);
					content += line + " ";
				}
			}//end while
			reader.close();
			
			content = content.trim();
			String[] words = content.split("\\s+");
			for(String word : words) {
				writer.write(word);
				writer.newLine();
			}
			writer.close();
			
		}else {	
			System.err.println("文件读取失败");
		}//end if
		
	}//end handleCorpus()
	
	/**
	 * add BEMS 4tags to each word
	 * @throws IOException
	 */
	public static void addTags() throws IOException{
		String readPath = "Files/Result/handledCorpus.utf8";
		String writePath = "Files/Result/taggedCorpus.utf8";

		File readFile = new File(readPath);
		File file = new File(writePath);


		if(readFile.isFile() && readFile.exists()) {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
			
			BufferedReader reader = new BufferedReader(inputStreamReader);
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);
			
			String line = new String();
			while ((line = reader.readLine()) != null) {
				writer.write(generateBEMS(line));
				writer.newLine();
			}
			reader.close();
			writer.close();
		}else {	
			System.err.println("文件读取失败");
		}//end if
	}
	
	/**
	 * add (BEMS)tag to each character
	 * *我——>我/S
	 * *喜欢——>喜/B欢/E
	 * *自然语言处理——>自/B然/M语/M言/M处/M理/E
	 * 
	 * @param line
	 * @return
	 */
	public static String generateBEMS(String line) {
		String tagged = new String();
		int len = 0;
		
		String[] strings = line.split("\\s+");
		
		for(int i = 0; i < strings.length; i++) {
			len = strings[i].length();
			
			if(1 == len) {
				strings[i] += "/S";
			}else if(2 <= len){
				String[] str = strings[i].split("");
				str[0] += "/B";
				
				if(2 < len) {
					for(int j = 1; j < len - 1; j++) {
						str[j] += "/M";
					}
				}
				
				str[len -1] += "/E";
				
				strings[i] = "";
				for(int k = 0; k < len; k++)
					strings[i] += str[k];
			}
		}//end for
		
		
		for(int t = 0; t < strings.length; t++)
			tagged += strings[t];
		
		return tagged;
	}//end addTags()

	/**
	 * statistics each character
	 * @throws IOException
	 */
	public static void characterStatistics() throws IOException {
		String readPath = "Files/Result/handledCorpus.utf8";
		String writecharacterStatisticsPath = "Files/Result/characterStatistics.utf8";

		File readFile = new File(readPath);
		File writeCharacterStatistics = new File(writecharacterStatisticsPath);

		if(readFile.isFile() && readFile.exists()) {
			HashMap<String, Integer> hashMap = new HashMap<>();
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(writeCharacterStatistics));
			
			BufferedReader reader = new BufferedReader(inputStreamReader);
			BufferedWriter chatacterWriter = new BufferedWriter(outputStreamWriter);
			
			String characterSet = new String();
			String line = new String();
			while((line = reader.readLine()) != null) {
				characterSet += line.replaceAll("\\s+", "");
				
			}//end while
			reader.close();
			
			/**
			 * statistics each character
			 */
			String[] characters = characterSet.split("");
			for(String character:characters){
				if(hashMap.containsKey(character))
					hashMap.put(character, hashMap.get(character) + 1);
				else 
					hashMap.put(character, 1);
			}
			for (HashMap.Entry<String, Integer> entry : hashMap.entrySet()) {
				chatacterWriter.write(entry.getKey()+" "+ entry.getValue());				  
				chatacterWriter.newLine();
			}  
			chatacterWriter.close();
			
		}else {	
			System.err.println("文件读取失败");
		}//end if
		
	}
	
	/**
	 * statistics all bigram
	 * @throws IOException
	 */
	public static void bigramStatistics() throws IOException {
		String readPath = "Files/Result/handledCorpus.utf8";
		String writeBigramPath = "Files/Result/bigramStatistics.utf8";
		File readFile = new File(readPath);
		File writeBigram = new File(writeBigramPath);

		if(readFile.isFile() && readFile.exists()) {
			HashMap<String, Integer> ngramMap = new HashMap<>();
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(writeBigram));
			
			BufferedReader reader = new BufferedReader(inputStreamReader);
			BufferedWriter bigramWriter = new BufferedWriter(outputStreamWriter);
			
			String line = new String();
			while((line = reader.readLine()) != null) {
				if(line.length() > 1) {
					char[] ch = line.toCharArray();
					List<String> bigrams = NGramGenerator.generate(ch, 2, "");
					for(String bigram : bigrams) {
						if(ngramMap.containsKey(bigram))
							ngramMap.put(bigram, ngramMap.get(bigram) + 1);
						else 
							ngramMap.put(bigram, 1);
					}
				}
				
			}//end while
			reader.close();
			
			/**
			 * statistics all bigram
			 */
			for (HashMap.Entry<String, Integer> entry : ngramMap.entrySet()) {
				bigramWriter.write(entry.getKey()+" "+ entry.getValue());				  
				bigramWriter.newLine();
			}
			bigramWriter.close();
			
		}else {	
			System.err.println("文件读取失败");
		}//end if
		
	}

	/**/
	public static void splitBigram() throws IOException{
		String readPath = "Files/Result/handledCorpus.utf8";
		String wirtePath = "Files/Result/training.utf8";
	
		File readFile = new File(readPath);
		File writeFile = new File(wirtePath);
		
		if(readFile.isFile()&&readFile.exists()) {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(writeFile));
			
			BufferedReader reader = new BufferedReader(inputStreamReader);
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);
			
			String line =new String();
			while((line = reader.readLine()) != null) {
				if(line.length() > 1) {
					String[] string = line.split("");
					
					line = "";
					for(String str : string)
						line += str + " ";
					
					writer.write(line.trim());
					writer.newLine();
				}else {
					writer.write(line);
					writer.newLine();
				}//end else if
			}//end while
			
			reader.close();
			writer.close();
			
		}else {
			System.err.println("读取文件错误！");
		}
	}

	public static void main(String[] args) {
		try {
			long starTime=System.currentTimeMillis();
			System.out.println("start handle corpus...");
			handleCorpus();
			System.out.println("end handle corpus.");
		
//			System.out.println("start add tags...");
//			addTags();
//			System.out.println("end add tags.");
//			
//			System.out.println("start statistics characters...");
//			characterStatistics();
//			System.out.println("end statistics characters.");
//			
//			System.out.println("start statistics bigrams...");
//			bigramStatistics();
//			System.out.println("end statistics bigrams.");

			splitBigram();
			long endTime=System.currentTimeMillis();
			System.out.println("用时："+((double)(endTime-starTime)/1000.0)+"s");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
