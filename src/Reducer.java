import lib.shared.Message;
import lib.shared.Store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer extends Communication {

    private static final Map<Integer, ReducerStorage> requestData = new HashMap<>();
    private static String ip;
    private static final int port = TCPServer.basePort - 1;
    public static final int serverPort = TCPServer.basePort - 2;

    private TCPServer server;
    private static Integer workerCount; // sync on requestData

    public Reducer(Socket connection) {
        try {
            server = new TCPServer(connection);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public Reducer(){};

    public static void main(String[] args) {
        ServerSocket listener;
        try
        {
            listener = new ServerSocket(serverPort);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        Reducer reducer = new Reducer();

        try
        {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
        reducer.startConnection(ip, port);
        Message<Integer> msg = reducer.receiveMessage();
        reducer.stopConnection();
        workerCount = msg.getValue();
        try
        {
            while (true)
            {
                Socket serverSocket = listener.accept();
                Thread t = new Reducer(serverSocket);
                t.start();
            }
        }
        catch (Exception e)
        {
            System.err.println("Couldn't start server: " + e.getMessage() );
            throw new RuntimeException(e);
        }
    }

    public void run(){
        this.handleRequest();
    }

    private <T> void handleRequest(){
        BackendMessage<T> request = (BackendMessage<T>) server.receiveMessage();
        System.out.println("Server received: " + request.getRequest() );
        final ReducerStorage reducerStorage;
        if (request.getClient() == Client.MASTER)
        {
            switch (request.getRequest())
            {
                case RESET:
                    synchronized (requestData){
                        if (requestData.containsKey(request.getId()))   // REMOVE_WORKER race condition
                            requestData.get(request.getId()).reset(workerCount);
                    }
                    break;
                case REMOVE_WORKER:
                    synchronized (requestData){
                        workerCount--;
                        for (var entry : requestData.entrySet())
                            prepareForReduce(entry.getKey(), entry.getValue());
                    }
                    break;
                case ADD_WORKER:
                    synchronized (requestData){
                        workerCount++;
                    }
                    break;
                default:
                    System.err.println("Unknown Master request: " + request.getRequest());
                    break;
            }
            server.sendMessage(new Message<>());
            return;
        }

        synchronized (requestData)
        {
            if (!requestData.containsKey(request.getId()))
            {
                reducerStorage = new ReducerStorage(workerCount, request.getClient(), request.getRequest());
                requestData.put(request.getId(), reducerStorage);
            }
            else
                reducerStorage = requestData.get(request.getId());
        }

        synchronized (reducerStorage)
        {
            reducerStorage.reduceCounter();
            reducerStorage.addData(request.getValue());
        }
        prepareForReduce(request.getId(), reducerStorage);
    }

    private <T> void sendToMaster(BackendMessage<T> msg){
        System.out.println("Notifying Master");
        startConnection(ip, port);
        sendMessage(msg);
        stopConnection();
    }

    private void prepareForReduce(Integer id, ReducerStorage counterData)
    {
        synchronized (counterData){
            if (counterData.getCounter() == 0)
            {
                System.out.println("Start reducing " + id);
                synchronized (requestData){ //cleanup
                    requestData.remove(id);
                }
                BackendMessage<List<Object>> msg = new BackendMessage<>(counterData.getData());
                msg.setId(id);
                msg.setClient(counterData.getClient());
                msg.setRequest(counterData.getRequestCode());
                sendToMaster(reduce(msg));
            }
        }
    }

    private static <T> BackendMessage<T> reduce(BackendMessage<List<Object>> msg){
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        List<Object> list = msg.getValue();
        T val = switch (client) { // only add cases where broadcast is being used
            case Customer -> switch (code) {
                case STUB_TEST_1 -> null;
                case STUB_TEST_2 -> makeNum(list);
                case SEARCH -> (T) getFilteredStores(list);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) { // only add cases where broadcast is being used
                case GET_SALES_BY_STORE_TYPE -> (T) reducerSalesByStoreType(list);
                case GET_SALES_BY_PRODUCT_TYPE -> (T) reducerSalesByProductType(list);
                case GET_STORES -> (T) getStores(list);
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
            case MASTER -> null; // MASTER never requires reduce
        };
        BackendMessage<T> result = new BackendMessage<>(val);
        result.setClient(client);
        result.setRequest(code);
        result.setId(msg.getId());
        return result;
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
