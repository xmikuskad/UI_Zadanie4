package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class MenuController {

	@FXML
	TextArea memoryArea, ruleArea;
	@FXML
	Button solveButton, stepButton;
	
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
	}
	
	private void callSolver() 
	{
		String result = solver.Solve(ruleArea.getText(), memoryArea.getText());
		memoryArea.setText(result);
	}
	
	private void callNextStep()
	{
		//TODO next step function
		System.out.println("Next Step called");
	}
}
