import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer {

    public static Map<Integer, List<Object>> map = new HashMap<>();

    public static <T,T1> T1 reduce(Message<T> msg){
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        List<Object> list = map.get(msg.getId());
        return switch (client) { // only add cases where broadcast is being used
            case Customer -> switch (code) {
                case STUB_TEST_1 -> null;
                case STUB_TEST_2 -> makeNum(list);
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
        };
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
