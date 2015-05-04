package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigParser {
	HashMap<String, String> config;
	public ConfigParser (String filename) {
		File file = new File(filename);
		Scanner scanner = null;
		config = new HashMap<String, String>();
		try {
			scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			String a = scanner.nextLine();
			String[] content = a.split(":");
			if(!a.startsWith("#") && content.length == 2) {
				config.put(content[0].trim(), content[1].trim());
			}
		}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();
	}
	
	public String getString(String string){
		return config.get(string);
	}
	
	public long getLong(String string){
		return new Long(config.get(string));
	}
	
	public int getInt(String string){
		return new Integer(config.get(string));
	}
	public boolean getBool(String string){
		return config.get(string).contains("T")||config.get(string).contains("True")
				||config.get(string).contains("true")||config.get(string).contains("Yes")
				||config.get(string).contains("yes");
	}
}