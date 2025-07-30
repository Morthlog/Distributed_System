# Food delivery app
The objective of this project was to design and implement a **multithreaded distributed system** for a food delivery application, utilizing the **MapReduce** framework. Due to constraints prohibiting the use of the *java.util.concurrent* package, synchronization was handled manually using Java’s built in locking mechanisms, specifically the *synchronized* keyword in combination with *wait* and *notify* methods. It was developed during the 5th semester as part of the Distributed Systems course at AUEB.


## Tasks

We were tasked with implementing the following:

* A [backend](Backend) system which:
    * Enables communication using TCP sockets.
    * Supports **manual thread synchronization** using *synchronized*, *wait*, and *notify* — java.util.concurrent was prohibited.
    * Supports **passive replication**, allowing for seamless operation in the event of a single Broker failure.

* A [frontend](CustomerApp) app for the customer which: 
    * Supports **asynchronous** communication

## Architecture

* The [Master](Backend/src/Master.java) runs on a dedicated serverSocket and handles incoming connections from multiple Clients. It also:

	* Maintains a separate serverSocket to communicate with the Reducer.

	* Manages a dedicated serverSocket for each Broker.

	* Coordinates the flow of data between components.


* Clients (either [Customers](Backend/src/Customer.java) or [Managers](Backend/src/ManagerConsoleApp.java)) connect to the Master to send requests. Based on the request type, the Master either forwards it to a specific Broker or broadcasts it to all Brokers.

* [Brokers](Backend/src/Worker.java):

    * Maintains its own data as well as redundant data from other Brokers for fault tolerance.

    * Responds to requests from the Master.

    * Communicates with the Reducer when participating in broadcast operations.

    * Notifies the Master to update redundant copies when its own memory state changes.

* [Reducer](Backend/src/Reducer.java):

    * Accepts data from all Brokers during broadcast operations on its own serverSocket.
        * Master uses the port to update the number of active Brokers 

    * Aggregates or processes the data and sends the result back to the Master.

* Redundancy & Recovery:

    * When a Broker updates its memory, it asks the Master to synchronize redundant copies across the system.

    * If a Broker fails, the Master instructs other Brokers to promote relevant redundant data to active memory.

    * Once the failed Broker is back online, the Master restores its state by:

		* Pausing new requests from Clients

        * Sending back its updated data.

        * Reverting any promoted data on other Brokers back to redundancy.
        
		* Resuming requests from Clients. 

* Master, Brokers and Reducer run multithreaded.

## How to run

1. Download the repository with
` git clone https://github.com/Morthlog/Distributed_System `

2. Open the backend folder using IntelliJ IDEA.

   * Project linking is handled automatically via the .idea directory.

If everything runs on the same device, go to step 6

3. Inside IntelliJ press Ctrl + Shift + F
   
4. Search for `InetAddress.getLocalHost().getHostAddress()
`

5. Replace each usage with a hardcoded IP string, e.g.: `"192.168.2.1"`
   * Replace "192.168.2.1" with the local IP address of the device hosting the target component.

    *   | Component 				| Needs ip of 	|  
     	|-							|-				|
     	| ManagerConsoleApp 		| Master		|  
     	| Customer 					| Master 		|   
     	| Master 					| Reducer      	|   
     	| Reducer 					| Master 		|  
     	| Worker (Main) 			| Master 		|  
     	| Worker (ManageRequest) 	| Reducer 		|  

	* Don’t forget to comment out try-catch blocks wherever you change the ip.

6. Start Master with argument the number of Brokers.
7. Start each Broker with argument his id in order, from 0 to N-1, where N the argument in step 6.
8. Start Reducer.

The Backend system is up and running. You can now run any number of Managers or Clients. If you want to run the application:

1. Open CustomerApp using Android Studio.

2. If running on the same device as Master, no changes are required.

3. If running on a different device/emulator:

    * Press Ctrl + Shift + F in Android Studio.

    * Search for: `ip = "10.0.2.2";`

	* Replace with the IP address of the Master, e.g.:
    `ip = "192.168.2.1";`

4. Build and run the app.

## Contributors
<a href="https://github.com/Morthlog/Distributed_System/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Morthlog/Distributed_System"/>
</a>
