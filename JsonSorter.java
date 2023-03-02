import java.util.*;
import java.lang.*;
import org.json.*;

/**
 * Recursive JsonSorter. To use JsonSorter, follow the steps below:
 *  - Create a Map (preferably TreeMap) with 'json-key' to 'unique-id' map.
 *  - Create a JsonSorter object with the map and the json string
 *  - Call jsonSorter.sort().
 * Check ExampleUsage for more usage details.
 */
public class JsonSorter {
    String jsonStr;
    Map<String, String> jsonKeyMap;

    public JsonSorter (String jsonStr, Map<String, String> jsonKeyMap) {
        this.jsonStr = jsonStr;
        this.jsonKeyMap = jsonKeyMap;
    }

    private JSONObject jsonSorterRecursive(JSONObject jsonObject, String prefix) {
        if (jsonObject.isEmpty()) {
            return new JSONObject("{}");
        }

        Set<String> jsonKeys = jsonObject.keySet();

        for(String jsonKey : jsonKeys) {
            if(jsonObject.opt(jsonKey) instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray(jsonKey);
                JSONArray sortedJsonArray = new JSONArray();

                List<JSONObject> list = new ArrayList<JSONObject>();
                for(int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getJSONObject(i));
                }

                if(jsonKeyMap.containsKey(prefix + jsonKey)) {
                    list.sort((Comparator) (o1, o2) -> {
                        String firstJsonStr = "";
                        String secondJsonStr = "";
                        try {
                            JSONObject x = (JSONObject) o1;
                            JSONObject y = (JSONObject) o2;

                            String jsonKeyToken = jsonKeyMap.get(prefix + jsonKey);
                            List<String> jsonKeyList = Arrays.asList(jsonKeyToken.split(",", 5));

                            for (String keyJson : jsonKeyList) {
                                List<String> jsonKeyCustomList = Arrays.asList(keyJson.split("_", 5));
                                JSONObject tempFirstObject = x;
                                for(String jsonKeyCustomToken : jsonKeyCustomList) {
                                    if(tempFirstObject.keySet().contains(jsonKeyCustomToken)) {
                                        if(tempFirstObject.opt(jsonKeyCustomToken) instanceof JSONArray)
                                            tempFirstObject = tempFirstObject
                                                    .getJSONArray(jsonKeyCustomToken)
                                                    .getJSONObject(0);
                                        else {
                                            firstJsonStr += tempFirstObject.get(jsonKeyCustomToken);
                                            break;
                                        }
                                    }
                                }

                                JSONObject tempSecondObject = y;
                                for(String jsonKeyCustomToken : jsonKeyCustomList) {
                                    if(tempSecondObject.keySet().contains(jsonKeyCustomToken)) {
                                        if(tempSecondObject.opt(jsonKeyCustomToken) instanceof JSONArray)
                                            tempSecondObject = tempSecondObject
                                                    .getJSONArray(jsonKeyCustomToken)
                                                    .getJSONObject(0);
                                        else {
                                            secondJsonStr += tempSecondObject.get(jsonKeyCustomToken);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return firstJsonStr.compareTo(secondJsonStr);
                    });
                }

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject listObject = jsonSorterRecursive(list.get(i), prefix + jsonKey + ".");
                    sortedJsonArray.put(listObject);
                }

                jsonObject = jsonObject.put(jsonKey, sortedJsonArray);
            } else if(jsonObject.opt(jsonKey) instanceof JSONObject) {
                JSONObject jsonObjectTemp = jsonSorterRecursive(jsonObject.optJSONObject(jsonKey), prefix + jsonKey + ".");
                jsonObject = jsonObject.put(jsonKey, jsonObjectTemp);
            }
        }

        return jsonObject;
    }

    public String sort() {
        JSONObject jsonObject = new JSONObject(jsonStr);
        return jsonSorterRecursive(jsonObject,"").toString();
    }
}
