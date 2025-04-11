import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DummyApp
{
    public static void displayStores(List<Store> stores)
    {
        if (stores == null || stores.isEmpty())
        {
            System.out.println("No stores found matching the filter.");
            return;
        }
        System.out.println("Available stores:");
        for (int i = 0; i < stores.size(); i++)
        {
            Store store = stores.get(i);
            System.out.println(i + ": " + store.getStoreName() + ". Food Category: " + store.getFoodCategory() + ". Stars: " + store.getStars());
            // calculated from system
//            System.out.println("Price Category: " + store.getPriceCategory());
        }
    }


    public static void displayStoreProducts(Store store)
    {
        List<Product> products = store.getProducts();
        System.out.println("Products in " + store.getStoreName() + ":");
        for (int i = 0; i < products.size(); i++)
        {
            System.out.println(i + ": " + products.get(i).getProductName() + ". Price: " + products.get(i).getPrice());
        }
    }

    public static void main(String[] args)
    {
        try
        {
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Enter name: ");
            String name = keyboard.nextLine();
            Customer customer = new Customer(name);

            String ip = InetAddress.getLocalHost().getHostAddress();
            customer.startConnection(ip, TCPServer.basePort);

            while (true)
            {

                Filter filter = new Filter();

                // Enter user coordinates
                System.out.print("Enter your latitude (e.g., 37.9932963): ");
                double latitude = Double.parseDouble(keyboard.nextLine());

                System.out.print("Enter your longitude (e.g., 23.733413): ");
                double longitude = Double.parseDouble(keyboard.nextLine());
                filter.setCoordinates(latitude, longitude);

                //Chose food categories
                System.out.println("Choose from the available food categories by typing the category's number separated by space(e.g 1 5 2)");
                FoodCategory[] categories = filter.getAvailableFoodCategories();
                for (int i = 0; i < categories.length; i++)
                {
                    System.out.println(i + ": " + categories[i]);
                }

                String chosenCategories = keyboard.nextLine();
                String[] categoryIndexes = chosenCategories.split("\\s+");
                List<FoodCategory> selectedCategories = new ArrayList<>();

                for (String indexString : categoryIndexes)
                {
                    int index = Integer.parseInt(indexString);
                    selectedCategories.add(categories[index]);
                }
                filter.setCategories(selectedCategories.toArray(new FoodCategory[0]));
                System.out.println("Categories chosen" + Arrays.toString(filter.getCategories()));

                // User selects the minimum number of stars. Results will have at least this many stars
                System.out.println("Choose least stars" + Arrays.toString(filter.getAvailableStars()));
                int chosenStars = Integer.parseInt(keyboard.nextLine());
                filter.setStars(chosenStars);

                // User selects the minimum price. Results will have at least this price.
                System.out.println("Choose least price by typing the price's number");
                String[] prices = filter.getAvailablePrices();
                for (int i = 0; i < prices.length; i++)
                {
                    System.out.println(i + ": " + prices[i]);
                }
                String chosenPrice = keyboard.nextLine();
                int index = Integer.parseInt(chosenPrice);
                filter.setPrice(prices[index]);

                customer.setFilter(filter);

                System.out.println("Final filter: " + customer.filter);

                //send filter to master
                customer.search(filter);

                // Receive and display stores
                List<Store> stores = (List<Store>) customer.receiveMessageObject();
                displayStores(stores);

                // Choose a store
                System.out.println("Enter the number of the store you want to view products for:");
                int storeIndex = Integer.parseInt(keyboard.nextLine());
                Store selectedStore = stores.get(storeIndex);
                displayStoreProducts(selectedStore);

                // Choose products
                System.out.println("Type product numbers separated by space to add to cart");
                String input = keyboard.nextLine();
                List<Product> products = selectedStore.getProducts();
                String[] parts = input.split("\\s+");
                for (String part : parts)
                {
                    int productIndex = Integer.parseInt(part);
                    String productName = products.get(productIndex).getProductName();
                    customer.addToCart(productName);
                    System.out.println("Product '" + productName + "' added to cart.");
                }


                System.out.println("Do you want to buy this order? Type 'Yes' to buy, 'No' to start again or 'exit' to close the app");
                String answer = keyboard.nextLine();
                if(answer.equalsIgnoreCase("Yes"))
                {
                    customer.startConnection(ip, TCPServer.basePort);
                    customer.buy();
                }
                else if(answer.equalsIgnoreCase("No"))
                {
                    return;
                }
                else break;

            }
            keyboard.close();
        }

        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
