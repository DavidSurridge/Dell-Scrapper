package exper;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class T1 {
	private static String name;
	private static Double price;
	private static String cpuDescription;
	private static String CPU;
	private static String operatingSysDescription;
	private static String ramDescription;
	private static String diskDescription;
	private static String graphicsDescription;
	private static String graphicsModel;
	private static String screenDescription;

	public T1() {
	}

	public void setName(JsonObject input) {
		T1.name = input.getJsonObject("Stack").getJsonObject("Title").get("Value").toString();
	}

	public String getName() {
		return T1.name;
	}

	public void setPrice(JsonObject input) {
		T1.price = Double.parseDouble(input.getJsonObject("Stack").getJsonObject("Pricing").getJsonObject("DellPrice")
				.get("InnerValue").toString());
	}

	public Double getPrice() {
		return T1.price;
	}

	public void setCpuDescription(JsonObject input) {
		T1.cpuDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(0).get("Value")
				.toString();
	}

	public String getCpuDescription() {
		return T1.cpuDescription;
	}

	public String getCPU() {
		return T1.CPU;
	}

	public void setCPU() {
		T1.CPU = getStringContaining("Core™ ", " ", getCpuDescription());
	}

	public String getRamDescription() {
		return T1.ramDescription;
	}

	public void setRamDescription(JsonObject input) {
		int i = 2;
		if (T1.name.contains("Alienware")) {
			i = 4;
		}
		if (T1.name.contains("Inspiron 15")) {
			i = 3;
		}

		T1.ramDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Value")
				.toString();
	}

	public String getOperatingSysDescription() {
		return T1.operatingSysDescription;
	}

	public void setOperatingSysDescription(JsonObject input) {
		T1.operatingSysDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(1)
				.get("Value").toString();
	}

	public String getDiskDescription() {
		return T1.diskDescription;
	}

	public void setDiskDescription(JsonObject input) {
		int i = 3;
		if (T1.name.contains("Alienware")) {
			i = 5;
		}
		if (T1.name.contains("Inspiron 15")) {
			i = 4;
		}
		T1.diskDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Value")
				.toString();
	}

	public String getGraphicsDescription() {
		return T1.graphicsDescription;
	}

	public void setGraphicsDescription(JsonObject input) {
		int i = 4;
		if (T1.name.contains("Alienware")) {
			i = 2;
		}
		if (T1.name.contains("Inspiron 15")) {
			i = 5;
		}
		T1.graphicsDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Value")
				.toString();
	}

	public String getGraphicsModel() {
		return T1.graphicsModel;
	}

	public void setGraphicsModel() {
		T1.graphicsModel = getStringContaining("", " ", getGraphicsDescription());
	}

	public String getScreenDescription() {
		return T1.screenDescription;
	}

	public void setScreenDescription(JsonObject input) {
		int i = 5;
		if (T1.name.contains("Alienware")) {
			i = 3;
		}
		if (T1.name.contains("Inspiron 15 3000")) {
			i = 5;
		}
		/*if (T1.name.contains("Inspiron 15") & !T1.name.contains("Inspiron 15 3000")) {
			i = 6;
		}*/
		T1.screenDescription = input.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(i).get("Value")
				.toString();
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

	public static void main(String[] args) throws Exception {
		T1 s = new T1();

		Document response = Jsoup.connect("http://www.dell.com/ie/p/laptops?").get();
		Element container = response.getElementById("laptops");
		Elements laptops = container.getElementsByAttribute("data-testid");
		List<String> hrefs = laptops.eachAttr("href");

		for (String href : hrefs) {
			//make getter and setter methods for laptop model
			String laptopModel = getStringContaining("/spd/", "", href);
			//filter some setter methods using the laptop model variable
			System.out.println("Laptop model: " + laptopModel);

			URL url = new URL("http://www.dell.com/csbapi/en-ie/productanavfilter/GetSystemsResults?ProductCode="
					+ laptopModel + "&page=1&pageSize=3&preview=");

			try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
				JsonObject obj = rdr.readObject();
				JsonArray results = obj.getJsonObject("Results").getJsonArray("Stacks");

				for (int i = 0; i < results.size(); i++) {
					JsonObject input = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i);
					s.setName(input);
					s.setPrice(input);
					s.setCpuDescription(input);
					s.setCPU();
					s.setDiskDescription(input);
					s.setRamDescription(input);
					s.setGraphicsDescription(input);
					s.setGraphicsModel();
					s.setScreenDescription(input);
					if(T1.name.toLowerCase().contains("inspiron")) {
					System.out.println("name: " + s.getName());
					System.out.println("price: " + s.getPrice());
					System.out.println("CPUdesc: " + s.getCpuDescription());
					System.out.println("CPU: " + s.getCPU());
					System.out.println("diskDesc: " + s.getDiskDescription());
					System.out.println("ramDesc: " + s.getRamDescription());
					System.out.println("graphicsDesc: " + s.getGraphicsDescription());
					System.out.println("graphicsModel: " + s.getGraphicsModel());
					System.out.println("screenDesc: " + s.getScreenDescription());
					System.out.println(" ");}
				}
			}
		}
	}
}
