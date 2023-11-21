package dailyplanr.models;

import java.util.ArrayList;
import java.util.List;

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
	
	public static List<String> getAllStatus() {
        List<String> allStatus = new ArrayList<String>();

        for (Status stat : values())
            allStatus.add(stat.desc);

        return allStatus;
    }

}
