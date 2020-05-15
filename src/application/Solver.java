package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Solver {

	private final String ADD = "pridaj";
	private final String DELETE = "vymaz";
	private final String MESSAGE = "sprava";
	private final String QUESTION = "otazka";
	
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
	
	MenuController menuCon;
	boolean askedQuestion = false;

	//Toto sa vykona pri jednom kliku na tlacidlo. solveOnce je true ak vykona iba jeden krok
	public void solveItAll(MenuController con, boolean solveOnce) 
	{
		menuCon = con;
		while(true) {
			askedQuestion = false;
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
				System.out.println("CALLED "+helperList.get(0).split(" ",2)[1]);
				helperList.remove(0);
			}
			
			try {
				solveIt(ruleRawTextList);
			} catch (Exception e) {
				con.setMessageText("BAD RULES FORMATTING!");
				return;
			}
			
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
			
			if(solveOnce || askedQuestion || (con.getHelperText().length() <=0 && memoryTextCount == memoryTextList.size()))
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
			if(ruleRawTextList.get(j).length() == 0)  continue;
			
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
					if(isSubstring(condition,memoryString) || condition.equals(memoryString))
					{
						List<String> conditionList = new ArrayList<String>();
						conditionList.add(condition);
						conditionsWithNames.get(i).add(conditionList);
						System.out.println("NO ? FOUND");
					}
				}	
				continue;
			}
			
			String conditionRegexed = condition.replaceAll("[?]{1}[A-Z]{1}","");
						
			conditionRegexed = removeEdgeSpaces(conditionRegexed);
			
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
		System.out.println("ADDING RULE: "+addingRule);
		List<String> actionList = new ArrayList<String>();
		if(addingRule.contains(",")) {
			actionList = Arrays.asList(addingRule.split(","));
		}else {
			actionList.add(addingRule);
		}
		
		for(String rule: actionList) {
			System.out.println("RULE"+rule);
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
			else if(command.equals(QUESTION)) {
				System.out.println("Otazka!");
				boolean exists = false;
				String[] tmp2 = other.split("-",2);
				String action = removeEdgeSpaces(tmp2[1]);
				String question = tmp2[0];
				
				String rawAction = action.replaceAll("!", "");
				rawAction = removeEdgeSpaces(rawAction);
				
				String[] tmp3 = question.split(":",2);
				String questionAsk = removeEdgeSpaces(tmp3[0]);
				String[] questionOpt = removeEdgeSpaces(tmp3[1]).split(" ");
				
				for(String opt:questionOpt) {
					String actionTest = action.replaceAll("!", opt);
					for(String text:memoryTextList) {
						if(text.equals(actionTest)) {
							exists = true;
							System.out.println("EQ: "+text+"| "+actionTest);
						}
					}
				}
				if(!exists) {
					System.out.println("Found question!");
					System.out.println("Question "+questionAsk+"\nOptions: ");
					for(String text:questionOpt) {
						System.out.println(text);
					}
					askedQuestion = true;
					askQuestion(questionAsk,questionOpt,action);
				}
				
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
			else if(command.equals(QUESTION)) {
				System.out.println("Otazka!");
				boolean exists = false;
				String[] tmp2 = other.split("-",2);
				String action = removeEdgeSpaces(tmp2[1]);
				String question = tmp2[0];
				
				String rawAction = action.replaceAll("!", "");
				rawAction = removeEdgeSpaces(rawAction);
				
				String[] tmp3 = question.split(":",2);
				String questionAsk = removeEdgeSpaces(tmp3[0]);
				String[] questionOpt = removeEdgeSpaces(tmp3[1]).split(" ");
				
				for(String opt:questionOpt) {
					String actionTest = action.replaceAll("!", opt);
					for(String text:memoryTextList) {
						if(text.equals(actionTest)) {
							exists = true;
						}
					}
				}
				if(!exists) {
					validCommand = true;				
				}
			}
		}
		
		return validCommand;
		
	}
	
	private void createPermutations(int i, List<String> list)
	{
		if(conditionsWithNames.size() <= i) {
			if(list.size() > 0) {
				permutationResult.add(list);
				System.out.println("ADDED PERM "+list);
			}
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
	
	private void askQuestion(String questionName,String[] options,String action)
	{
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("QuestionPopup.fxml"));	
			QuestionShowController con = new QuestionShowController();
			loader.setController(con);
			VBox root = (VBox) loader.load();
			Scene scene = new Scene(root);		
			Stage stage = new Stage();
			
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Otazka");
			
			//Nastavuje prioritu. Neda sa vratit naspat dokial nezavru toto okno
			stage.initModality(Modality.APPLICATION_MODAL); 			
			stage.show();	
			
			con.setQuestion(questionName,options,action,stage,menuCon,this);
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}

//Toto je controller pre edit menu
class QuestionShowController
{
	@FXML MenuButton dropdown;
	@FXML Button saveBtn;
	@FXML Label questionLabel;
	
	public void setQuestion(String questionName,String[] options,String action,Stage stage, MenuController con, Solver solver) {
		questionLabel.setWrapText(true);
		questionLabel.setText(questionName);
		
		dropdown.setText("Choose one!");
		dropdown.getItems().clear();
		
		List<String> adding = new LinkedList<String>();
		
		if(options.length > 0) {
			for (String opt : options) {
				CheckBox cb0 = new CheckBox(opt);  
				cb0.setUserData(opt);
				CustomMenuItem item0 = new CustomMenuItem(cb0); 
				item0.setHideOnClick(false);  
				dropdown.getItems().add(item0);
				cb0.setSelected(false);
			}
		}
		
		saveBtn.setOnAction(e-> {		
			for (MenuItem custom : dropdown.getItems()) {
				CheckBox item = (CheckBox)((CustomMenuItem)custom).getContent();
				if(item.isSelected()) {
					String gotItem = (String)item.getUserData();
					con.setMemoryText(con.getMemoryText()+action.replaceAll("!", gotItem)+"\n");
				}
			}	
			solver.solveItAll(con, false);
			stage.close();
		});
	}

}
