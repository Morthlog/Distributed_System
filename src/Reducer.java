import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer {

    public static Map<Integer, List<Object>> map = new HashMap<>();

    public static <T,T1> T1 reduce(BackendMessage<T> msg){
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        List<Object> list = map.get(msg.getId());
        return switch (client) { // only add cases where broadcast is being used
            case Customer -> switch (code) {
                case STUB_TEST_1 -> null;
                case STUB_TEST_2 -> makeNum(list);
                case SEARCH -> (T1) getFilteredStores(list);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) { // only add cases where broadcast is being used
                case GET_SALES_BY_STORE_TYPE -> (T1) reducerSalesByStoreType(list);
                case GET_SALES_BY_PRODUCT_TYPE -> (T1) reducerSalesByProductType(list);
                case GET_STORES -> (T1) getStores(list);
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
            case MASTER -> null; // MASTER never requires reduce
        };
    }

    private static Map<String, Map<String, Double>> reducerSalesByType(List<Object> mappedResults)
    {
        Map<String, Map<String, Double>> combinedSales = new HashMap<>();

        if (mappedResults != null)
        {
            for (Object result : mappedResults)
            {
                if (result instanceof Map)
                {
                    Map<String, Map<String, Double>> typeMap = (Map<String, Map<String, Double>>) result;

                    for (Map.Entry<String, Map<String, Double>> typeEntry : typeMap.entrySet())
                    {
                        String categoryType = typeEntry.getKey();
                        Map<String, Double> storeSales = typeEntry.getValue();

                        if (!combinedSales.containsKey(categoryType))
                        {
                            combinedSales.put(categoryType, new HashMap<>());
                        }

                        Map<String, Double> combinedStoreSales = combinedSales.get(categoryType);

                        combinedStoreSales.putAll(storeSales);

                    }
                }
            }
        }
        return combinedSales;
    }


    private static Map<String, Map<String, Double>> reducerSalesByStoreType(List<Object> mappedResults) {
        return reducerSalesByType(mappedResults);
    }

    private static Map<String, Map<String, Double>> reducerSalesByProductType(List<Object> mappedResults) {
        return reducerSalesByType(mappedResults);
    }

    private static Map<String, ExtendedStore> getStores(List<Object> mappedResults) {
        Map<String, ExtendedStore> combinedStores = new HashMap<>();

        if (mappedResults != null) {
            for (Object result : mappedResults) {
                if (result instanceof Map) {
                    combinedStores.putAll((Map<String, ExtendedStore>) result);
                }
            }
        }
        return combinedStores;
    }

    private static List<Store> getFilteredStores(List<Object> mappedResults)
    {
        List<Store> combinedStores = new ArrayList<>();
        if (mappedResults != null)
        {
            for (Object result : mappedResults)
            {
                for (Object store : (List<?>) result)
                {
                    if (store instanceof ExtendedStore)
                    {
                        combinedStores.add(((ExtendedStore) store).toCustomerStore());
                    }
                }
            }
        }
        return combinedStores;
    }


    /**
     * Temporary for stubUser
     */
    private static <T> T makeNum(List<Object> list) {
        Integer total = 0;
        for (Object o : list)
        {
            System.out.println(o);
            total += (Integer)o + 1000;
        }
        return (T)total;
    }
}
