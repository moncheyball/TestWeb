package jp.ne.monk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayInfo {

	private static List<String> mNowIndexList = null;

	private static HashMap<String,String> mLoopCounterMap = new HashMap<String,String>();

	public DisplayInfo() {

	}

	public void test() {

		if (null == mNowIndexList) {
			mNowIndexList = new ArrayList<>();
			mNowIndexList.add(0, "3");
			mNowIndexList.add(1, "1");
			mNowIndexList.add(2, "4");
		}
		System.out.println("nowIndexList:" + mNowIndexList);

//		JSONObject jsonObject = getJsonElement(nowIndexList);
//		System.out.println("[" + nowIndexList + "]jsonObject:" + jsonObject);

//		System.out.println("getIncrementIndexList:" + getIncrementIndexList(mNowIndexList));
		System.out.println("getIncrementPageIndexList:" + getIncrementPageIndexList(mNowIndexList));

	}

	/**
	 * Index情報（nowIndex等）を文字列に変換
	 * @param list
	 * @return
	 */
	String listToString(List<String> list) {
		String listStr = "";
		for (String element: list) {
			listStr = listStr + element;
		}
		return listStr;
	}

	/**
	 * Indexで指定された要素の JSONObject を返却する。<br>
	 * Indexで指定した要素が存在しない場合 null。
	 * @param indexList jsonの要素を一意に決めるためのIndex情報（nowIndex等）
	 * @return
	 */
	private JSONObject getJsonElement(List<String> indexList) {

		JsonDisplayOrder order = new JsonDisplayOrder();
		JSONObject jsonObject = order.getJson();

		for (String index: indexList) {
			try {
				jsonObject = jsonObject.getJSONObject(index);
			} catch (JSONException e) {
				// index が存在しない場合は、存在しないデータとして null を返却
				System.out.println(e);
				return null;
			}
			try {
				// "type":"loop" 用に "sub"(一つ下の階層)を取得
				// "type":"page" ("sub"がない)場合は JSONException となるが処理継続。
				jsonObject = jsonObject.getJSONObject("sub");
			} catch (JSONException e) {
				System.out.println(e);
			}
		}
		System.out.println("jsonObject:" + jsonObject);

		return jsonObject;
	}

	/**
	 * Indexで指定された要素の JSONObject を返却する。<br>
	 * Indexで指定した要素が存在しない場合、及び、Servlet情報がない場合は null。
	 * @param indexList jsonの要素を一意に決めるためのIndex情報（nowIndex等）
	 * @return
	 */
	private JSONObject getJsonServletElement(List<String> indexList) {
		JSONObject jsonObject = getJsonElement(indexList);
		try {
			jsonObject.getString("servlet");
		} catch (JSONException e) {
			// "type":"page" 出ない場合、nullを返却
			System.out.println(e);
			return null;
		}

		return jsonObject;
	}

	/**
	 * 次にServlet情報を含むJsonObjectのIndex情報を返却する
	 * @param nowIndexList 現在のIndex情報
	 * @return
	 */
	private List<String> getIncrementPageIndexList(List<String> nowIndexList) {

		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		JSONObject jsonObject = null;

		while (null == jsonObject) {
			nextIndexList = getIncrementIndexList(nextIndexList);
			System.out.println("nextIndexList:" + nextIndexList);
			if (nextIndexList.equals(nowIndexList)) {
				// 一周したら抜ける
				// TODO BUG 抜けてくれていない
				nextIndexList = null;
				break;
			}
			jsonObject = getJsonServletElement(nextIndexList);
		}

		return nextIndexList;
	}


	/**
	 * Index情報を一つ進める
	 * @param nowIndexList 現在のIndex情報
	 * @return 一つ進めたIndex情報
	 */
	private List<String> getIncrementIndexList(List<String> nowIndexList) {
		System.out.println("getIncrementIndexList() nowIndexList:" + nowIndexList);

		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		// Jsonの末尾かどうか
		boolean isEndofJson = true;

		// １階層下の要素を確認
		nextIndexList.add("1");
//		System.out.println("nextIndexList:" + nextIndexList);
		JSONObject jsonObjectSub = getJsonElement(nextIndexList);
		if (null == jsonObjectSub) {
			// 「indexList.add("1");」で追加した要素を削除
			nextIndexList.remove(nextIndexList.size() - 1);

			// １階層下に要素が存在しない場合、同階層の１つ後の要素を確認
			for (int i = nowIndexList.size() - 1; i >= 0; i--) {
				String nextIndex = String.valueOf(Integer.parseInt(nextIndexList.get(i)) + 1);
				nextIndexList.set(i, nextIndex);

				JSONObject jsonObjectNext = getJsonElement(nextIndexList);
				if (null == jsonObjectNext) {
					// jsonObject が存在しない場合、末尾の要素を削除して、一つ上の要素確認しにいく。
					nextIndexList.remove(i);
				} else {
					// jsonObject が存在した場合、確定としてループを抜ける
					isEndofJson = false;
					break;
				}
			}
		} else {
			// １階層下に要素があった場合、確定
			isEndofJson = false;
		}

		if (isEndofJson) {
			// Jsonの末尾の場合、IndexList [1]を返却
			nextIndexList.clear();
			nextIndexList.add(0, "1");
		}

		return nextIndexList;
	}


}
