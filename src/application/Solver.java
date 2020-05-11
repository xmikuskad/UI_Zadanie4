package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Solver {

	private final String ADD = "pridaj";
	
	//nameOfRule a action vratia meno a akciu daneho pravidla;
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
	private List<String> memoryTextList = new ArrayList<String>();
	
	//Obsahuje vsetky specialne poznatky
	private List<List<String>> specialConditions = new ArrayList<List<String>>();
	
	public void SolveOneStep(String rulesRawText, String memoryText, MenuController con) 
	{
		memoryTextList = new ArrayList<String>();
		
		//Kontrola prazdnej pamati - mozno zle ak su tam otazky na opytanie
		if(!memoryText.isEmpty()) {
			//Rozdelenie pamati na jednotlive informacie
			memoryTextList = Arrays.asList(memoryText.split("\n"));
		}
		//TODO Osetrit prazdnu pamat!
		/*else {
			System.out.println("Memory is empty!");
			return;
		}*/
		
		//Rozdelime text na jednotlive pravidla
		List<String> ruleRawTextList = Arrays.asList(rulesRawText.split("\n\n"));
		System.out.println("Rule count "+ruleRawTextList.size());	
		
		//Vyprazdnenie premennych 
		nameOfRule = new ArrayList<String>();
		action = new ArrayList<String>();
		memoryAdding = new ArrayList<String>();
		permutationResult = new ArrayList<List<String>>();
		specialConditions = new ArrayList<List<String>>();
		
		//Pre kazde pravidlo z pravidiel
		for(int j=0;j<ruleRawTextList.size();j++) {
			System.out.println("\n\nCHECKING NEW RULE");
			conditionsWithoutNames = new ArrayList<String>();
			conditionsWithNames= new ArrayList<List<List<String>>>();
			specialConditions.add(new ArrayList<String>());
			
			//Toto obsahuje jedno konkretne pravidlo
			//0 obsahuje meno, 1 pravidla, 2 akcie
			List<String> ruleRawText = Arrays.asList(ruleRawTextList.get(j).split("\n"));
			
			//Pridanie mena a akcie do listu
			nameOfRule.add(ruleRawText.get(0));
			action.add(ruleRawText.get(2));
			
			System.out.println("Meno "+ruleRawText.get(0));
			System.out.println("Pravidla "+ruleRawText.get(1));
			List<String> conditionsList = Arrays.asList(ruleRawText.get(1).split(","));
		
			//Toto vytvori conditionsWithoutNames a conditionsWithNames
			processConditions(memoryTextList,conditionsList,j);
									
			//Vypis cistych premennych
			for(String text3 : conditionsWithoutNames) {
					System.out.println("NO NAME: |"+text3+"|");
			}
			
			//Vypis cistych mien
			for(List<List<String>> text4 : conditionsWithNames) {
				System.out.println("NEW CONDITION");
				for(List<String> text5: text4) {
					System.out.println("New record");
					for(String text7: text5)
					System.out.println("NAME: |"+text7+"|");
				}
			}
			
			permutationResult = new ArrayList<List<String>>();
			createPermutations(0,new ArrayList<String>());
			
			//Vypis permutacii
			for(List<String> text8 : permutationResult) {
				System.out.println("\nNEW COMBINATION");
				for(String text9 : text8)
					System.out.println("COMB: |"+text9+"|");
			}
			
			System.out.println("Akcie "+ruleRawText.get(2));
			
			checkPermutations(j);
			
		}
		
		//memoryTextList.addAll(memoryAdding);
		
		StringBuilder builder = new StringBuilder();
		//Vypis pamate
		for(String text : memoryTextList) {
			System.out.println("!! "+text);
			builder.append(text+"\n");
		}
		
		//Vypis pamate
		for(String text : memoryAdding) {
			System.out.println("++ "+text);
			builder.append(text+"\n");
		}		
		
		con.setMemoryText(builder.toString());		
		

	}
	
	private void processConditions(List<String> memoryTextList, List<String> conditionsList, int index) 
	{	
		for(int i =0; i<conditionsList.size(); i++) {			
			String condition = conditionsList.get(i);
			
			String conditionRegexed = condition.replaceAll("[?]{1}[A-Z]{1}","");
						
			conditionRegexed = removeEdgeSpaces(conditionRegexed);
			System.out.println("RULE: |"+conditionRegexed+"|");
			
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
					System.out.println("Permutation wrong");
					adding = false;
					break;
				}
			}
			
			if(adding && specialConditionsEdit.size() > 1) {
				
				for(int i =0;i<specialConditionsEdit.size();i++)
				{
					System.out.println("setting "+specialConditionsEdit.get(i)+" to "+nameVariables.get(specialConditionsEdit.get(i)));
					specialConditionsEdit.set(i,nameVariables.get(specialConditionsEdit.get(i)));
					
				}
				System.out.println("SIZE "+specialConditionsEdit.size());
				//Kontrola duplikacii
				for(int m=0;m<specialConditionsEdit.size();m++) {
					for(int n=m+1;n<specialConditionsEdit.size();n++) {
						System.out.println("M "+m+" | N "+n);
						System.out.println("1 "+specialConditionsEdit.get(m));
						System.out.println("2 "+specialConditionsEdit.get(n));
						if(specialConditionsEdit.get(m).equals(specialConditionsEdit.get(n))) {
							adding = false;
						}
					}
				}
			}
			
			
			if(adding) {
				System.out.println("Adding "+nameOfRule.get(index));
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
				
				processCommand(addingRule);

			}
		}
		
	}
	
	private void processCommand(String addingRule)
	{
		String[] tmp = addingRule.split(" ",2);
		String command = tmp[0];
		String other = tmp[1];
		
		//TODO dokoncit aj ostatne moznosti
		if(command.equals(ADD)) {
			boolean exists = false;
			
			for(String text:memoryTextList) {
				if(text.equals(other))
					exists=true;
			}
			
			if(!exists)
				memoryAdding.add(other);
		}
		
	}
	
	private void createPermutations(int i, List<String> list)
	{
		if(conditionsWithNames.size() <= i) {
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
	
	//TODO mozno je to mazanie medzier zbytocne :/
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
	
	
}
