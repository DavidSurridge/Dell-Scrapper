package exper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class LaptopDaoImpl implements LaptopDao {
  private Connection conn = null;
  private Statement statement = null;
  private ResultSet resultSet = null;

  private final String daysDate =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
  
  /**
   * Implementation of Data Access Object to store and retrieve data 
   * stored in an AWS Relational Database table. <br><br>
   * The constructor method looks for the database URL and credentials
   * stored in the system environment.
   *  
   */
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

  @Override
  public Map<String, Double> getPrices() {
   
    final String sql = "select  \r\n" 
        + "Identifier, \r\n"  
        + "price\r\n"  
        + "from\r\n"  
        + "(\r\n" 
        + "select \r\n" 
        + " Identifier, \r\n"  
        + "     price,\r\n"  
        + "    date,\r\n" 
        + "       max(date) over (partition by Identifier) max_my_date\r\n"  
        + "  FROM laptopInfo.dbo.laptopRecords \r\n"  
        + ")  a\r\n" 
        + "where date = max_my_date \r\n" ;
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
        
    StringJoiner sqlAddRecords = new StringJoiner(", ");
    for (Laptop laptopResult : newLaptopResults.values()) {
      
      String values = "('" 
          + laptopResult.getItemIdentifier() 
          + "','" + daysDate 
          + "','" + laptopResult.getLaptopModel() 
          + "'," + laptopResult.getPrice() 
          + ",'" + laptopResult.getName() 
          + "')";
      sqlAddRecords.add(values);
    }
    
    String sqlStatment = laptopRecordsInsertHeader + sqlAddRecords.toString(); 
    try {
      statement.executeUpdate(sqlStatment);
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
