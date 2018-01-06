package exper;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DellScrape {
  
  private DellScrape() {}
  
  private static final Logger logger = LogManager.getLogger();
  private static String accountSid = System.getenv().get("TwilioACCOUNT_SID");
  private static String authToken = System.getenv().get("TwilioAUTH_TOKEN");
  private static Double priceDiffThreshold = 100.00;
  
  public static void main(String[] args) {
    handleRequest("");
  }
  
  /**
   * AWS Lambda handler that runs scrape code and sends results to SQL database .
   * @return String 
   */
  public static String handleRequest(String input) {

    try {
      doScrape();
    } catch (Exception e1) {
      logger.fatal("Error performing doScrape Method", e1);
    }
    return "finished";
  }
  
  /**
   * Load information from the Dell site. <br>
   * Uses DellLaptopBuilder to assist in processing information. <br>
   * Uses LaptopDaoImp to transmit information to DB. 
   * @throws MalformedURLException 
   * 
   */
  public static void doScrape() throws IOException {
    HashMap<String, Laptop> laptopsResults = new HashMap<>();
    Document response = Jsoup.connect("http://www.dell.com/ie/p/laptops?").get();
    Element container = response.getElementById("laptops");
    Elements laptops = container.getElementsByAttribute("data-testid");
    List<String> hrefs = laptops.eachAttr("href");
    
    try (LaptopDao connect = new LaptopDaoImpl()) {
      Map<String, Double> currentPrices = connect.getPrices();
     
      for (String href : hrefs) {
        String laptopModel = getStringContaining("/spd/", "", href);

        URL url = new URL("http://www.dell.com/csbapi/en-ie/productanavfilter/GetSystemsResults?ProductCode="
            + laptopModel + "&page=1&pageSize=60&preview=");

        try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
          JsonObject obj = rdr.readObject();
          JsonArray results = obj.getJsonObject("Results").getJsonArray("Stacks");

          for (int i = 0; i < results.size(); i++) {
            JsonObject input = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i);
            DellLaptopBuilder dellLaptopBulilder = new DellLaptopBuilder(input);
            Laptop laptop = dellLaptopBulilder.getLaptop();
            String laptopId = laptop.getItemIdentifier();
            double newPrice = laptop.getPrice();
            double currentPrice = currentPrices.isEmpty() ? 0 : currentPrices.get(laptopId);
            double priceDiff = currentPrice - newPrice;
             
            if ((newPrice > 0) && (priceDiff > 1 || priceDiff < -1)) {
              laptopsResults.put(laptopId, laptop);
            }

            if (priceDiff > priceDiffThreshold) {
              sendText(laptopId, newPrice, currentPrice);
            }
          }
        }
      }
      if (!laptopsResults.isEmpty()) {
        connect.insertNewLaptopResults(laptopsResults);
      }
    }
  }

  public static void sendText(String laptopModel, Double price, Double newPrice) {
    String messageContent = "Price change detected for " 
        + laptopModel + "."
        + "price from: " + price + "to: "
        + newPrice;

    Twilio.init(accountSid, authToken);

    try {
      MessageCreator messageCreator = Message.creator(new PhoneNumber("+353852112881"),
          new PhoneNumber("+353861801038"), messageContent);
      messageCreator.create();
    } catch (ApiException e) {
      logger.warn("Error in sendText Method", e);
    }
  }
  
  public static String getStringContaining(String start, String end, String string) {
    if (string.indexOf(start) != -1) {
      string = string.substring(string.indexOf(start) + start.length());
      if (!end.equals("")) {
        string = string.substring(0, string.indexOf(end));
      }
      return string;
    }
    return null;
  }
}
