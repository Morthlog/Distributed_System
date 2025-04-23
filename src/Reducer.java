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
                case ADD_STORE -> null;
                case REMOVE_PRODUCT -> null;
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
            case MASTER -> null; // MASTER never requires reduce
        };
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
                    if (store instanceof Store)
                    {
                        combinedStores.add((Store) store);
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
