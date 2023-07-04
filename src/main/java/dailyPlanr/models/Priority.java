package dailyPlanr.models;

import java.util.ArrayList;
import java.util.List;

public enum Priority {
	
	HIGH("High"),
	LOW("Low"),
	MEDIUM("Medium");
	
	private String text;
	
	Priority(String text){
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public static List<String> getAllPriorities() {
        List<String> allPriorities = new ArrayList<String>();

        for (Priority priority : values())
            allPriorities.add(priority.text);

        return allPriorities;
    }

}
