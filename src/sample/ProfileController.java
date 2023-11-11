package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import sample.model.Datasource;
import sample.model.User;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProfileController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField lastField;
    @FXML
    private TextField firstField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TableView bookingHistoryView;
    @FXML
    private Circle circleImage;

    private File selectedImage;
    private String path;

    private User user;
    private Image profileImage;

    private final String default_image = "file:/src/sample/picture/user-default.png";

    public void initialize(User currentUser){
        user = currentUser;

        path = currentUser.getImagePath();
        profileImage = new Image("file:" + path);

        usernameField.setText(currentUser.getUsername());
        lastField.setText(currentUser.getLastName());
        firstField.setText(currentUser.getFirstName());
        emailField.setText(currentUser.getEmail());
        passwordField.setText(currentUser.getPassword());

//        if(path != null){
//            circleImage.setFill(new ImagePattern(profileImage));
//        }else{
//            circleImage.setFill(new ImagePattern(new Image(default_image)));
//        }

        bookingHistoryView.setItems(Datasource.getInstance().queryBookingHistory(user.getId()));
    }

    @FXML
    public void showBookingHistory(){
        bookingHistoryView.setVisible(true);
    }

    @FXML
    public void cancel(){
        usernameField.getScene().getWindow().hide();
    }

    @FXML
    public void saveInformation(){
        String username = usernameField.getText();
        String firstName = firstField.getText();
        String lastName = lastField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        // copy image and save the default image url
        copyProfilePicture();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setImagePath(path);

        if(Datasource.getInstance().updateUser(firstName, lastName, username, password, email, user.getId(), path)){

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Обновить информацию");
            alert.setContentText("Успешное обновление информации");
            alert.showAndWait();
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setContentText("Не получилось обновить информацию");
            alert.showAndWait();
        }

    }

    @FXML
    public void uploadImageClick(ActionEvent event) throws IOException {

        try {
            FileChooser fc = new FileChooser();
            selectedImage = fc.showOpenDialog(null);
            // checking that input file is not null and handling the exception
            if (selectedImage == null)
                return;
            else if (ImageIO.read(selectedImage) == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Загрузите фото в JPG или PNG формате!",
                        ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {
                    return;
                }
            } else {
                Image img = SwingFXUtils.toFXImage(ImageIO.read(selectedImage), null);
//                posterImage.setImage(img);
                profileImage = img;
                circleImage.setFill(new ImagePattern(profileImage));
                path = selectedImage.getPath();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
    }

    public void copyProfilePicture(){

        Path source = Paths.get(path);
        Path destination = Paths.get("//src//sample//picture//" + source.getFileName());
        path = destination.toString();
        try {
            Files.copy(source, destination);
        }catch (IOException e){
            e.getStackTrace();
        }

    }
}
