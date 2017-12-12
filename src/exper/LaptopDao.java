package exper;

import java.sql.Statement;
import java.util.Map;

public interface LaptopDao extends AutoCloseable {
  

  
  public Map<String, Double> getPrices();
  
  public void insertNewLaptopResults(Map<String, Laptop> newLaptopResults);
  
  public void close();
}
