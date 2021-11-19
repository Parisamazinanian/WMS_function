package main.java;


import main.java.data.Item;
import main.java.data.StockRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.java.data.StockRepository.getWarehouses;

/**
 * Provides necessary methods to deal through the Warehouse management actions
 *
 * @author riteshp
 */
public class TheWarehouseManager {
  // =====================================================================================
  // Member Variables
  // =====================================================================================

  // To read inputs from the console/CLI
  private final Scanner reader = new Scanner(System.in);
  private final String[] userOptions = {
    "1. List items by warehouse", "2. Search an item and place an order", "3. Browse by category", "4.Quit"
  };
  // To refer the user provided name.
  private String userName;
  //Declare and initialize an empty static List of String named 'SESSION_ACTIONS'.
  private static List<String> SESSION_ACTIONS = new ArrayList<String>();

  // =====================================================================================
  // Public Member Methods
  // =====================================================================================

  /** Welcome User */
  public void welcomeUser() {
    this.seekUserName();
    this.greetUser();
  }

  /** Ask for user's choice of action */
  public int getUsersChoice() {
    //Ask the user to insert a number to show choose a performance
    int choice;
    while (true) {
      System.out.println("Please choose 1, 2 , 3 or 4 for: \n"+ Arrays.toString(userOptions));
      choice = reader.nextInt();
      if (choice > 3 || choice < 1) {
        System.out.println("Please enter a valid number. ");
      } else {
        break;//if the user types the correct option 1, 2 or 3, then it will break and show the result. Otherwise the while loop ask the user to give the correct option
      }
    }
    return choice;

  }

  /** Initiate an action based on given option */
  public void performAction(int option) {
    // 4. The user is asked to pick a choice using the numeric values associated
    switch (option) {
      case 1:
        this.listItemsByWarehouse();
        break;
      case 2:
        // Search an item and place an order
        this.searchItemAndPlaceOrder();
        break;
      case 3: // 3. Browse by category
        this.browseByCategory();
        break;
      case 4: // "4. Quit"
        this.quit();
        break;
      default:
        System.out.println("Sorry! Please put a valid option.");
    }
  }


  /**
   * Confirm an action
   *
   * @return action
   */
  public boolean confirm(String message) {
    // a boolean method for the time we ask yes or no question from the user
    String choice ;
    do{
      System.out.printf("%s (y/n)\n",message);
      choice = this.reader.next();
      choice += this.reader.nextLine();
      if(choice.length() > 0){
        choice = choice.trim();
      }
      choice = choice.toLowerCase();

    }while(! choice.startsWith("y") && ! choice.startsWith("n"));
    //check if the user choice is yes : true otherwise false
    return choice.startsWith("y");
  }

  /** End the application */
  public void quit() {
    System.out.printf("\nThank you for your visit, %s!\n", this.userName);
    this.listSessionActions();
    System.exit(0);
  }

  // =====================================================================================
  // Private Methods
  // =====================================================================================

  /** Get user's name via CLI */
  private void seekUserName() {
    // 1.The user is asked to provide a name.
    System.out.println("What is your name?");
    this.userName= reader.nextLine();
  }

  /** Print a welcome message with the given user's name */
  private void greetUser() {
    // 2. The user is greeted by its name.
    System.out.println("Hello "+ this.userName+". Welcome to our warehouse");
  }

  private void listItemsByWarehouse() {
    // 4.i>> list items of the warehouses
    //Items of first warehouse
    /*System.out.println("Items in warehouse 1:");
    this.listItems(WAREHOUSE1);
    //Items of the second warehouse
    System.out.println("Items in warehouse 1:");
    this.listItems(WAREHOUSE2);*/
    Set<Integer> warehouses = getWarehouses();
    for (int warehouse :warehouses ) {
      System.out.printf("Items in warehouse %d:%n",warehouse);
      this.listItems(StockRepository.getItemsByWarehouse(warehouse));
    }
    System.out.println();
    //how many items in each warehouse :
    for (int warehouse :warehouses ) {
      System.out.printf("Total items in warehouse %d: %d%n",warehouse, StockRepository.getItemsByWarehouse(warehouse).size());

    }
    //System.out.println("Listed "+getTotalListedItems(StockRepository.getAllItems())+" items");
    this.SESSION_ACTIONS.add("Listed "+getTotalListedItems(StockRepository.getAllItems())+" items");
    //System.out.println("Searched "+getAppropriateeIndefiniteArticle()+askItemToOrder());
    //this.SESSION_ACTIONS.add("Searched "+getAppropriateeIndefiniteArticle()+askItemToOrder());
    //System.out.println("Browsed the category"+browseByCategory());
    //this.SESSION_ACTIONS.add("Browsed the category"+browseByCategory());

  }

  private void listItems(List<Item> items) {
    // 4.i>> list of the items by each warehouse
    for (Item item : items) {
      System.out.printf("\n- %s\n", item.toString());
    }
  }

  private void searchItemAndPlaceOrder() {
    // input an item name
    String itemName =askItemToOrder();
    //The amount of available items
    int availableAmount= this.getAvailableAmount(itemName);
    //ask it they want to place an order
    if(availableAmount>0){
      this.askAmountAndConfirmOrder(availableAmount, itemName);
    }
    //System.out.println("Searched "+getAppropriateeIndefiniteArticle(itemName)+itemName);
    this.SESSION_ACTIONS.add("Searched "+getAppropriateeIndefiniteArticle(itemName)+itemName);
  }

  /**
   * Ask the user to specify an Item to Order
   *
   * @return String itemName
   */
  private String askItemToOrder() {
    // 4.ii.a >> ask the user to import an item name
    System.out.println("Please enter an item name: ");
    String ItemName= this.reader.next().toLowerCase();
    ItemName +=reader.nextLine().toLowerCase();

    return ItemName;
  }

  /**
   * Calculate total availability of the given item
   *
   * @param itemName itemName
   * @return integer availableCount
   */
  private int getAvailableAmount(String itemName) {
    // 4.ii.b.b>>>The total amount of items in any warehouse that match that name.
    //The location of those items: the name of the warehouse (ex: Warehouse 1), if it can only be found in one, Both warehouses if it is in both and Not in stock if it is in none.
    //If it can be found in more than one warehouse, it will also print a line saying which warehouse has the highest amount of those items (and how many does it have).
    //Set<Integer> warehouses =StockRepository.getWarehouses();//It provides a set of warehouse ids
    int totalCount = 0;
    /*for (int Id:warehouses) {
      totalCount +=find(itemName,StockRepository.getItemsByWarehouse(Id));//counting the total amount of the item in the warehouses
    }*/
    // get warehouse wise availability
    int maxWarehouse = 0;
    int maxAvailability = 0;
    Set<Integer> warehouses =StockRepository.getWarehouses();
    for (int id : warehouses) {
      totalCount +=find(itemName,StockRepository.getItemsByWarehouse(id));
      int whCount = find(itemName,StockRepository.getItemsByWarehouse(id));
      if (whCount > maxAvailability) {
        maxWarehouse = id;
        maxAvailability = whCount;
      }
    }
    if(totalCount==0){
      System.out.println("Not in stock");
    }else {
      System.out.println("Amount available: "+totalCount);
      System.out.println("Maximum availability: "+ maxAvailability+" in Warehouse "+maxWarehouse);
    }
    
      

    return totalCount;



    /*int count1= find(itemName,StockRepository.getItemsByWarehouse(1));
    int count2= find(itemName,StockRepository.getItemsByWarehouse(2));
    int totalCount = count1+count2;
//the total amount of item in warehouse
    System.out.println("The total number of " + itemName + " in our warehouses: " + totalCount);
    //Not in stock
    if(totalCount==0){
      System.out.println("Location: Not in stock");
    }else {
      //when the item is in both warehouses
      if (count1 > 0 && count2 > 0) {
        //System.out.println("Both warehouses");
        //which warehouse has the highest
        if (count1 > count2) {
          System.out.println("Warehouse 1 has the highest amount of " + itemName + " equal to: " + count1);
        } else if (count1 < count2) {
          System.out.println("Warehouse 2 has the highest amount of " + itemName + " equal to: " + count2);
        } else {
          System.out.println("There are the same amounts of the item in both Warehouses: " + count1 + " in each");
        }
      } else {
        if (count1 > 0) {
          System.out.println("Warehouse 1 has the highest amount of " + itemName + " equal to: " + count1);
        } else {
          System.out.println("Warehouse 2 has the highest amount of " + itemName + " equal to: " + count2);
        }
      }
    }
    return totalCount;*/
  }

      /**
       * Find the count of an item in a given warehouse
       *
       * @param item the item
       * @param warehouse the warehouse
       * @return count
       */
      private int find (String item, List<Item> warehouse){
       /* // 4.ii.b.a>> find the amount of the item in any of the warehouses
        //every element of the warehouse array is should be equal to the item name
        int count = 0;
        for (String x : warehouse) {
          if (item.equals(x)) {
            count++;
          }
        }
        return count;
      }*/
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        int count=0;
        for(Item element:warehouse){
         // System.out.println(String.format("%s %s", element.getState().toLowerCase(), element.getCategory().toLowerCase()));
          if(item.equals(String.format("%s %s", element.getState().toLowerCase(), element.getCategory().toLowerCase()))) {
            LocalDate itemStockDate = element.getDateOfStock().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            System.out.println("- Warehouse "+element.getWarehouse()+ " (in stock for "+ ChronoUnit.DAYS.between(itemStockDate, now)+" days)");
            count++;
          }
        }
        return count;
      }
  /** Ask order amount and confirm order */
  private void askAmountAndConfirmOrder(int availableAmount, String item) {
    // 4.ii.c >> ask the user if they want to order
    boolean toOrder = this.confirm("Would you like to order the searched item?");
    if (toOrder) {
      //If the answer is yes, it should ask the user how many do they want
      int orderAmount = this.getOrderAmount(availableAmount);
      //System.out.println(orderAmount);
      //if the orderAmount is -1 then just go to the searchItemAndPlaceOrder()  :
      if (orderAmount > 0) {
        System.out.printf("%d %s %s been ordered\n", orderAmount, item, (orderAmount == 1 ? "has" : "have"));
      }
    }
  }

  /**
   * Get amount of order
   *
   * @param availableAmount
   * @return
   */
  //If the answer is yes, it should ask the user how many do they want.
  //If the desired amount is equal or lower than the total available, it will proceed and show a message saying the order has been placed. The message should show the item name and amount ordered.
  //If the desired amount is higher than the total available, it should show an error message and should ask the user if they want to order the maximum available, instead.
  private int getOrderAmount(int availableAmount) {
    // If the answer is yes we should ask the user how many do they want
    System.out.println("How many of the item do you want to order? ");
    int orderAmount = -1;
    do{
      // read the amount from the cli
      orderAmount = Integer.parseInt(this.reader.nextLine());
      // if the orderAmount is more than the availableAmount
      if(orderAmount > availableAmount){
        // an error message and should ask
        System.out.println("The desired amount is higher than the total available. The maximum amount is: "+ availableAmount);
        System.out.println("=================================================");
        // ask if they want to order the maximum available, instead.
        boolean orderAll = this.confirm("Would you like to order the maximum available?");
        if(orderAll){
          orderAmount = availableAmount;
        }
        else{
          boolean keepOrdering = this.confirm("Do you want to order another amount of this item?");
          if(keepOrdering){
            orderAmount = -1;
          }else{
            orderAmount = 0;
          }

        }
      }else if(orderAmount < 0){
        // the orderAmount is less than or equal to 0
        // show error and ask the user to enter a valid amount
        System.out.println("Sorry !! the amount is not valid, please enter a value more than 0");
      }else if(orderAmount<=availableAmount){
        return orderAmount;
      } else{
        // -1 : this.getUsersChoice();
        return -1;
      }
    }while(orderAmount < 0 || orderAmount > availableAmount);

    return orderAmount;
  }

  //Option 3: Brows category
  private void browseByCategory() {
    Map<String, List<Item>> categoryWiseItems = new HashMap<>();
    List<String> categories = new ArrayList<>(StockRepository.getCategories());
    System.out.println(categories);
    for (int i = 0; i < categories.size(); i++) {
      String category = categories.get(i);
      List<Item> catItems = StockRepository.getItemsByCategory(category);
      categoryWiseItems.put(category, catItems);
      System.out.printf("%d. %s (%d)%n", (i + 1), category, catItems.size());
    }

    int catIndex=0;
    do {
      System.out.println("Type the number of the category to browse:");
      try {
        catIndex = reader.nextInt();

      } catch (Exception e) {
        System.out.printf("Enter an integer between 1 and %d%n", categories.size());
      }

    } while (catIndex <= 0 || catIndex > categories.size());

    String category = categories.get(catIndex - 1);
    System.out.printf("List of %ss available:%n", category.toLowerCase());
    List<Item> catItems = categoryWiseItems.get(category);
    for (Item item : catItems) {
      System.out.printf("%s, Warehouse %d%n", item.toString(), item.getWarehouse());
    }
    //System.out.println("Browsed the category"+category.toLowerCase());
    this.SESSION_ACTIONS.add("Browsed the category"+category.toLowerCase());

    //String Browsedthecategory=category.toLowerCase();
    //System.out.println(Browsedthecategory);
  }
  //Create a method named getTotalListedItems which returns an integer value that is the number of the total items in the list.
private int getTotalListedItems(List<Item> ListOfItems ){
    return ListOfItems.size();
}

private String getAppropriateeIndefiniteArticle(String itemName){
  Pattern p = Pattern.compile("[aeiou]", Pattern.CASE_INSENSITIVE);
  Matcher m = p.matcher(Character.toString(itemName.charAt(0)));
  if (m.find()) {
    return "an ";
  }
  else {
    return"a ";
  }
  /*char ch=itemName.charAt(0);
  if(ch=='a' || ch=='A' || ch=='e' || ch=='E' ||
          ch=='i' || ch=='I' || ch=='o' || ch=='O' ||
          ch=='u' || ch=='U')
  {
    return "an ";
  }
  else {
    return "a ";
  }*/
}

//Create a private method named listSessionActions which
// returns nothing and prints the Session summary at the end of the session. Call this method inside the quit() method before System.exit(0)
private void listSessionActions(){
    if(this.SESSION_ACTIONS.size()>0){
      System.out.println("In this session you have: ");
      for(int i=0;i<this.SESSION_ACTIONS.size() ;i++){
        System.out.println((i+1)+". "+SESSION_ACTIONS.get(i));
      }
    }else {
      System.out.println("In this session you have not done anything.");
    }
}
}

