

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class Main extends Application {

    Canvas canva = new Canvas(1200, 1200);

    GraphicsContext gc = canva.getGraphicsContext2D();

    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement ps = null;
     private static PreparedStatement ps2 = null;
    private static ResultSet rs = null;
    
    private String dbName="[HS-ULM\\boydedaev]"; 

   
    
     private static final String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
     private static final String connectionDescriptor =
      "jdbc:sqlserver://I-MSSQL-01;databasename=kratzer_db;IntegratedSecurity=true";
   
     
    

    public static void makeConnection() {
        // Open the database
        try {
 
            // Establish JDBC connection
              Class.forName(driverClass);
             con =  DriverManager.getConnection(connectionDescriptor,"","");

          
 

            // Create a statement
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws SQLException {

        Label selected = new Label("You select");

        ///Get Data from Map Table 
        ObservableList<String> data = FXCollections.observableArrayList(showNameCitys());

        ListView<String> ls = new ListView<String>(data);
        ls.prefWidth(100);
        ls.setMaxWidth(100);
        ls.setPrefSize(300, 350);// size 
        ls.setOrientation(Orientation.VERTICAL);//how will be shown

//listView 
        MultipleSelectionModel<String> mode = ls.getSelectionModel();

        mode.selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {

               

                try {
                    
                      gc.clearRect(0, 0, 1000, 1000);
                    ps = con.prepareStatement("SELECT Cfrom.PosX,Cfrom.PosY,Cto.PosX,Cto.PosY,R.Distance FROM"+dbName+".MAPab M\n"+
                             "INNER JOIN"+dbName+".ROADab R ON M.ID = R.MapID\n" +
                             "INNER JOIN"+dbName+".CITYab Cfrom ON Cfrom.MapID = M.ID AND Cfrom.ID = R.IDfrom\n" +
                             "INNER JOIN"+dbName+".CITYab Cto ON Cto.MapID = M.ID AND Cto.ID = R.IDto\n"+
                             "WHERE M.Name =? AND R.IDfrom < IDto");

                    selected.setText(newValue);
                    ps.setString(1, newValue);
                    rs = ps.executeQuery();

                    //then
                    while (rs.next()) {

                        gc.setStroke(Color.CADETBLUE);
                        gc.setLineWidth(1.0D);
                        gc.strokeLine(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4));
                       
                        gc.setTextAlign(TextAlignment.LEFT);
                        int middleX = (rs.getInt(1) +rs.getInt(3)) / 2;
                        int middleY = (rs.getInt(2) + rs.getInt(4)) / 2;
                        gc.strokeText(rs.getString(5), middleX+10,middleY);
 
                //or
                //gc.strokeLine(rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7));

                    }
                    
                        ps2 = con.prepareStatement("SELECT CITYab.Name,PosX,PosY From"+dbName+".CITYab\n" +
                                                    "Join"+dbName+".MAPab on MAPab.ID=CITYab.MapID\n" +
                                                    "Where MAPab.Name=?");
                        ps2.setString(1, newValue);
                        rs=ps2.executeQuery(); 
 
                        while (rs.next()) {

                            //make points
                            canva.getGraphicsContext2D().fillRect(rs.getDouble(2), rs.getDouble(3), 3, 3);
                            gc.fillRect(rs.getDouble(2) - 4, rs.getDouble(3) - 4, 9.0D, 9.0D);
                            
                            
                            //set Text 
                            gc.setTextAlign(TextAlignment.RIGHT);
                            gc.fillText(rs.getString(1), rs.getInt(2) +135, rs.getInt(3)+5 );
                            //gc.strokeText(rs.getString(1), rs.getInt(2) + 10, rs.getInt(3));

                           
                            
                        }
 
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

              
            }

        }
        );

        FlowPane root = new FlowPane();
        Scene scene = new Scene(root, 1200, 900);

        root.setOrientation(Orientation.VERTICAL);

        root.getChildren().add(ls);
        root.getChildren().add(selected);
        root.getChildren().add(canva);

        primaryStage.setTitle("Assigment 2");

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        makeConnection();
        launch(args);
    }

    private ArrayList showNameCitys() throws SQLException {

        ArrayList arrayList = new ArrayList();

        stmt = con.createStatement();

        rs = stmt.executeQuery("select Name from"+dbName+".MAPab");

        while (rs.next()) {

            arrayList.add(rs.getString(1));

        }

        return arrayList;

    }

}