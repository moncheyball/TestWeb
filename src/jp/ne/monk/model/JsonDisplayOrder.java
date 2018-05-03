package jp.ne.monk.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDisplayOrder {

	private static JSONObject jsonObject = null;

	public JsonDisplayOrder() {

	}

	/**
	 * JSON Object を返却
	 * http://yh9092.hatenablog.com/entry/2017/11/16/232835
	 */
	public JSONObject getJson() {
		System.out.println("getJson()");

		// 既に展開済みの場合、再読み込みを行わない。
		if (null != jsonObject) {
			return jsonObject;
		}

		// Jsonファイルの読み込み
		String jsonStr = readFile();
		if (null == jsonStr) {
			return null;
		}

		// JsonObject の取得
        try {
			JSONArray jsonArray = new JSONArray(jsonStr);
			jsonObject = jsonArray.getJSONObject(0);
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * JSONファイルを読み込み
	 * @return
	 */
	private static String readFile() {
		try {

			File file = new File("E:\\Program\\pleiades\\workspace\\TestWeb\\WebContent\\WEB-INF\\json\\DisplayOrder.json");
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String jsonStr = "";
			String readLine = reader.readLine();
			while(readLine != null){
				jsonStr += readLine;
				readLine = reader.readLine();
			}
			reader.close();
			return jsonStr;
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return null;
		} catch (IOException e){
			System.out.println(e);
			return null;
		}
	}

}
