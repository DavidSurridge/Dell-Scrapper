package exper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LaptopDaoImpl implements LaptopDao{
  private Connection conn = null;
  private Statement statement = null;
  private ResultSet resultSet = null;

  private final String daysDate =
      new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

  
  public LaptopDaoImpl() {
   
    String url = System.getenv().get("AWSdatabase");
    String user = System.getenv().get("AWSDbUser");
    String password = System.getenv().get("AWSDbPassword");
    
    try {
      conn = DriverManager.getConnection(url, user, password);
      statement = 
          conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }



  //get distinct results for the latest information for each model. 
  //put identifier and information into map(string, array).
  //
  @Override
  public Map<String, Double> getPrices() {
    final String sql = "SELECT " 
        + "Identifier, price " 
        + "FROM laptopInfo.dbo.laptopRecords " 
        + "order by date desc";
    Map<String, Double> map = new HashMap<>();
    
    try {
      resultSet = statement.executeQuery(sql);

      while (resultSet.next()) {
        map.put(resultSet.getString(1), resultSet.getDouble(2));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return map;
  }

  @Override
  public void insertNewLaptopResults(Map<String, Laptop> newLaptopResults) {
    final String laptopRecordsInsertHeader = "USE laptopInfo " 
        + "INSERT INTO [dbo].[laptopRecords]" 
        + "           ([Identifier]"
        + "           ,[date]" 
        + "           ,[laptopModel]" 
        + "           ,[price]" 
        + "           ,[laptoName])"
        + "     VALUES";
    
    final String valuesDummy = "('test1','" 
        + daysDate 
        + "','modelTest' " 
        + ",0000 " 
        + ",'nameTest')";
    
    StringBuilder sqlAddRecords = new StringBuilder();
    sqlAddRecords.append(laptopRecordsInsertHeader);
    for (Laptop laptopResult : newLaptopResults.values()) {
      String values = "('" + laptopResult.getItemIdentifier() 
                         + "','" + daysDate 
                         + "','" + laptopResult.getLaptopModel() 
                         + "'," + laptopResult.getPrice() 
                         + ",'" + laptopResult.getName() 
                         + "'),";
      sqlAddRecords.append(values);
    }
    sqlAddRecords.append(valuesDummy);

    try {
      statement.executeUpdate(sqlAddRecords.toString());
   
      statement.executeUpdate(
          "USE [laptopInfo]\r\n" 
          + "DELETE FROM [dbo].[laptopRecords]\r\n"
              + "      WHERE Identifier = 'test1'");
    } catch (SQLException e) {

      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      if (conn != null) {
        conn.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (resultSet != null) {
        resultSet.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
