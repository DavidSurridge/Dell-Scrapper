package exper;

import javax.json.JsonObject;

public class DellLaptopBuilder {
  private Laptop laptop;

  public DellLaptopBuilder(JsonObject input) {
    final String con1 = "Stack";  
    final String con2 = "Value";
    final String con3 = "Specs";
    final String con4 = "TechSpecs";
    String laptopModel = null;
    laptopModel = input.getJsonObject(con1)
        .getJsonObject("ProductCode")
        .get(con2)
        .toString();
    
    String name = null;
    name = input.getJsonObject(con1).getJsonObject("Title").get(con2).toString();
    
    Double price = null;
    price = Double.parseDouble(
        input.getJsonObject(con1).getJsonObject("Pricing")
        .getJsonObject("DellPrice").get("InnerValue").toString());

    String productSoldOut = null;
    productSoldOut = input.getJsonObject(con1).get("ProductSoldOut").toString();
    
    String itemIdentifier = null;    
    itemIdentifier = input.getJsonObject(con1).get("ItemIdentifier").toString();

    String ramDescription = null;
    String diskDescription = null;
    String cpuDescription = null;
    String graphicsDescription = null;
    String screenDescription = null;
    
    int size = input.getJsonObject(con3).getJsonArray(con4).size();
    for (int i = 0; i < size; i++) {
      String jsonLabel = input.getJsonObject(con3)
          .getJsonArray(con4)
          .getJsonObject(i)
          .get("Label")
          .toString();
      String jsonValue = input.getJsonObject(con3)
          .getJsonArray(con4)
          .getJsonObject(i)
          .get(con2)
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

    //to be filled in
    String operatingSysDescription = null;
    String graphicsModel = null;
    String cpu = null;
    
    laptop = new Laptop(laptopModel, 
        name, 
        price, 
        cpuDescription, 
        cpu, 
        operatingSysDescription, 
        ramDescription,
        diskDescription, 
        graphicsDescription, 
        graphicsModel, 
        screenDescription, 
        productSoldOut, 
        itemIdentifier);
    // ensure thread safe.
    // look into syncronise and alterenatives
  }

  public Laptop getLaptop() {
    return laptop;
  }
}