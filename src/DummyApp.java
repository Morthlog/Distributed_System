import java.util.*;

public class DummyApp
{
    private static volatile boolean loading;
    private static volatile boolean animationIsDone;
    private Scanner keyboard;
    private Customer customer;
    Filter filter;
    Store selectedStore;

    public DummyApp()
    {
        keyboard = new Scanner(System.in);
        filter = new Filter();
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
        System.out.println("Default filters set");
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
            System.out.printf("%d: %s. Food Category: %s. Stars: %.1f. Price Category: %s%n",
                    i,
                    store.getStoreName(),
                    store.getFoodCategory(),
                    store.getStars(),
                    store.getPriceCategory());

        }
    }

    public Store chooseStore(List<Store> stores)
    {
        System.out.println("Enter the number of the store you want to view products for:");
        int storeIndex = getIntInput();
        return stores.get(storeIndex);
    }

    public String[] displayStoreProducts(Store store)
    {
        Map<String, Product> products = store.getProducts();
        System.out.println("Products in " + store.getStoreName() + ":");
        int i = 0;
        String[] keyMapping = new String[products.size()];
        for (Product product : products.values())
        {
            System.out.println(i + ": " + product.getProductName() + ". Price: " + product.getPrice());
            keyMapping[i] = product.getProductName();
            i++;
        }
        return keyMapping;
    }

    public void chooseProducts(Store store)
    {
        customer.clearShoppingCart();
        customer.addStoreNameToCart(store.getStoreName());
        while (true)
        {
            String[] keyMapping = displayStoreProducts(store);
            System.out.println("Type product number to add to cart (or -1 to finish):");
            int chosenIndex = getIntInput();

            if (chosenIndex == -1)
            {
                break;
            }

            String selectedProductName = keyMapping[chosenIndex];
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
            playAnimation();// loading must be put after the call or in new thread, else the thread will get stuck
        }
        else if (choice == 2)
        {
            System.out.println("Order canceled. Restarting...");
            runShopping();
        }
        else
        {
            System.out.println("Closing app...");
        }
    }

    public void playAnimation()
    {
        animationIsDone = false;
        loading = true;
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
                System.out.print(text + "\r");
            }
        }
        System.out.print(" ".repeat(text.length()) + "\r");
        animationIsDone = true;
    }

    private void handleSearchResults(List<Store> stores)
    {
        stopAnimation();
        displayStores(stores);

        if (stores.isEmpty())
        {
            System.out.println("Try with new filters. Restarting...");
            runShopping();
            return;
        }

        selectedStore = chooseStore(stores);
        chooseProducts(selectedStore);
        finalizeOrder();
    }

    private void handleBuyResult(String confirmation)
    {
        stopAnimation();
        System.out.println("Purchase response: " + confirmation);
        rateStore();
    }

    private void handleRatingResult(String confirmation)
    {
        stopAnimation();
        System.out.println("Rating response: " + confirmation);

        System.out.println("How do you want to continue?");
        System.out.println("1. Make new order");
        System.out.println("2. Close the app");
        int choice = getIntInput();

        if (choice == 1)
        {
            runShopping();
        }
        else
        {
            System.out.println("Closing app...");
        }
    }

    private void rateStore()
    {
        System.out.println("Rate store from 1 to 5.");
        int rating = getIntInput();
        new Thread(() -> customer.rateStore(this::handleRatingResult, selectedStore.getStoreName(), rating)).start();

        playAnimation();
    }

    public void sendFilters()
    {
        //send filter to the server and use callback for the answer
        new Thread(() -> customer.search(filter, this::handleSearchResults)).start();
        playAnimation();
    }

    private void runShopping()
    {
        setDefaultFilters();
        int choice;

        System.out.println("Coordinates detected: latitude = " + filter.getLatitude() + ", longitude = " + filter.getLongitude());
        System.out.println("Do you want to enter new coordinates?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        choice = getIntInput();
        if (choice == 1)
        {
            inputCoordinates();
        }

        System.out.println("Do you want to filter the stores?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        choice = getIntInput();

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
                choice = getIntInput();

                switch (choice)
                {
                    case 1:
                        chooseFoodCategories();
                        break;
                    case 2:
                        chooseLeastStars();
                        break;
                    case 3:
                        choosePriceCategories();
                        break;
                    case 4:
                        continueFiltering = false;
                        break;
                    default:
                        System.out.println("Invalid option, try again.");
                }
            }
        }

        sendFilters();
    }

    private void stopAnimation(){
        loading = false;
        while (!animationIsDone) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args)
    {
        DummyApp app = new DummyApp();
        app.inputCustomerName();
        app.runShopping();
    }
}
