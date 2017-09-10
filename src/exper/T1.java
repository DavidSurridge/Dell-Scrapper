package exper;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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

	public static String getCpuDescription() {
		return T1.cpuDescription;
	}

	public void setCpu(JsonObject input) {
		T1.CPU = getStringContaining("Core™ ", " ", getCpuDescription());
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

			String laptopModel = getStringContaining("/spd/", "", href);
			System.out.println(laptopModel);

			URL url = new URL("http://www.dell.com/csbapi/en-ie/productanavfilter/GetSystemsResults?ProductCode="
					+ laptopModel + "&page=1&pageSize=3&preview=");

			try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
				JsonObject obj = rdr.readObject();
				JsonArray results = obj.getJsonObject("Results").getJsonArray("Stacks");

				for (int i = 0; i < results.size(); i++) {
					JsonObject name = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i);
					s.setName(name);
					// .getJsonObject("Stack").getJsonObject("Title").get("Value").toString();
					// add details then append to array like structure to contain each instance.
					s.setPrice(name);
					s.setCpuDescription(name);

					String cpuDescription = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i)
							.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(0).get("Value").toString();
					String CPU = getStringContaining("Core™ ", " ", getCpuDescription());

					String operatingSysDescription = obj.getJsonObject("Results").getJsonArray("Stacks")
							.getJsonObject(i).getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(1)
							.get("Value").toString();

					String ramDescription = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i)
							.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(2).get("Value").toString();

					/*
					 * String RAM = getStringContaining("", ";", ramDescription); String ramSize =
					 * getStringContaining("", " ", RAM); String ramType = getStringContaining(" ",
					 * "-", RAM); int ramSpeed = Integer.parseInt(getStringContaining("-", "MHz",
					 * RAM));
					 */

					String diskDescription = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i)
							.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(3).get("Value").toString();

					String drives[] = diskDescription.split(" \\+ ");
					for (String drive : drives) {

						boolean SSD = drive.contains("Solid State Drive");
						boolean HDD = drive.contains("Hard Drive");
						String size = getStringContaining("", " ", drive);
					}

					String graphicsDescription = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i)
							.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(4).get("Value").toString();
					String graphicsModel = getStringContaining("", " ", graphicsDescription);

					String screenDescription = obj.getJsonObject("Results").getJsonArray("Stacks").getJsonObject(i)
							.getJsonObject("Specs").getJsonArray("TechSpecs").getJsonObject(5).get("Value").toString();
					// String screenSize = getStringContaining("", "-inch", screenDescription);
					String screenResolutionType = getStringContaining("-inch ", " (", screenDescription);

					System.out.println("name: " + s.getName());
					System.out.println("price: " + s.getPrice());
					/*
					 * System.out.println("CPU: " + CPU); System.out.println("RAMDESC: " +
					 * ramDescription); System.out.println("DISKDESC: " + diskDescription);
					 * System.out.println("GRAPHICSDESC: " + graphicsDescription);
					 * System.out.println("SCREENSDESC: " + screenDescription);
					 */System.out.println(" ");
				}
			}
		}
	}
}
