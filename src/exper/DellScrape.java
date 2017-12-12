package exper;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import java.io.InputStream;
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
  private static final Logger logger = LogManager.getLogger();
  private static String accountSid = null;
  private static String authToken = null;
  private static Double priceDiffThreshold = 100.00;
  private static  Map<String, Laptop> newLaptopResults = null;
  
  /**
   * Scrape of Dell site for pricing info.
   * @return map 
   * @throws Exception if cannot connect to dell site then throws generic Exception
   */
  public static Map<String, Laptop> doScrape() throws Exception {
    HashMap<String, Laptop> laptopsResults = new HashMap<>();
    Document response = Jsoup.connect("http://www.dell.com/ie/p/laptops?").get();
    Element container = response.getElementById("laptops");
    Elements laptops = container.getElementsByAttribute("data-testid");
    List<String> hrefs = laptops.eachAttr("href");
    
    for (String href : hrefs) {
      String laptopModel = getStringContaining("/spd/", "", href);

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
    accountSid = System.getenv().get("TwilioACCOUNT_SID");
    authToken = System.getenv().get("TwilioAUTH_TOKEN");
  }
  
  public static void main(String[] args) {
    handleRequest("");
  }
  
  public static String handleRequest(String input) {

    setCredentials();
    try {
      newLaptopResults = doScrape();
    } catch (Exception e1) {
      logger.fatal("Error performing doScrape Method", e1);
    }

    if (newLaptopResults != null && !newLaptopResults.isEmpty()) {
      try (LaptopDao connect = new LaptopDaoImpl()) {
        Map<String, Double> currentPrice = connect.getPrices();

        if (currentPrice != null && !currentPrice.isEmpty()) {
          newLaptopResults.forEach((identifier, laptop) -> {

            double priceDiff = currentPrice.get(identifier) - laptop.getPrice();

            if (priceDiff > -1 && priceDiff < 1) {
              newLaptopResults.remove(identifier);

            } else if (priceDiff > priceDiffThreshold) {
              sendText(laptop.getName(), laptop.getPrice(), currentPrice.get(identifier));
            }
          });
        }
        connect.insertNewLaptopResults(newLaptopResults);
      }
    }
    return "finished all";
  }

  public static void sendText(String laptopModel, Double price, Double newPrice) {
    String messageContent = "Price change detected for " 
        + laptopModel + "."
        + "price from: " + price + "to: "
        + newPrice;

    Twilio.init(accountSid, authToken);
    MessageCreator messageCreator = Message.creator(new PhoneNumber("+353852112881"),
          new PhoneNumber("+353861801038"), messageContent);
    messageCreator.create();
    
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
