import java.util.ArrayList;
import java.util.List;

public class Reducer {

    public static <T> Object reduce(Message<T> msg, List<Object> list){
        Client client = msg.getClient();
        int code = msg.getRequest();
        return switch (client) { // only add cases where broadcast is being used
            case Customer -> switch (code) {
                case 1 -> null;
                case 2 -> makeNum(list);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) { // only add cases where broadcast is being used
                case 1 -> null;
                case 2 -> null;
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
    private static Object makeNum(List<Object> list) {
        Integer total = 0;
        for (Object o : list)
        {
            System.out.println(o);
            total += (Integer)o + 1000;
        }
        return total;
    }
}
