package dailyPlanr.models;

public enum Status {
	TODO("To do"),
	INPROGRESS("In progress"),
	DONE("Done"),
	ARCHIVE("Archive");
	
	private String desc;
	
	Status(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
