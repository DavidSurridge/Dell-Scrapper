package exper;

import java.net.*;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;

public class T1 {

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

				if (!name.toString().contains("Alienware")) {
					for (Element laptop : laptopModels) {
						Elements singles = laptop.children();
						for (Element single : singles) {
							// figure out better way of extracting children
							System.out.println(name.text());
							
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "7").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "9").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "13").eachText().get(0));
							System.out.println(
									single.getElementsByAttributeValue("data-specindex", "15").eachText().get(0));
							System.out.println(single.getElementsByClass("pLine dellPrice").eachText().get(0));
							System.out.println(" ");
						}
					}
				}
			}
		}
	}
}
