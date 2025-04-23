import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DummyApp
{
    private static boolean loading;
    private Scanner keyboard;
    private Customer customer;
    Filter filter;

    public DummyApp()
    {
        keyboard = new Scanner(System.in);
        filter = new Filter();
        setDefaultFilters();
    }

    public int getIntInput()
    {
        int num = keyboard.nextInt();
        keyboard.nextLine(); // Clear the Enter key left after nextInt()
        return num;
    }

    public void inputCustomerName()
    {
        System.out.print("Enter name: ");
        String name = keyboard.nextLine();
        customer = new Customer(name);
        filter.setLatitude(37.9932963);
        filter.setLongitude(23.733413);
    }

    public void inputCoordinates()
    {
        System.out.print("Enter your latitude (e.g., 37.9932963): ");
        double latitude = Double.parseDouble(keyboard.nextLine());
        System.out.print("Enter your longitude (e.g., 23.733413): ");
        double longitude = Double.parseDouble(keyboard.nextLine());
        filter.setLatitude(latitude);
        filter.setLongitude(longitude);
    }

    public void chooseFoodCategories()
    {
        System.out.println("Choose from the available food categories by typing the category's number separated by space (e.g., 1 5 2):");
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
        filter.setFoodCategories(selectedCategories.toArray(new FoodCategory[0]));
    }

    public void chooseLeastStars()
    {
        System.out.println("Choose least stars " + Arrays.toString(filter.getAvailableStars()));
        int chosenStars = getIntInput();
        filter.setStars(chosenStars);
    }

    public void choosePriceCategories()
    {
        System.out.println("Choose price categories by typing the price's number separated by space (e.g., 0 1 2)::");
        String[] prices = filter.getAvailablePrices();
        for (int i = 0; i < prices.length; i++)
        {
            System.out.println(i + ": " + prices[i]);
        }

        String choices = keyboard.nextLine();
        String[] categoryIndexes = choices.split("\\s+");
        List<String> selectedCategories = new ArrayList<>();

        for (String indexString : categoryIndexes)
        {
            int index = Integer.parseInt(indexString);
            selectedCategories.add(prices[index]);
        }
        filter.setPriceCategories(selectedCategories.toArray(new String[0]));
    }

    public void setDefaultFilters()
    {
        filter.setFoodCategories(filter.getAvailableFoodCategories());
        filter.setStars(1);
        filter.setPriceCategories(filter.getAvailablePrices());
        System.out.println("Default filters set: " + filter);
    }

    public void displayStores(List<Store> stores)
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
            System.out.println(i + ": " + store.getStoreName()
                    + ". Food Category: " + store.getFoodCategory()
                    + ". Stars: " + store.getStars()
                    + ". Price Category: " + store.getPriceCategory());
        }
    }

    public Store chooseStore(List<Store> stores)
    {
        System.out.println("Enter the number of the store you want to view products for:");
        int storeIndex = getIntInput();
        return stores.get(storeIndex);
    }

    public void displayStoreProducts(Store store)
    {
        List<Product> products = store.getProducts();
        System.out.println("Products in " + store.getStoreName() + ":");
        for (int i = 0; i < products.size(); i++)
        {
            System.out.println(i + ": " + products.get(i).getProductName()
                    + ". Price: " + products.get(i).getPrice());
        }
    }

    public void chooseProducts(Store store)
    {
        List<Product> products = store.getProducts();
        customer.addStoreNameToCart(store.getStoreName());
        while (true)
        {
            displayStoreProducts(store);
            System.out.println("Type product number to add to cart (or -1 to finish):");
            int productIndex = getIntInput();

            if (productIndex == -1)
            {
                break;
            }

            String selectedProductName = products.get(productIndex).getProductName();
            System.out.println("Type quantity for '" + selectedProductName + "':");
            int quantity = getIntInput();
            customer.addToCart(selectedProductName, quantity);
            System.out.println("Added " + quantity + " x '" + selectedProductName + "' to cart.");
        }
    }

    public void finalizeOrder()
    {
        System.out.println("How do you want to continue?");
        System.out.println("1. Complete order");
        System.out.println("2. Cancel and start again");
        System.out.println("3. Close the app");
        int choice = getIntInput();
        
        if (choice == 1)
        {
            new Thread(() -> customer.buy(this::handleBuyResult)).start();
            loading = true;
            playAnimation();// loading must be put after the call or in new thread, else the thread will get stuck
        }
        else if (choice == 2)
        {
            System.out.println("Order canceled. Restarting...");
            main(null);
        }
        else
        {
            System.out.println("Closing app...");
        }
    }

    public void playAnimation()
    {
        StringBuilder text = new StringBuilder("Loading.");
        long start = System.currentTimeMillis();
        long end;
        while (loading)
        {
            end = System.currentTimeMillis();
            if (end - start > 200)
            {
                text.append(".");
                start = end;
            }

            System.out.print(text + "\r");
        }
    }

    private void handleSearchResults(List<Store> stores)
    {
        loading = false;
        displayStores(stores);

        if (stores.isEmpty())
        {
            System.out.println("Try with new filters. Restarting...");
            main(null);
            return;
        }

        Store selectedStore = chooseStore(stores);
        chooseProducts(selectedStore);
        finalizeOrder();
    }

    private void handleBuyResult(String confirmation)
    {
        loading = false;
        System.out.println("Purchase response: " + confirmation);
    }

    public static void main(String[] args)
    {
        DummyApp app = new DummyApp();
        app.inputCustomerName();
        int choice;

        System.out.println("Coordinates detected: latitude = " + app.filter.getLatitude() + ", longitude = " + app.filter.getLongitude());
        System.out.println("Do you want to enter new coordinates?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        choice = app.getIntInput();
        if (choice == 1)
        {
            app.inputCoordinates();
        }

        System.out.println("Do you want to filter the stores?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        choice = app.getIntInput();

        if (choice == 1)
        {
            boolean continueFiltering = true;
            while (continueFiltering)
            {
                System.out.println("Available filters:");
                System.out.println("1. Choose food categories");
                System.out.println("2. Choose stars");
                System.out.println("3. Choose price category");
                System.out.println("4. Continue");
                choice = app.getIntInput();

                switch (choice)
                {
                    case 1:
                        app.chooseFoodCategories();
                        break;
                    case 2:
                        app.chooseLeastStars();
                        break;
                    case 3:
                        app.choosePriceCategories();
                        break;
                    case 4:
                        continueFiltering = false;
                        break;
                    default:
                        System.out.println("Invalid option, try again.");
                }
            }
        }

        //send the filter to the server
        new Thread(() ->
                app.customer.search(app.filter, app::handleSearchResults)).start();
        loading = true;
        app.playAnimation();
    }
}
