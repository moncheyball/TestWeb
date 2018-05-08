package jp.ne.monk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import jp.ne.monk.constfile.ConstJsonDisplayOrder;

public class DisplayInfo {

	/**
	 * 現在の "type":"loop" 要素のIndex情報<br>
	 * JSON の Key を格納する（例：[1, 1, 1]）
	 */
	private static List<String> mNowIndexList = null;

	/**
	 *  "type":"loop" 要素のループ回数を格納したHashMap<br>
	 *  （例：Index情報 [1, 1] のループ回数取得する場合は Key"11"を指定する）
	 */
	private static HashMap<String, Integer> mLoopCounterMap = new HashMap<String, Integer>();

	/**
	 * コンストラクタ
	 */
	public DisplayInfo() {

	}

	/**
	 * 動作確認用
	 */
	public void test() {

		if (null == mNowIndexList) {
			mNowIndexList = new ArrayList<>();
			mNowIndexList.add(0, "1");
//			mNowIndexList.add(1, "1");
//			mNowIndexList.add(2, "1");
		}
		System.out.println("nowIndexList:" + mNowIndexList);

		for (int i = 0; i < 100; i++) {
			mNowIndexList =  new ArrayList<>(getIncrementedServletIndexList(mNowIndexList));
			System.out.println("**** getIncrementedServletIndexList:" + mNowIndexList);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ループカウンタ関連
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
	 * HashMapで管理しているループ回数の値を取得
	 * @param indexList Index情報
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
	 * ループ回数をHashMapに格納
	 * @param indexList Index情報
	 * @param count ループ回数
	 */
	private void setLoopCount(List<String> indexList, int count) {
		mLoopCounterMap.put(listToString(indexList), count);
	}

	/**
	 * ループ回数をインクリメント
	 * @param indexList Index情報
	 */
	private void incrementLoopCount(List<String> indexList) {
		JSONObject jsonObject = getJsonElement(indexList, ConstJsonDisplayOrder.TYPE_LOOP);
		if (null == jsonObject) {
			System.out.println("incrementLoopCount() null == jsonObject");
			return;
		}
		try {
			// ループ回数を更新
			int maxCount = jsonObject.getInt(ConstJsonDisplayOrder.COUNT);
			int count = getLoopCount(indexList);
			int updateCount = (count + 1) % (maxCount + 1);
			// HashMapに格納
			setLoopCount(indexList, updateCount);
			System.out.println("incrementLoopCount()" + indexList + " count:" + count + ", updateCount:" + updateCount + ", maxCount:" + maxCount);

		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * ループ回数がMax値に達したか
	 * @param indexList
	 * @return ture:Max値に達している false:Max値に達していない
	 */
	private boolean isMaxedOutLoopCount(List<String> indexList) {
		int count = getLoopCount(indexList);
		// ループ回数"0"となっていた場合、Max値に達したと見なす。
		return (count == 0);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Index情報更新
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
			jsonObject = getJsonElement(nextIndexList, "page");
		}
		return nextIndexList;
	}

	/**
	 * Index情報を一つ進める（"page"/"loop"を問わない）<br>
	 * ※getIncrementedServletIndexList() からのみ使用する想定。
	 * @param nowIndexList 現在のIndex情報
	 * @return 一つ進めたIndex情報
	 */
	private List<String> getIncrementedIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		String type = getTypeJsonElement(nextIndexList);
		switch (type) {
		case ConstJsonDisplayOrder.TYPE_LOOP:
			nextIndexList = getIncrementedLoopIndexList(nextIndexList);
			break;
		case ConstJsonDisplayOrder.TYPE_PAGE:
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
	 * ※"loop" 以外のIndex情報では使用不可。<br>
	 * ※getIncrementedIndexList() からのみ使用する想定。
	 * @param nowIndexList 現在のIndex情報
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
			if (null != type && type.equals(ConstJsonDisplayOrder.TYPE_LOOP)) {
				// 同階層に次の要素有のため Index情報確定
			} else {
				// 一階層上の要素有無確認
				nextIndexList.remove(end);
				type = getTypeJsonElement(nextIndexList);
				if (null != type && type.equals(ConstJsonDisplayOrder.TYPE_LOOP)) {
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
	 * ※"page" 以外のIndex情報では使用不可。<br>
	 * ※getIncrementedIndexList() からのみ使用する想定。
	 * @param nowIndexList 現在のIndex情報
	 * @return
	 */
	private List<String> getIncrementedPageIndexList(List<String> nowIndexList) {
		List<String> nextIndexList = new ArrayList<>(nowIndexList);

		// 同階層の次の要素を確認。  例 [1, 1, 1] => [1, 1, 2]
		int end = nextIndexList.size() - 1;
		String nextIndex =  String.valueOf(Integer.parseInt(nextIndexList.get(end)) + 1);

		nextIndexList.set(end, nextIndex);

		String type = getTypeJsonElement(nextIndexList);
		if (null != type && type.equals(ConstJsonDisplayOrder.TYPE_PAGE)) {
			// "page"要素有のため Index情報確定
		} else {
			// 一階層上の"loop"要素のIndex情報を返却。  例 [1, 1, 1] => [1, 1]
			nextIndexList.remove(end);
		}

		return nextIndexList;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// JSON取得関連
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
				if(jsonObject.has(ConstJsonDisplayOrder.SUB)) {
					// "sub"要素を持っている場合、"sub"要素を取得してから index の要素を確認する
					jsonObject = jsonObject.getJSONObject(ConstJsonDisplayOrder.SUB);
				}
				jsonObject = jsonObject.getJSONObject(index);
			} catch (JSONException e) {
				// index が存在しない場合は、存在しないデータとして null を返却
				System.out.println(e + " indexList:" + indexList);
				return null;
			}
		}
		return jsonObject;
	}

	/**
	 * type が一致していた場合に、indexList で指定したJSONObjectを返却<br>
	 * type が不一致、または、JSONObjectが取得できなかった場合は null を返却
	 * @param indexList jsonの要素を一意に決めるためのIndex情報（nowIndex等）
	 * @param type "loop"または"page"
	 * @return
	 */
	private JSONObject getJsonElement(List<String> indexList, String type) {
		JSONObject jsonObject = getJsonElement(indexList);
		if (null == jsonObject) {
			return null;
		}
		try {
			String typeValue = jsonObject.getString(ConstJsonDisplayOrder.TYPE);
			if (typeValue.equals(type)) {
				return jsonObject;
			}
		} catch (JSONException e) {
			// 異常値のため null を返却
			System.out.println(e + " indexList:" + indexList + ", type:" + type);
			return null;
		}

		return null;
	}

	/**
	 * Index情報で指定した要素の"type"を返却する
	 * @param indexList Index情報
	 * @return "loop"/"page"を返却。JSONデータが取得できなかった場合は nullを返却
	 */
	private String getTypeJsonElement(List<String> indexList) {
		String type = null;
		JSONObject jsonObject = getJsonElement(indexList);
		if (null == jsonObject) {
			return null;
		}
		try {
			if (jsonObject.has(ConstJsonDisplayOrder.TYPE)) {
				type = jsonObject.getString(ConstJsonDisplayOrder.TYPE);
			}
		} catch (JSONException e) {
			System.out.println(e + " indexList:" + indexList);
			return null;
		}
		return type;
	}

}
