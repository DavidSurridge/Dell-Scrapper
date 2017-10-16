package exper;

public class Laptop implements Comparable<Object> {

	private String laptopModel;
	private String name;
	private Double price;
	private String cpuDescription;
	private String CPU;
	private String operatingSysDescription;
	private String ramDescription;
	private String diskDescription;
	private String graphicsDescription;
	private String graphicsModel;
	private String screenDescription;
	private String productSoldOut;
	private String itemIdentifier;

	public Laptop(String laptopModel, String name, Double price, String cpuDescription, String CPU,
			String operatingSysDescription, String ramDescription, String diskDescription, String graphicsDescription,
			String graphicsModel, String screenDescription, String productSoldOut, String itemIdentifier) {
		this.laptopModel = laptopModel;
		this.name = name;
		this.price = price;
		this.cpuDescription = cpuDescription;
		this.CPU = CPU;
		this.operatingSysDescription = operatingSysDescription;
		this.ramDescription = ramDescription;
		this.diskDescription = diskDescription;
		this.graphicsDescription = graphicsDescription;
		this.graphicsModel = graphicsModel;
		this.screenDescription = screenDescription;
		this.productSoldOut = productSoldOut;
		this.itemIdentifier = itemIdentifier;
	}

	public String getLaptopModel() {
		return laptopModel;
	}

	public String getName() {
		return name;
	}

	public Double getPrice() {
		return price;
	}

	public String getCpuDescription() {
		return cpuDescription;
	}

	public String getCPU() {
		return CPU;
	}

	public String getOperatingSysDescription() {
		return operatingSysDescription;
	}

	public String getRamDescription() {
		return ramDescription;
	}

	public String getDiskDescription() {
		return diskDescription;
	}

	public String getGraphicsDescription() {
		return graphicsDescription;
	}

	public String getGraphicsModel() {
		return graphicsModel;
	}

	public String getScreenDescription() {
		return screenDescription;
	}

	public String getProductSoldOut() {
		return productSoldOut;
	}

	public String getItemIdentifier() {
		return itemIdentifier;
	}

	@Override
	public int compareTo(Object anotherLaptop) {
		  if (!(anotherLaptop instanceof Laptop))
		      throw new ClassCastException("A Laptop object expected.");
		    Double anotherLaptopPrice = ((Laptop) anotherLaptop).getPrice();  
		    return (int) (this.price - anotherLaptopPrice);    
	
	}
}