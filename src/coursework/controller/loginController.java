package coursework.controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import coursework.model.Alert;
import coursework.model.DatabaseConnection;
import coursework.model.LoadStage;
import coursework.model.Notification;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class loginController implements Initializable {

    @FXML
    private Label userLogin;
    @FXML
    private TextField usernameTextField;
    @FXML
    private Label welcome;
    @FXML
    private TextField passwordTextField;
    @FXML
    private JFXButton login;
    @FXML
    private Label username;
    @FXML
    private Label password;
    @FXML
    private Label close;
    @FXML
    private Label minimize;
    @FXML
    private AnchorPane parentroot;
    public static int userID;
    public static String userName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    private boolean validateFields() {
        if (usernameTextField.getText().isEmpty() || passwordTextField.getText().isEmpty()) {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Ошибка", "Заполните все поля!");
            return false;
        } else {
            return true;
        }
    }

    private void clearFields() {
        usernameTextField.clear();
        passwordTextField.clear();
    }

    @FXML
    private void close(MouseEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void minimize(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void login(ActionEvent event) throws IOException {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        String query = "SELECT * FROM User WHERE Username = ? AND Password = ?";
        if (validateFields() && checkIfAccountExist()) {
            try {
                conn = DatabaseConnection.Connect();
                pre = conn.prepareStatement(query);
                pre.setString(1, usernameTextField.getText().trim());
                pre.setString(2, passwordTextField.getText().trim());
                rs = pre.executeQuery();
                if (rs.next()) {
                    switch (rs.getString("Usertype")) {
                        case "Administrator": {
                            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Message", "Access granted");
                            LoadStage loadStage = new LoadStage("/coursework/view/adminPanel.fxml", close);
                            Thread thread = new Thread(new showMessage(rs.getString(4)));
                            thread.setDaemon(true);
                            thread.start();
                            break;
                        }
                        case "Librarian": {
                            userID = rs.getInt(1);
                            userName = rs.getString(4);
                            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Message", "Access granted");
                            LoadStage loadStage = new LoadStage("/coursework/view/main.fxml", close);
                            Thread thread = new Thread(new showMessage(rs.getString(4)));
                            thread.setDaemon(true);
                            thread.start();
                            break;
                        }
                    }
                } else {
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Information", "Логин или пароль введены неверно");
                }
            } catch (SQLException ex) {
                System.err.println(ex);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (pre != null) {
                        pre.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    System.err.println(ex);
                }
            }
        }
    }

    @FXML
    private void loadPasswordRetrivalPanel(MouseEvent event) throws IOException {
        LoadStage stage = new LoadStage("/coursework/view/forgetPassword.fxml", close);
    }

    @FXML
    private void createAccount(MouseEvent event) throws IOException {
        LoadStage stage = new LoadStage("/coursework/view/createAccount.fxml", close);
    }

    @FXML
    private void signIn(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            login(new ActionEvent());
        }
    }

    private boolean checkIfAccountExist() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String selectQuery = "SELECT COUNT(*) FROM User";
        try {
            connection = DatabaseConnection.Connect();
            preparedStatement = connection.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();
            int numberOfRows = resultSet.getInt(1);
            if (numberOfRows == 0) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Information", "There is no user account, create a new account");
                clearFields();
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(loginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private class updateFeeAfterADay implements Runnable {


        LocalTime localTime = LocalTime.MAX;
        LocalTime time = LocalTime.now();
        long minutes = ChronoUnit.MINUTES.between(LocalTime.now(), LocalTime.MAX);
        long restingTime = minutes * 60000;

        @Override
        public void run() {
            try {
                System.out.println(minutes);
                while (true) {
                    Thread.sleep(restingTime);

                    long m = ChronoUnit.MINUTES.between(LocalTime.now(), LocalTime.MAX);
                    if (m == 0) {
                        long m2 = ChronoUnit.MINUTES.between(LocalTime.now().plusMinutes(1), LocalTime.MAX);
                        restingTime = m2 * 60000;
                        System.out.println(m2);
                    } else {
                        restingTime = m * 60000;
                        System.out.println(m);
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(loginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class showMessage extends Task<Void> {
        private final String username;
        public showMessage(String username){
            this.username = username;
        }

        @Override
        protected Void call() throws Exception {
            Thread.sleep(1500);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Notification notification = new Notification("Message", "Welcome "+username, 3);
                }
            });
            return null;
        }
    }

    private void requestFocus(TextField field) {
        field.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.DOWN) {
                    passwordTextField.requestFocus();
                }
                if (event.getCode() == KeyCode.UP) {
                    usernameTextField.requestFocus();
                }
            }
        });
    }
}
