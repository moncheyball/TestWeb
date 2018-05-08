package jp.ne.monk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayInfo {

	private static List<String> mNowIndexList = null;

	private static HashMap<String, Integer> mLoopCounterMap = new HashMap<String, Integer>();

	private static final String JSON_TYPE_LOOP = "loop";
	private static final String JSON_TYPE_PAGE = "page";


	public DisplayInfo() {

	}

	public void test() {

		if (null == mNowIndexList) {
			mNowIndexList = new ArrayList<>();
			mNowIndexList.add(0, "1");
//			mNowIndexList.add(1, "1");
//			mNowIndexList.add(2, "1");
		}
		System.out.println("nowIndexList:" + mNowIndexList);

//		JSONObject jsonObject = getJsonElement(nowIndexList);
//		System.out.println("[" + nowIndexList + "]jsonObject:" + jsonObject);

//		System.out.println("getIncrementIndexList:" + getIncrementIndexList(mNowIndexList));
//		System.out.println("getIncrementPageIndexList:" + getIncrementedPageIndexList(mNowIndexList));

		for (int i = 0; i < 100; i++) {
			mNowIndexList =  new ArrayList<>(getIncrementedServletIndexList(mNowIndexList));
			System.out.println("**** getIncrementedServletIndexList:" + mNowIndexList);
		}

	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Index情報（nowIndex等）を文字列に変換<br>
	 * 例：[1, 2, 3] => "123"
	 * @param list
	 * @return
	 */
	private String listToString(List<String> list) {
		String listStr = "";
		for (String element: list) {
			listStr = listStr + element;
		}
		return listStr;
	}

	/**
	 * HashMapで管理しているループカウンタの値を取得
	 * @param indexList
	 * @return
	 */
	private int getLoopCount(List<String> indexList) {
		Integer count = mLoopCounterMap.get(listToString(indexList));
		if (null == count) {
			count = 0;
		}
		return count;
	}

	/**
	 * HashMapで管理しているループカウンタに値を設定
	 * @param indexList
	 * @param count
	 */
	private void setLoopCount(List<String> indexList, int count) {
		mLoopCounterMap.put(listToString(indexList), count);
	}

	private void incrementLoopCount(List<String> indexList) {
		JSONObject jsonObject = getJsonLoopElement(indexList, JSON_TYPE_LOOP);
		if (null == jsonObject) {
			System.out.println("incrementLoopCount() null == jsonObject");
			return;
		}
		try {
			int maxCount = jsonObject.getInt("count");
			int count = getLoopCount(indexList);
			int updateCount = (count + 1) % (maxCount + 1);
			setLoopCount(indexList, updateCount);
			System.out.println("incrementLoopCount()" + indexList + " count:" + count + ", updateCount:" + updateCount + ", maxCount:" + maxCount);

		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * HashMapで管理しているループカウンタがMax値であるか
	 * @param indexList
	 * @return ture:Max値に達している false:Max値に達していない
	 */
	private boolean isMaxedOutLoopCount(List<String> indexList) {
		int count = getLoopCount(indexList);
		return (count == 0);
	}

	private boolean BAKisMaxedOutLoopCount(List<String> indexList) {
		int count = getLoopCount(indexList);
		JSONObject jsonObject = getJsonElement(indexList);
		if (null == jsonObject) {
			System.out.println("isMaxedOutLoopCount() null == jsonObject");
			return false;
		}
		try {
			String type = jsonObject.getString("type");
			if (!type.equals(JSON_TYPE_LOOP)) {
				return false;
			}
			int maxCount = jsonObject.getInt("count");
			boolean isMaxOut = (count >= (maxCount - 1));
			System.out.println("isMaxedOutLoopCount()" + indexList +" count:" + count + " ,maxCount:" + maxCount + " ,return:" + isMaxOut);
			return isMaxOut;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 次に表示するServlet情報を含むJsonObjectのIndex情報を返却する
	 * @param nowIndexList 現在のIndex情報
	 * @return
	 */
	private List<String> getIncrementedServletIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);
		JSONObject jsonObject = null;

		while (null == jsonObject) {
			// 次の要素のIndexListを取得して、"page"要素であれば確定
			nextIndexList = getIncrementedIndexList(nextIndexList);
			jsonObject = getJsonLoopElement(nextIndexList, "page");
		}
		return nextIndexList;
	}


	/**
	 * Index情報を一つ進める（"page"/"loop"を問わない）
	 * @param nowIndexList 現在のIndex情報
	 * @return 一つ進めたIndex情報
	 */
	private List<String> getIncrementedIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		String type = getTypeJsonElement(nextIndexList);

		switch (type) {
		case JSON_TYPE_LOOP:
			nextIndexList = getIncrementedLoopIndexList(nextIndexList);
			break;
		case JSON_TYPE_PAGE:
			nextIndexList = getIncrementedPageIndexList(nextIndexList);
			break;
		default:
			// エラー
			nextIndexList = null;
			break;
		}

		return nextIndexList;
	}


	/**
	 * 現在のIndex情報が"loop"である場合の、Index情報を一つ進める処理。<br>
	 * ※"loop" 以外のIndex情報では使用不可。
	 * @param nowIndexList
	 * @return
	 */
	private List<String> getIncrementedLoopIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		// 現在のIndex情報のループカウンタをインクリメント
		incrementLoopCount(nowIndexList);

		if (isMaxedOutLoopCount(nextIndexList)) {
			// 同階層の次の要素を確認。  例 [1, 1, 1] => [1, 1, 2]

			int end = nextIndexList.size() - 1;
			String nextIndex =  String.valueOf(Integer.parseInt(nextIndexList.get(end)) + 1);
			nextIndexList.set(end, nextIndex);

			String type = getTypeJsonElement(nextIndexList);
			if (null != type && type.equals(JSON_TYPE_LOOP)) {
				// 同階層に次の要素有のため Index情報確定
			} else {
				// 一階層上の要素有無確認
				nextIndexList.remove(end);
				type = getTypeJsonElement(nextIndexList);
				if (null != type && type.equals(JSON_TYPE_LOOP)) {
					// 一階層上の要素を返却
				} else {
					// 同階層に次、及び、一階層上に要素なし。=> Jsonの末尾 => IndexList [1]を返却
					nextIndexList.clear();
					nextIndexList.add(0, "1");
				}
			}
		} else {
			// ループカウンタがMAX値でないため、一階層したのIndex情報を返却
			nextIndexList.add("1");
		}

		return nextIndexList;
	}

	/**
	 * 現在のIndex情報が"page"である場合の、Index情報を一つ進める処理。<br>
	 * ※"page" 以外のIndex情報では使用不可。
	 * @param nowIndexList
	 * @return
	 */
	private List<String> getIncrementedPageIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		// 同階層の次の要素を確認。  例 [1, 1, 1] => [1, 1, 2]
		int end = nextIndexList.size() - 1;
		String nextIndex =  String.valueOf(Integer.parseInt(nextIndexList.get(end)) + 1);

		nextIndexList.set(end, nextIndex);

		String type = getTypeJsonElement(nextIndexList);
		if (null != type && type.equals(JSON_TYPE_PAGE)) {
			// "page"要素有のため Index情報確定
		} else {
			// 一階層上の"loop"要素のIndex情報を返却。  例 [1, 1, 1] => [1, 1]
			nextIndexList.remove(end);
		}

		return nextIndexList;
	}



	/**
	 * Index情報を一つ進める（"page"/"loop"を問わない）
	 * @param nowIndexList 現在のIndex情報
	 * @return 一つ進めたIndex情報
	 */
	private List<String> BAKgetIncrementedIndexList(List<String> nowIndexList) {
		System.out.println("getIncrementIndexList() nowIndexList:" + nowIndexList);

		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		// Jsonの末尾かどうか
		boolean isEndofJson = true;


		// １階層下の要素を確認
		nextIndexList.add("1");
		JSONObject jsonObjectChiled = getJsonChildElement(nextIndexList);
		if (null == jsonObjectChiled) {
			// 「indexList.add("1");」で追加した要素を削除
			nextIndexList.remove(nextIndexList.size() - 1);

			// １階層下に要素が存在しない場合、同階層の１つ後の要素を確認
			//   例：現在[1, 1]とする。[1, 1, 1]がない場合、[1, 2]を確認する。
			int i = nowIndexList.size() - 1;
			String nextIndex = String.valueOf(Integer.parseInt(nextIndexList.get(i)) + 1);
			nextIndexList.set(i, nextIndex);

			JSONObject jsonObjectNext = getJsonElement(nextIndexList);
			if (null == jsonObjectNext) {
				// jsonObject が存在しない場合、一つ上の要素のループカウンタを確認する。
				nextIndexList.remove(i);
				if (isMaxedOutLoopCount(nextIndexList)) {
					// ループカウンタがMAX値である場合、末尾の要素を削除して、一つ上の要素確認しにいく。
					//   例：現在[1, 3]とする。[1, 3]が末尾の要素である場合、[2]。
					String nextIndexA = String.valueOf(Integer.parseInt(nextIndexList.get(i-1)) + 1);
					nextIndexList.set(i-1, nextIndexA);
				} else {
					//インクリメント★
					incrementLoopCount(nextIndexList);

					// ループカウンタがMAX値である場合、末尾の要素を削除して、一つ上の要素確認しにいく。
					//   例：現在[1, 3]とする。[1, 3]が末尾の要素である場合、[1, 1]を確認しに行く。
					nextIndexList.add(i, "1");
				}
				jsonObjectNext = getJsonElement(nextIndexList);
				if (null == jsonObjectNext) {
					// jsonObject が存在した場合、確定
					isEndofJson = false;
				}
			} else {
				// jsonObject が存在した場合、確定
				isEndofJson = false;
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

//	private void getIncrementedIndexList


	/**
	 * Indexで指定された要素の JSONObject を返却する。<br>
	 * Indexで指定した要素が存在しない場合、及び、Servlet情報がない場合は null。
	 * @param indexList jsonの要素を一意に決めるためのIndex情報（nowIndex等）
	 * @return
	 */
	private JSONObject getJsonServletElement(List<String> indexList) {
		JSONObject jsonObject = getJsonChildElement(indexList);
		try {
			jsonObject.getString("servlet");
		} catch (JSONException e) {
			// 異常値のため null を返却
			// "type":"page" でない（"servlet" を含まない場合）場合、nullを返却
//			System.out.println(e);
			return null;
		}

		return jsonObject;
	}

	/**
	 * type が一致していた場合に、indexList で指定したJSONObjectを返却<br>
	 * type が不一致、または、JSONObjectが取得できなかった場合は null を返却
	 * @param indexList
	 * @param type "loop"または"page"
	 * @return
	 */
	private JSONObject getJsonLoopElement(List<String> indexList, String type) {
		JSONObject jsonObject = getJsonElement(indexList);
		try {
			String typeValue = jsonObject.getString("type");
			if (typeValue.equals(type)) {
				return jsonObject;
			}
		} catch (JSONException e) {
			// 異常値のため null を返却
			System.out.println(e);
			return null;
		}

		return null;
	}

	/**
	 * Index情報で指定した要素の"type"を返却する
	 * @param indexList Index情報
	 * @return "loop"/"page". 取得できなかった場合は null
	 */
	private String getTypeJsonElement(List<String> indexList) {

		String type = null;
		JSONObject jsonObject = getJsonElement(indexList);
		if (null == jsonObject) {
			return null;
		}
		try {
			if (jsonObject.has("type")) {
				type = jsonObject.getString("type");
			}
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return type;
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
				if(jsonObject.has("sub")) {
					// "sub"要素を持っている場合、"sub"要素を取得してから index の要素を確認する
					jsonObject = jsonObject.getJSONObject("sub");
				}
				jsonObject = jsonObject.getJSONObject(index);
			} catch (JSONException e) {
				// index が存在しない場合は、存在しないデータとして null を返却
				System.out.println(e + " indexList" + indexList);
				return null;
			}
		}
//		System.out.println("jsonObject:" + jsonObject);

		return jsonObject;
	}

	/**
	 * Indexで指定された要素の JSONObject を返却する。<br>
	 * Indexで指定した要素が存在しない場合 null。
	 * @param indexList jsonの要素を一意に決めるためのIndex情報（nowIndex等）
	 * @return
	 */
	private JSONObject getJsonChildElement(List<String> indexList) {

		JsonDisplayOrder order = new JsonDisplayOrder();
		JSONObject jsonObject = order.getJson();

		for (String index: indexList) {
			try {
				jsonObject = jsonObject.getJSONObject(index);
			} catch (JSONException e) {
				// index が存在しない場合は、存在しないデータとして null を返却
//				System.out.println(e);
				return null;
			}
			try {
				// "type":"loop" 用に "sub"(一つ下の階層)を取得
				jsonObject = jsonObject.getJSONObject("sub");
			} catch (JSONException e) {
				// "type":"page" の("sub"がない)場合は JSONException となるが処理継続。
//				System.out.println(e);
			}
		}
//		System.out.println("jsonObject:" + jsonObject);

		return jsonObject;
	}

}
