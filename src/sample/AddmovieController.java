package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sample.model.Datasource;
import sample.model.Movie;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class AddmovieController {

    private File selectedImage;
    @FXML
    private ImageView posterImage;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private TableView<Movie> tableView;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ComboBox<String> hourBox;
    @FXML
    private ComboBox<String> minutesBox;

    private String absPath = "";
    private String pathInDB = "";

    private ObservableList<Movie> movies;
    private ObservableList<String> listHours = FXCollections.observableArrayList();
    private ObservableList<String> listMinutes = FXCollections.observableArrayList("00", "15", "30", "45");

    private ContextMenu contextMenu;

    public void initialize(){

        // generate time
        generateTime(listHours, 0, 23);
        hourBox.setItems(listHours);
        minutesBox.setItems(listMinutes);

        movies = Datasource.getInstance().listMovies();
        tableView.setItems(movies);
        contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Удалить фильм");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Movie movie = tableView.getSelectionModel().getSelectedItem();
                deleteMovie(movie);
            }
        });

        contextMenu.getItems().add(deleteMenuItem);
        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2){
                    Movie selectedMovie = tableView.getSelectionModel().getSelectedItem();

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    Dialog<ButtonType> dialog = new Dialog<>();
                    fxmlLoader.setLocation(getClass().getResource("view/editmovie.fxml"));

                    try{
                        dialog.getDialogPane().setContent(fxmlLoader.load());
                    }catch (IOException e){
                        e.getStackTrace();
                    }
                    EditmovieController editmovieController = fxmlLoader.getController();
                    editmovieController.processResult(selectedMovie);

                    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

                    Optional<ButtonType> results = dialog.showAndWait();
                    if(results.isPresent() && results.get() == ButtonType.OK){
                        editmovieController.updateMovie();
                        refreshTable();
                    }


                }
            }
        });
        tableView.setContextMenu(contextMenu);
    }

    @FXML
    public void addMovie(){

        String title = titleField.getText();
        System.out.println("Заголовок: " + title);
        String desc = descField.getText();
        String date = null;

        String valueHour = hourBox.getValue();
        String valueMinute = minutesBox.getValue();
        String time = (valueHour + ":" + valueMinute);

        if(datePicker.getValue() != null){
            date = datePicker.getValue().toString();
        }

        if(title.isEmpty() || desc.isEmpty() || date == null || absPath.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Предупреждение");
            alert.setContentText("Пожалуйста заполните все поля");
            alert.showAndWait();
        }else{
            // copy poster and evaluate the path for database
            copyFilmPoster();
            if(Datasource.getInstance().addMovie(title,desc, date, time, pathInDB)){
                resetFields();
                System.out.println("Добавление успешное");
            }

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
                posterImage.setImage(img);
                absPath = selectedImage.getPath();
                System.out.println(absPath);
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

    }

    public void copyFilmPoster(){
        absPath = selectedImage.getPath();

        Path source = Paths.get(absPath);
        Path destination = Paths.get("//src//sample//poster//" + source.getFileName());
        pathInDB = destination.toString();

        try {
            Files.copy(source, destination);
        }catch (IOException e){
            e.getStackTrace();
        }

    }

    public void deleteFilmPoster(String deletePath){
        Path deleteImage = Paths.get(deletePath);
        try{
            Files.delete(deleteImage);
            System.out.println("Удалить фото: " + deletePath);
        }catch (IOException e){
            e.getStackTrace();
        }
    }

    @FXML
    public void cancel(){
        Platform.exit();
    }

    public void refreshTable(){
        movies = Datasource.getInstance().listMovies();
        tableView.setItems(movies);
    }
    @FXML
    public void back(){
        mainBorderPane.getScene().getWindow().hide();

    }

    public void resetFields(){
        posterImage.setImage(new Image("sample/images/defaultPoster.png"));
        absPath = "";
        titleField.setText("");
        descField.setText("");
        datePicker.setValue(null);
    }

    public void deleteMovie(Movie movie){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удалить фильм");
        alert.setContentText("Вы уверены в удалении: " + movie.getTitle() );
        Optional<ButtonType> results = alert.showAndWait();
        if(results.isPresent() && results.get() == ButtonType.OK) {
            if (Datasource.getInstance().deleteMovieById(movie.getId())) {
                deleteFilmPoster(movie.getUrl());
                refreshTable();
            }
        }
    }

    public void generateTime(ObservableList<String> observableList, int start, int end){
        for(int i = start; i <= end; i++){
            observableList.add(String.format("%02d", i));
        }
    }

}
