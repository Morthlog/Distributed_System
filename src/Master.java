
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class Master {

    public static void main(String[] args){
        final int n_workers = Integer.parseInt(args[0]);
        final String DATA_PATH = "./Data/Stores.json";
        final Scanner on = new Scanner(System.in);
        Process[] workers = new Process[n_workers];
        // Initialize workers
        for (int i = 0; i < n_workers; i++)
        {
            try
            {
                System.out.println("Starting worker #" + i + "...");
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd", "/c", "start", "cmd", "/k", "java Worker " + i);
                workers[i] = pb.start();
            }
            catch(IOException e)
            {
                System.err.printf("Could not start worker #%d\n", i);
            }
        }

        // Read initial memory
        JSONArray stores;
        try
        {
            Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
            stores = (JSONArray) ((JSONObject)temp).get("Stores");
        }
        catch (Exception e)
        {
            System.out.println("JSON data could not be parsed");
            throw new RuntimeException();
        }

        // Send store data to each Worker
        int currentWorker = 0;
        for (var obj: stores)
        {
            JSONObject store = (JSONObject)obj;

            currentWorker = (currentWorker + 1) % n_workers;
        }
        
        //https://www.baeldung.com/a-guide-to-java-sockets
        // Loop and wait for TCP call

    }
}
