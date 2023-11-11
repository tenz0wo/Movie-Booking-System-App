package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.model.Datasource;

public class SignupController {
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button signUpBtn;

    @FXML
    public void processSignUp(){
        String firstName = firstnameField.getText().trim();
        String lastName = lastnameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        if(firstName.isEmpty() && lastName.isEmpty() && username.isEmpty() && password.isEmpty() && email.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Внимание");
            alert.setHeaderText("Пожалуйста заполните все поля");
            alert.setContentText(null);
            alert.showAndWait();
        }else{
            if(Datasource.getInstance().addUser(firstName, lastName, username, password, email)){
                signUpBtn.getScene().getWindow().hide();
                System.out.println("Успешное добавление");
            }
        }
    }

    @FXML
    public void cancel(){
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
