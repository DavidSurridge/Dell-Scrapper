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

public class DellScrape {
	public static void main(String[] args) throws Exception {
		// while(true) {
		// Thread.sleep(5000);
		// TimeUnit.SECONDS.sleep(5);
		System.out.println("Start");

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
				}
			}
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
