import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.net.*;
import static java.lang.Thread.sleep;

public class Master extends Thread
{
	TCPServer serverClient = null;
	List<TCPServer> serverWorker = new ArrayList<>();

	public Master(Socket connection, Connection type, List<TCPServer> workers)
	{
		if (type == Connection.Client)
			serverClient = new TCPServerClient(connection, type);
		this.serverWorker = workers;
	}

	public Master()
	{
	}

	public Object startForBroker(Filter filter)
	{
		Object response = null;
		try
		{
			System.out.println("Waiting for connection...");
			serverWorker.get(0).startConnection();
			serverWorker.get(0).out.writeObject(filter);
			serverWorker.get(0).out.flush();
			response = serverWorker.get(0).in.readObject();
			if (response != null)
			{
				System.out.println("Got message: " + response);
			}
			else
			{
				System.out.println("unrecognised greeting");
			}
		}
		catch (IOException | ClassNotFoundException e)
		{
			System.err.println("Couldn't start server: " + e.getMessage());
		}
		return response;
	}

	private Object processPurchase(java.util.Set<String> cart)
	{
		Object response = null;
		try
		{
			System.out.println("Processing purchase request...");
			serverWorker.get(0).startConnection();
			serverWorker.get(0).out.writeObject(cart);
			serverWorker.get(0).out.flush();
			response = serverWorker.get(0).in.readObject();
			if (response != null)
			{
				System.out.println("Got purchase response: " + response);
			}
			else
			{
				System.out.println("unrecognised purchase response");
			}
		}
		catch (IOException | ClassNotFoundException e)
		{
			System.err.println("Error processing purchase: " + e.getMessage());
		}
		return response;
	}

	private void startForClient()
	{
		try
		{
			Object request = serverClient.in.readObject();
			if (request instanceof Filter)
			{
				System.out.println("Client asked for: " + request);
				Object response = startForBroker((Filter) request);
				serverClient.out.writeObject(response);
			}
			else if (request instanceof java.util.Set)
			{
				Object response = processPurchase((java.util.Set<String>) request);
				serverClient.out.writeObject(response);
			}
			serverClient.out.flush();
		}
		catch (IOException | ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void run()
	{
		this.startForClient();
	}

	public static void compile()
	{
		try
		{
			System.out.println("Compile Worker...");
			ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
					"del Worker.class && javac Worker.java && javac stubUser.java");
			Process p = pb.start();
			p.waitFor();
		}
		catch (Exception e)
		{
			System.err.println("Could not compile Worker");
		}
	}

	public void connectWorkers(int size)
	{
		for (int i = 0; i < size; i++)
		{
			serverWorker.add(new TCPServer(TCPServer.basePort + i + 1, Connection.Broker));
			System.out.println(TCPServer.basePort + i + 1);
		}
	}

	public static void main(String[] args)
	{
		compile();
		final int n_workers = Integer.parseInt(args[0]);
		final String DATA_PATH = "./Data/Stores.json";
		final Scanner on = new Scanner(System.in);
		Process[] workers = new Process[n_workers];
		Master server = new Master();
		server.connectWorkers(n_workers);
		for (int i = 0; i < n_workers; i++)
		{
			try
			{
				System.out.println("Starting worker #" + i + "...");
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "java Worker " + i);
				workers[i] = pb.start();
			}
			catch (IOException e)
			{
				System.err.printf("Could not start worker #%d\n", i);
			}
		}
		JSONArray stores;
		try
		{
			Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
			stores = (JSONArray) ((JSONObject) temp).get("Stores");
		}
		catch (Exception e)
		{
			System.out.println("JSON data could not be parsed");
			throw new RuntimeException();
		}
		int currentWorker = 0;
		for (var obj : stores)
		{
			JSONObject store = (JSONObject) obj;
			currentWorker = (currentWorker + 1) % n_workers;
		}
		ServerSocket serverClient;
		try
		{
			serverClient = new ServerSocket(TCPServer.basePort);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			while (true)
			{
				Socket serverSocket = serverClient.accept();
				Thread t = new Master(serverSocket, Connection.Client, server.serverWorker);
				t.start();
				sleep(0);
				if (false)
					break;
			}
			System.out.println("Would you like to exit?");
			String answer = on.nextLine();
			System.out.println(answer);
			if (answer.equals("Yes"))
			{
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			sleep(15000);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
