package application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MenuController {

	@FXML
	TextArea memoryArea, ruleArea, helperArea, messageArea;
	@FXML
	Button solveButton, stepButton, loadFileButton;
	@FXML
	TextField fileNameField;
	
	Solver solver;
	
	public void initialize()
	{
		solver = new Solver();
		
		solveButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				callSolver();
			}
			
		});
		
		stepButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				callNextStep();
			}
			
		});
		
		loadFileButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				loadFile();
			}
			
		});
	}
	
	private void loadFile() {	
		String filePath = fileNameField.getText();
		if(filePath.isEmpty())	return;
		
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
        	ruleArea.setText("Subor nenajdeny");
            return;
        }
        
        ruleArea.setText(contentBuilder.toString());
	}
	
	private void callSolver() 
	{
		solver = new Solver();
		setMessageText("");
		setHelperText("");
		solver.solveItAll(this,false);
	}
	
	public void setMemoryText(String text)
	{
		memoryArea.setText(text);
	}
	
	public void setRulesText(String text)
	{
		ruleArea.setText(text);
	}
	
	public void setMessageText(String text)
	{
		messageArea.setText(text);
	}
	
	public void setHelperText(String text)
	{
		helperArea.setText(text);
	}
	
	
	public String getMemoryText()
	{
		return memoryArea.getText();
	}
	
	public String getRulesText()
	{
		return ruleArea.getText();
	}
	
	public String getMessageText()
	{
		return messageArea.getText();
	}
	
	public String getHelperText()
	{
		return helperArea.getText();
	}
	
	private void callNextStep()
	{
		//solver.makeOneStep(ruleArea.getText(), memoryArea.getText(), messageArea.getText(), helperArea.getText(),this);
		solver.solveItAll(this,true);
	}
}
