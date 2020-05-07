package application;

import java.util.Arrays;
import java.util.List;

public class Solver {

	
	public String Solve(String ruleText, String memoryText) 
	{
		List<String> memoryTextList = Arrays.asList(memoryText.split("\n"));
		
		char character = 'A';
		int iter = 1;
		while(ruleText.contains("?") && character <= 'Z')
		{
			System.out.println(ruleText);
			ruleText = ruleText.replaceAll("\\?"+character, String.valueOf(iter));
			iter++;
			character++;
			System.out.println(ruleText);
		}
		
		for(String text : memoryTextList) {
			System.out.println("!! "+text);
		}
		return ruleText;
		//return "Rule "+ruleText+"\nMemory "+memoryText;
	}
	
	
}
