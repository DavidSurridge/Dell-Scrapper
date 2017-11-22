package exper;

import javax.json.JsonObject;

public class DellParse {
  private Laptop laptop;

  public DellParse(JsonObject input) {
    String name = null;
    Double price = null;
    String ramDescription = null;
    String diskDescription = null;
    String cpuDescription = null;
    String graphicsDescription = null;
    String screenDescription = null;
    String CPU = null;
    String laptopModel = null;
    String operatingSysDescription = null;
    String graphicsModel = null;
    String productSoldOut = null;
    String itemIdentifier = null;

    laptopModel = input.getJsonObject("Stack").getJsonObject("ProductCode").get("Value").toString();
    name = input.getJsonObject("Stack").getJsonObject("Title").get("Value").toString();
    price = Double.parseDouble(
        input.getJsonObject("Stack").getJsonObject("Pricing").getJsonObject("DellPrice").get("InnerValue").toString());

    productSoldOut = input.getJsonObject("Stack").get("ProductSoldOut").toString();
    itemIdentifier = input.getJsonObject("Stack").get("ItemIdentifier").toString();

    int size = input.getJsonObject("Specs").getJsonArray("TechSpecs").size();
    for (int i = 0; i < size; i++) {

      String jsonLabel = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Label")
          .toString();
      String jsonValue = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Value")
          .toString();

      if (jsonLabel.contains("Memory")) {
        ramDescription = jsonValue;
      }

      if (jsonLabel.contains("Hard Drive")) {
        diskDescription = jsonValue;
      }

      if (jsonLabel.contains("Processor")) {
        cpuDescription = jsonValue;
      }

      if (jsonLabel.contains("Video Card")) {
        graphicsDescription = jsonValue;
      }

      if (jsonLabel.contains("Display")) {
        screenDescription = jsonValue;
      }
    }
    laptop = new Laptop(laptopModel, name, price, cpuDescription, CPU, operatingSysDescription, ramDescription,
        diskDescription, graphicsDescription, graphicsModel, screenDescription, productSoldOut, itemIdentifier);
    // add function to send laptop details to the database. ensure database method
    // and parse class thread safe.
    // look into syncronise and alterenatives
  }

  public Laptop getLaptop() {
    return laptop;
  }
}