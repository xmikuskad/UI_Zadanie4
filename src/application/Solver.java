package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Solver {

	private final String ADD = "pridaj";
	private final String DELETE = "vymaz";
	private final String MESSAGE = "sprava";
	
	//nameOfRule a action vratia meno a akciu daneho pravidla;
	//mena musia byt bez medzery!
	private List<String> nameOfRule = new ArrayList<String>();
	private List<String> action = new ArrayList<String>();
	
	//Tento list obsahuje vsetky nove veci, ktore pridavame do dalsej iteracie
	private List<String> memoryAdding = new ArrayList<String>();
	
	//Toto obsahuje vsetky podmienky pravidla, ktore su upravene z "X je rodic Y" na list X,Y
	//Priklad: z (?R je rodic ?X)(?R je rodic ?Y)(muz ?Y)(<> ?X ?Y) dostanem R,X,R,Y,Y,X,Y
	private List<String>conditionsWithoutNames = new ArrayList<String>();
	//Toto je to iste akurat namiesto X,Y su tam konkretne mena. 
	//Budeme to porovnavat s conditionsWithoutName aby sme zistili, ze ci je dane dosadenie platne
	//Ziskali sme to pomocou permutacii vsetkych moznych moznosti
	private List<List<List<String>>> conditionsWithNames= new ArrayList<List<List<String>>>();
	
	//Toto je vysledok vsetkych permutacii z pamati
	private List<List<String>> permutationResult = new ArrayList<List<String>>();
	
	//Obsahuje vsetky poznatky
	private List<String> memoryTextList = new LinkedList<String>();
	
	//Obsahuje vsetky specialne poznatky
	private List<List<String>> specialConditions = new ArrayList<List<String>>();
	
	//Obsahuje vysledok prikazu sprava
	private List<String> messageList = new ArrayList<String>();
	
	//Obsahuje udaje z pomocnej pamati tj veci, ktore sa este len idu pridat do pamati
	private List<String> helperList = new ArrayList<String>();
	
	//Toto sa vykona pri jednom kliku na tlacidlo Next Step
	public void makeOneStep(String rulesRawText, String memoryText, String messageText, String helperText, MenuController con) 
	{
		memoryTextList = new ArrayList<String>();
		
		//Kontrola prazdnej pamati - mozno zle ak su tam otazky na opytanie
		if(!memoryText.isEmpty()) {
			//Rozdelenie pamati na jednotlive informacie
			memoryTextList = new LinkedList<String>(Arrays.asList(memoryText.split("\n")));
		}
		if(!messageText.isEmpty()) {
			messageList = new LinkedList<String>(Arrays.asList(messageText.split("\n")));
		}
		
		if(!helperText.isEmpty()) {
			helperList = new LinkedList<String>(Arrays.asList(helperText.split("\n")));
		}
		
		
		//Rozdelime text na jednotlive pravidla
		List<String> ruleRawTextList = Arrays.asList(rulesRawText.split("\n\n"));
		System.out.println("Rule count "+ruleRawTextList.size());	
		
		//Vyprazdnenie premennych 
		nameOfRule = new ArrayList<String>();
		action = new ArrayList<String>();
		memoryAdding = new ArrayList<String>();
		permutationResult = new ArrayList<List<String>>();
		specialConditions = new ArrayList<List<String>>();
		
		//Pridanie novej veci
		if(helperList.size() > 0) {
			processCommand(helperList.get(0).split(" ",2)[1]);
			helperList.remove(0);
		}
		
		solveIt(ruleRawTextList);
		
		StringBuilder memoryBuilder = new StringBuilder();
		//Vypis pamate
		for(String text : memoryTextList) {
			//System.out.println("!! "+text);
			memoryBuilder.append(text+"\n");
		}				
		
		StringBuilder messageBuilder = new StringBuilder();
		for(String text : messageList) {
			//System.out.println("// "+text);
			messageBuilder.append(text+"\n");
		}	
		
		StringBuilder helperBuilder = new StringBuilder();
		//Vypis pamate
		for(String text : helperList) {
			//System.out.println("// "+text);
			helperBuilder.append(text+"\n");
		}	
		
		for(String text : memoryAdding) {
			//System.out.println("++ "+text);
			if (!contains(helperList,text))
				helperBuilder.append(text+"\n");
		}			
		
		con.setMemoryText(memoryBuilder.toString());		
		con.setMessageText(messageBuilder.toString());
		con.setHelperText(helperBuilder.toString());
	}
	
	//Toto sa vykona pri jednom kliku na tlacidlo Solve
	public void solveItAll(MenuController con, boolean solveOnce) 
	{
		while(true) {
			String rulesRawText = con.getRulesText(); 
			String memoryText = con.getMemoryText();
			String messageText = con.getMessageText();
			String helperText = con.getHelperText();
			memoryTextList = new ArrayList<String>();
			int memoryTextCount;
			
			//Kontrola prazdnej pamati - mozno zle ak su tam otazky na opytanie
			if(!memoryText.isEmpty()) {
				//Rozdelenie pamati na jednotlive informacie
				memoryTextList = new LinkedList<String>(Arrays.asList(memoryText.split("\n")));
			}
			if(!messageText.isEmpty()) {
				messageList = new LinkedList<String>(Arrays.asList(messageText.split("\n")));
			}
			
			if(!helperText.isEmpty()) {
				helperList = new LinkedList<String>(Arrays.asList(helperText.split("\n")));
			}
			
			memoryTextCount = memoryTextList.size();
			
			//Rozdelime text na jednotlive pravidla
			List<String> ruleRawTextList = Arrays.asList(rulesRawText.split("\n\n"));
			System.out.println("Rule count "+ruleRawTextList.size());	
			
			//Vyprazdnenie premennych 
			nameOfRule = new ArrayList<String>();
			action = new ArrayList<String>();
			memoryAdding = new ArrayList<String>();
			permutationResult = new ArrayList<List<String>>();
			specialConditions = new ArrayList<List<String>>();
			
			//Pridanie novej veci
			if(helperList.size() > 0) {
				processCommand(helperList.get(0).split(" ",2)[1]);
				helperList.remove(0);
			}
			
			solveIt(ruleRawTextList);
			
			StringBuilder memoryBuilder = new StringBuilder();
			//Vypis pamate
			for(String text : memoryTextList) {
				//System.out.println("!! "+text);
				memoryBuilder.append(text+"\n");
			}				
			
			StringBuilder messageBuilder = new StringBuilder();
			for(String text : messageList) {
				//System.out.println("// "+text);
				messageBuilder.append(text+"\n");
			}	
			
			StringBuilder helperBuilder = new StringBuilder();
			//Vypis pamate
			for(String text : helperList) {
				//System.out.println("// "+text);
				helperBuilder.append(text+"\n");
			}	
			
			for(String text : memoryAdding) {
				//System.out.println("++ "+text);
				if (!contains(helperList,text))
					helperBuilder.append(text+"\n");
			}			
			
			con.setMemoryText(memoryBuilder.toString());		
			con.setMessageText(messageBuilder.toString());
			con.setHelperText(helperBuilder.toString());
			
			if(solveOnce || (con.getHelperText().length() <=0 && memoryTextCount == memoryTextList.size()))
			{
				//Neprislo nam ziadne nove pravidlo
				break;
			}
		}
	}
	
	
	public void solveIt(List<String> ruleRawTextList) 
	{
		//Pre kazde pravidlo z pravidiel
		for(int j=0;j<ruleRawTextList.size();j++) {			
			if(ruleRawTextList.size() <3)  continue;
			
			conditionsWithoutNames = new ArrayList<String>();
			conditionsWithNames= new ArrayList<List<List<String>>>();
			specialConditions.add(new ArrayList<String>());		
					
			//Toto obsahuje jedno konkretne pravidlo
			//0 obsahuje meno, 1 pravidla, 2 akcie
			List<String> ruleRawText = Arrays.asList(ruleRawTextList.get(j).split("\n"));
			
			//Pridanie mena a akcie do listu
			nameOfRule.add(ruleRawText.get(0));
			action.add(ruleRawText.get(2));
			

			List<String> conditionsList = Arrays.asList(ruleRawText.get(1).split(","));
		
			//Toto vytvori conditionsWithoutNames a conditionsWithNames
			processConditions(conditionsList,j);
											
			permutationResult = new ArrayList<List<String>>();
			createPermutations(0,new ArrayList<String>());
			
			checkPermutations(j);
			
		}

	}
	
	private void processConditions(List<String> conditionsList, int index) 
	{	
		for(int i =0; i<conditionsList.size(); i++) {			
			String condition = conditionsList.get(i);
			
			//Ked fakt neobsahuje premennu - napr ak "Vlado je muz"
			if(!condition.contains("?"))
			{
				conditionsWithoutNames.add(condition);
				conditionsWithNames.add(new ArrayList<List<String>>());
				for(String memoryString:memoryTextList) {
					if(isSubstring(condition,memoryString))
					{
						List<String> conditionList = new ArrayList<String>();
						conditionList.add(condition);
						conditionsWithNames.get(i).add(conditionList);
					}
				}	
				continue;
			}
			
			String conditionRegexed = condition.replaceAll("[?]{1}[A-Z]{1}","");
						
			conditionRegexed = removeEdgeSpaces(conditionRegexed);
			//System.out.println("RULE: |"+conditionRegexed+"|");
			
			//Specialne pre <>
			if(conditionRegexed.equals("<>"))
			{
				List<String> variables = Arrays.asList(condition.split(conditionRegexed));
				for(String variable:variables) {
					List<String> tmpList = stripName(variable);		
					specialConditions.get(index).addAll(tmpList);
				}
				continue;
			}
			
			
			//Pridanie cisteho X,Y,X,Y,Z,X			
			List<String> variables = Arrays.asList(condition.split(conditionRegexed));
			for(String variable:variables) {
				List<String> tmpList = stripName(variable);		
				conditionsWithoutNames.addAll(tmpList);
			}
			
			//Pridanie vystrihnutych mien v tvare Meno,Meno,Meno,Meno
			conditionsWithNames.add(new ArrayList<List<String>>());
			for(String memoryString:memoryTextList) {
				if(isSubstring(conditionRegexed,memoryString))
				{
					variables = Arrays.asList(memoryString.split(conditionRegexed));
					List<String> conditionList = new ArrayList<String>();
					for(String variable:variables) {
						List<String> tmpList = stripName(variable);	
						conditionList.addAll(tmpList);
					}
					if(conditionList.size() > 0) {
						conditionsWithNames.get(i).add(conditionList);
					}
				}
			}	
		}		
	}
	
	private void checkPermutations(int index) {	
		for(int j=0; j<permutationResult.size();j++) {
			List<String> specialConditionsEdit = new ArrayList<String>();
			specialConditionsEdit.addAll(specialConditions.get(index));
			List<String> permutation = permutationResult.get(j);
			HashMap<String, String> nameVariables = new HashMap<String, String>();
			boolean adding = true;
			
			//Overenie danej permutacie
			for(int i=0; i<permutation.size();i++) {
				String get = nameVariables.get(conditionsWithoutNames.get(i));
				if(get == null) {
					nameVariables.put(conditionsWithoutNames.get(i), permutation.get(i));
				}
				else if (!get.equals(permutation.get(i))) {
					adding = false;
					break;
				}
			}
			
			if(adding && specialConditionsEdit.size() > 1) {
				
				for(int i =0;i<specialConditionsEdit.size();i++)
				{
					specialConditionsEdit.set(i,nameVariables.get(specialConditionsEdit.get(i)));
					
				}
				
				//Kontrola duplikacii v ramci <>
				for(int m=0;m<specialConditionsEdit.size();m++) {
					for(int n=m+1;n<specialConditionsEdit.size();n++) {
						if(specialConditionsEdit.get(m).equals(specialConditionsEdit.get(n))) {
							adding = false;
						}
					}
				}
			}
			
			
			if(adding) {
				String addingRule = action.get(index);
				
				//Dosadzovanie premennych
				char character = 'A';
				int iter = 1;
				while(addingRule.contains("?") && character <= 'Z')
				{
					addingRule = addingRule.replaceAll("\\?"+character, nameVariables.get("?"+character));
					iter++;
					character++;
				}
				
				if(checkCommand(addingRule)) {				
					memoryAdding.add(nameOfRule.get(index)+" "+addingRule);
					System.out.println("ADDING");
				}
			}
		}
		
	}
	
	private void processCommand(String addingRule)
	{
		System.out.println(addingRule);
		List<String> actionList = new ArrayList<String>();
		if(addingRule.contains(",")) {
			actionList = Arrays.asList(addingRule.split(","));
		}else {
			actionList.add(addingRule);
		}
		
		for(String rule: actionList) {
			System.out.println(rule);
			String[] tmp = rule.split(" ",2);
			String command = tmp[0];
			String other = tmp[1];
			
			System.out.println("0 "+tmp[0]);
			System.out.println("1 "+tmp[1]);
			
			//TODO dokoncit aj ostatne moznosti
			if(command.equals(ADD)) {
				boolean exists = false;
				
				for(String text:memoryTextList) {
					if(text.equals(other))
						exists=true;
				}
				
				if(!exists)
					//memoryAdding.add(tmp[0]+other);
					memoryTextList.add(other);
			}
			else if(command.equals(DELETE)) {
				for(int i=0;i<memoryTextList.size();i++) {
					if(memoryTextList.get(i).equals(other)) {
						memoryTextList.remove(i);
						i--;
					}
				}
			}
			else if(command.equals(MESSAGE)) {
				boolean exists = false;
				
				for(String text:messageList) {
					if(text.equals(other))
						exists=true;
				}
				
				if(!exists)
					messageList.add(other);
			}
		}
		
	}
	
	private boolean checkCommand(String addingRule)
	{
		boolean validCommand = false;
		List<String> actionList = new ArrayList<String>();
		if(addingRule.contains(",")) {
			actionList = Arrays.asList(addingRule.split(","));
		}else {
			actionList.add(addingRule);
		}
		
		for(String rule: actionList) {
			System.out.println(rule);
			String[] tmp = rule.split(" ",2);
			String command = tmp[0];
			String other = tmp[1];
			
			//TODO dokoncit aj ostatne moznosti
			if(command.equals(ADD)) {
				boolean exists = false;
				
				for(String text:memoryTextList) {
					if(text.equals(other))
						exists=true;
				}
				
				if(!exists) {
					validCommand = true;
				}
			}
			else if(command.equals(DELETE)) {
				for(int i=0;i<memoryTextList.size();i++) {
					if(memoryTextList.get(i).equals(other)) {
						validCommand = true;
					}
				}
			}
			else if(command.equals(MESSAGE)) {
				boolean exists = false;
				
				for(String text:messageList) {
					if(text.equals(other))
						exists=true;
				}
				
				if(!exists)
					validCommand = true;
			}
		}
		
		return validCommand;
		
	}
	
	private void createPermutations(int i, List<String> list)
	{
		if(conditionsWithNames.size() <= i) {
			if(list.size() > 0)
				permutationResult.add(list);
			return;
		}
		
		for(int j=0;j<conditionsWithNames.get(i).size();j++) {
			List<String> tmpList = new ArrayList<String>();
			tmpList.addAll(list);
			tmpList.addAll(conditionsWithNames.get(i).get(j));
			createPermutations(i+1,tmpList);
		}
					
	}
	
	private boolean isSubstring(String substring, String string) {
		if(string.indexOf(substring) != -1)
			return true;
		else
			return false;
	}
	
	//TODO mozno je to mazanie medzier zbytocne
	private List<String> stripName(String variable) {
		List<String> returnList = new ArrayList<String>();
		
		//Ak obsahuje medzeru znamena to, ze tam je viacero premennych a treba ich oddelit
		//Napriklad pri tvare "manzelia X Y"
		if(variable.contains(" ")) {
			List<String> tmpList = Arrays.asList(variable.split(" "));
			
			for(String variableTmp:tmpList) {
				String tmpVariableEdited = removeEdgeSpaces(variableTmp);
				if (!tmpVariableEdited.equals(""))
					returnList.add(removeEdgeSpaces(tmpVariableEdited));
			}
		}
		else {
		//Inak nastal tvar "X je rodic Y"
		String variableEdited = removeEdgeSpaces(variable);
		if (!variableEdited.equals(""))
			returnList.add(removeEdgeSpaces(variableEdited));
		}
	
		return returnList;
	}
			
	//Vymaze medzery pred a po stringu
	private String removeEdgeSpaces(String ruleRegexed)
	{
		while(!ruleRegexed.isEmpty() && ruleRegexed.charAt(0) == ' ') {
			ruleRegexed = ruleRegexed.substring(1);
		}
	
		while(!ruleRegexed.isEmpty() && ruleRegexed.charAt(ruleRegexed.length()-1) == ' ') {
			ruleRegexed = ruleRegexed.substring(0, ruleRegexed.length()-1);
		}
		
		return ruleRegexed;
	}
	
	private boolean contains(List<String> list, String var)
	{
		for(String text:list)
		{
			if(text.equals(var))
				return true;
		}
		return false;
	}
	
}
