package exper;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

public class T1 {

	public static String getStringContaining(String start, String end, String string) {
		if (string.indexOf(start) != -1) {
			string = string.substring(string.indexOf(start) + start.length());
			string = string.substring(0, string.indexOf(end));
			return string;
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		Document response = Jsoup.connect("http://www.dell.com/ie/p/laptops?").get();

		Elements laptopTypes = response.getElementsByClass("c4 seriesOptions");
		for (Element laptops : laptopTypes) {
			Elements hrefHtml = laptops.getElementsByAttribute("href");
			List<String> hrefs = hrefHtml.eachAttr("href");
			for (String href : hrefs) {
				String laptopUrL = "http://www.dell.com" + href;
				response = Jsoup.connect(laptopUrL).get();
				Elements laptopModels = response.getElementsByClass("configStackBody highlightSpecs");
				Element name = response.getElementById("mastheadPageTitle");

				for (Element laptop : laptopModels) {
					Elements singles = laptop.children();
					// figure out better way of extracting children
					for (Element single : singles) {

						if (!name.toString().contains("Alienware")) {
							// CPU
							String CPU = single.getElementsByAttributeValue("data-specindex", "1").eachText().get(0);
							CPU = getStringContaining("Core™ ", " ", CPU);
							
							// Ram							
							String RAM = single.getElementsByAttributeValue("data-specindex", "7").eachText().get(0);
							RAM = getStringContaining("", "z", RAM);
							System.out.println(name.text());
							System.out.println(CPU);
							System.out.println(RAM + "z");
							
							// SSD/HDD
							String diskDescription = single.getElementsByAttributeValue("data-specindex", "9").eachText().get(0);
							boolean SSD = diskDescription.contains("Solid State Drive");
							boolean HDD = diskDescription.contains("Hard Drive");
							String drives[] = diskDescription.split(" \\+ ");
							for (String drive : drives) {
								// mapper to add each drive, size and type
								String size = getStringContaining("", " ", drive);
							}
							System.out.println("SSD: " + SSD + " HDD: " + HDD);
							// Graphics or Alienware SSD
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "13").eachText().get(0));
							// screen
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "15").eachText().get(0));
							// price
							System.out.println(single.getElementsByClass("pLine dellPrice").eachText().get(0));
							System.out.println(" ");
						} else {
							System.out.println(name.text());
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "1").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "11").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "13").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "5").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "9").eachText().get(0));
							System.out.println(" ");
						}
					}
				}
			}
		}
	}
}
