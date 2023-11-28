package com.trisysit.spring.files.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class AppUtil {
	public static LinkedHashMap<String, String> readDataLineByLine(String file, String fileName) {
		LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
		try {
			FileReader filereader = new FileReader(file);
			try (BufferedReader lineReader = new BufferedReader(filereader)) {
				String lineText = null;
				lineReader.readLine(); // skip header line
				while ((lineText = lineReader.readLine()) != null) {
					String[] data = lineText.split(",");
					String id = data[0];
					String sum = "";
					for (int i = 1; i < data.length; i++) {
						sum += data[i] + ",";
					}
					map1.put(sum, id);
				}
				lineReader.close();
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map1;
	}

	public static void populateFilesData(
			ArrayList<LinkedHashMap<String, String>> map) throws IOException {
		LinkedHashMap<String, ArrayList<String>> successValueMap = new LinkedHashMap<>();
		LinkedHashMap<String, ArrayList<String>> errorValueMap = new LinkedHashMap<>();
		LinkedHashMap<String, String> map1 = map.get(0);
		Set<String> keys = map1.keySet();
		String firstValue = "";
		for (String key : keys) {
			ArrayList<String> al = new ArrayList<>();
			ArrayList<String> eal = new ArrayList<>();
			boolean flag = false;
			firstValue = map1.get(key);
			al.add(key);
			for (int i = 1; i < map.size(); i++) {
				LinkedHashMap<String, String> map2 = map.get(i);
				Set<String> secondaryKeys = map2.keySet();
				for (String seckey : secondaryKeys) {
					String secValue = map2.get(seckey);
					if (secValue.equals(firstValue)) {
						flag = true;
						al.add(seckey);
						break;
					}
				}
			}if(flag == false) {
				eal.add(key);
				errorValueMap.put(firstValue, eal);
			}if(flag == true) {
				successValueMap.put(firstValue, al);
			}
		}
		csvWriterMethod(successValueMap , "success");
		csvWriterMethod(errorValueMap ,"error");
	}

	private static void csvWriterMethod(LinkedHashMap<String, ArrayList<String>> mapValue , String type) throws IOException {
		String dir = System.getProperty("user.dir");
		String filePath =dir + "/uploads" + "/" + type + ".csv";
		FileWriter outputfile = new FileWriter(filePath);
		try (BufferedWriter writer = new BufferedWriter(outputfile)) {
			Set<String> keys = mapValue.keySet();
			for (String key : keys) {
				StringBuffer sb = new StringBuffer();
			      for (String s : mapValue.get(key)) {
			         sb.append(s);
			         sb.append(" ");
			      }
				String value = key + "," + sb.toString() ;
				writer.write(value);
				writer.newLine ();
			}writer.close ();
		} catch (IOException ex) {
		  ex.printStackTrace(System.err);
		}
		
	}
}
