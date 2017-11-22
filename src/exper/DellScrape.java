package exper;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.*;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import org.glassfish.json.*;

public class DellScrape {

  private static String ACCOUNT_SID = null;
  private static String AUTH_TOKEN = null;
  private static String searchId = "cnx95605";
  private static Double priceDiffThreshold = 100.00;
  private static Double priceGoalThreshold = 1000.00;
  private String setDate = null;
  private static String url = null;
  private static String user = null;
  private static HashMap<String, Laptop> previousResults = null;
  private static String password = null;
  
  private final String daysDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
  private final String valuesDummy = "('test1','" 
      + daysDate 
      + "','modelTest' " 
      + ",0000 " 
      + ",'nameTest')";

  private final Long createYestDate = Date.valueOf(daysDate).getTime() - 1;
  private final String yesterdaysDate = new SimpleDateFormat("dd-MM-yyyy").format(createYestDate);
  
  public static Map<String, Laptop> doScrape() throws Exception {
    System.out.println("Start");
    HashMap<String, Laptop> laptopsResults = new HashMap<>();
    Document response = Jsoup.connect("http://www.dell.com/ie/p/laptops?").get();
    Element container = response.getElementById("laptops");
    Elements laptops = container.getElementsByAttribute("data-testid");
    List<String> hrefs = laptops.eachAttr("href");

    for (String href : hrefs) {
      String laptopModel = getStringContaining("/spd/", "", href);
      System.out.println("Laptop model: " + laptopModel);

      URL url = new URL("http://www.dell.com/csbapi/en-ie/productanavfilter/GetSystemsResults?ProductCode="
          + laptopModel + "&page=1&pageSize=60&preview=");

      try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
        JsonObject obj = rdr.readObject();
        JsonArray results = obj.getJsonObject("Results").getJsonArray("Stacks");

        for (int i = 0; i < results.size(); i++) {
          JsonObject input = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i);
          DellParse dellParser = new DellParse(input);
          Laptop laptop = dellParser.getLaptop();
          laptopsResults.put(laptop.getItemIdentifier(), laptop);
        }
      }
    }
    return laptopsResults;
  }

  private static void setCredentials() {
    System.out.println(System.getenv().get("AWSdatabase"));
    System.out.println(System.getenv().get("AWSDbPassword"));
    System.out.println(System.getenv().get("AWSDbUser"));
    System.out.println(System.getenv().get("TwilioACCOUNT_SID"));
    System.out.println(System.getenv().get("TwilioAUTH_TOKEN"));

    url = System.getenv().get("AWSdatabase");
    user = System.getenv().get("AWSDbUser");
    password = System.getenv().get("AWSDbPassword");
    ACCOUNT_SID = System.getenv().get("TwilioACCOUNT_SID");
    AUTH_TOKEN = System.getenv().get("TwilioAUTH_TOKEN");

  }

  private static void loadResultsToDb() {
  }

  public String handleRequest(String input) {

    setCredentials();

    Map<String, Laptop> newLaptopResults = null;
    try {
      newLaptopResults = doScrape();
    } catch (Exception e1) {
      System.out.println("Error performing doScrape Method");
      e1.printStackTrace();
    }

    
    String sql = "SELECT TOP (1) "
        + "Identifier, date, laptopModel, price, laptoName "
        + "FROM laptopInfo.dbo.laptopRecords "
        + "where identifier = '"
        + searchId + "'order by date desc";
    
    
    // connect to db
    try (Connection conn = DriverManager.getConnection(url, user, password);
        Statement statement = 
        conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);) { 

      if (resultSet.last()) {
        setDate = resultSet.getString(2);
      }
      // load results to db
      if (!daysDate.equals(setDate)) {
        StringBuilder sqlAddRecords = new StringBuilder();
        sqlAddRecords.append(
            "USE laptopInfo " 
            +  "INSERT INTO [dbo].[laptopRecords]" 
            + "           ([Identifier]" 
            + "           ,[date]"
            + "           ,[laptopModel]" 
            + "           ,[price]" 
            + "           ,[laptoName])" 
            + "     VALUES");

        if (newLaptopResults != null && !newLaptopResults.isEmpty()) {
          for (Laptop laptopResult : newLaptopResults.values()) {
            String values = "('" + laptopResult.getItemIdentifier() 
                               + "','" + daysDate 
                               + "','" + laptopResult.getLaptopModel() 
                               + "'," + laptopResult.getPrice() 
                               + ",'" + laptopResult.getName() 
                               + "'),";
            sqlAddRecords.append(values);
          }
          // clean
          //set as class variable to final value
  
          sqlAddRecords.append(valuesDummy);

          statement.executeUpdate(sqlAddRecords.toString());
          statement.executeUpdate(
              "USE [laptopInfo]\r\n" 
                + "DELETE FROM [dbo].[laptopRecords]\r\n" 
                + "      WHERE Identifier = 'test1'");
        }
      }

      // perhaps set it so that db only updated on price change. if price change high
      // enough then send text?


      // for (Laptop laptopResult : newLaptopResults.values()) {
      // searchId = laptopResult.getItemIdentifier();
      // }
      // initialize hashmap of search ids and threshold vaule to check here and start
      // loop?
      String sqlTwilio = "SELECT TOP 1 price FROM laptopInfo.dbo.laptopRecords where identifier = '" + searchId
          + "' and date > Convert(datetime,  '" + yesterdaysDate + "' ,103 ) order by date desc";
      ResultSet pricecheck = statement.executeQuery(sqlTwilio);
      if (pricecheck.last()) {

        // add boolean to only send text once?

        Double price = Double.parseDouble(pricecheck.getString(1));
        Double newPrice = newLaptopResults.get(searchId).getPrice();

        if (price != null && searchId != null) {
          if (price - newPrice > priceDiffThreshold || newPrice < priceGoalThreshold) {
            StringBuilder sqlAddRecords = new StringBuilder();
            // consider moving the below to above the if statements and replace with text
            sqlAddRecords.append("USE laptopInfo " + "INSERT INTO [dbo].[laptopRecords]" + "           ([Identifier]"
                + "           ,[date]" + "           ,[laptopModel]" + "           ,[price]"
                + "           ,[laptoName])" + "     VALUES");

            String values = "('" + newLaptopResults.get(searchId).getItemIdentifier() + "','" + daysDate + "','"
                + newLaptopResults.get(searchId).getLaptopModel() + "'," + newLaptopResults.get(searchId).getPrice()
                + ",'" + newLaptopResults.get(searchId).getName() + "'),";
          }
        }
        System.out.print(price + " " + newPrice);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("finished");
    return "finished all";
    // test by using 2 different search ids
    // consider method for search criteria, return price check on multiple laptop

    /*
     * if (previousResults != null & searchId != null) { if
     * (((previousResults.get(searchId).getPrice()) -
     * (newLaptopResults.get(searchId).getPrice()) > priceDiffThreshold) ||
     * (newLaptopResults.get(searchId).getPrice()) < priceGoalThreshold ) {
     * 
     * // just send an email instead? String messageContent =
     * "Price change detected for " +
     * newLaptopResults.get(searchId).getLaptopModel() + "." + "price from: " +
     * previousResults.get(searchId).getPrice() + "to: " +
     * newLaptopResults.get(searchId).getPrice();
     * 
     * Twilio.init(ACCOUNT_SID, AUTH_TOKEN); MessageCreator messageCreator =
     * Message.creator(new PhoneNumber("+353852112881"), new
     * PhoneNumber("+353861801038"), messageContent); Message message =
     * messageCreator.create(); System.out.println(message.getSid()); // add boolean
     * to stop sending repeatedly when priceGoalThreshold is reached. }
     * previousResults = newLaptopResults; }
     */
  }

  public static String getStringContaining(String start, String end, String string) {
    String substringStringFromStart = null;
    String substringStringToEnd = null;
    if (string.indexOf(start) != -1) {
      substringStringFromStart = string.substring(string.indexOf(start) + start.length());
      if (!end.equals("")) {
        substringStringToEnd = string.substring(0, string.indexOf(end));
      }
      return substringStringToEnd ;
    }
    return null;
  }
}
