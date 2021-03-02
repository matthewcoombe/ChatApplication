import java.io.*;
import java.net.*;
import java.util.*;

  /*
  * 02/03/2019
  * Chat application Server class to host the service of the server, port 12001, host IP Address
  * Authored by Kagiso, Mathew & Paul
  */

public class Server{

  private int port;
  private List<User> clientList;
  private ServerSocket server;

/**
  The constructor creates a server with the designated port and initialises the clientList of type
*/
  public Server(int port){
    this.port = port;
    this.clientList = new ArrayList<User>();
  }

/**
  Run the thread and listen for clients that want to access the server (join or send messages), authorise and host the service
*/
  public void run() throws IOException{
    server = new ServerSocket(port){  //Create the Server-side socket so that the server can be found by clientList
      protected void endServer() throws IOException{
        this.close();   //Has the endServer call to close the serverSocket
      }
    };
    System.out.println("Port 12001 is now open.");
    while(true){
      Socket client = server.accept();  // accept a new client
      String name = (new Scanner(client.getInputStream())).nextLine();
      name = name.replace(",","");
      name = name.replace(" ", "_");
      // Output feedback to the Server
      System.out.println("New participant: \""+name+"\"\n\t Coming from: "+client.getInetAddress().getHostAddress());
      User user = new User(client, name);
      this.clientList.add(user); // add the new client to the list of clientList
      user.getOutStream().println("Welcom to the chat room "+user.toString()+" this service is hosted at UCT, brought to you by Kagiso, Mathew and Paul"); // provide the user with feedback
      new Thread(new HandleClient(this, user)).start(); // Create a thread to listen for messages from the user
    }
  }

  /**
    remove a client from the client list after they disconnect
  */
  public void removeClient(User user){
    this.clientList.remove(user);
  }

  /**
    Send a message to all clients as received by the server from a single client
  */
  public void broadcastMessage(String message, User userMessage){
    for(User user : this.clientList){
      user.getOutStream().println(userMessage.toString()+"<span>: " + message+"</span>");
    }
  }

  /**
    Share the list of clients
  */
  public void allClients(){
    for(User user : this.clientList){
      user.getOutStream().println(this.clientList);
    }
  }

  /**
    Send a private message to a user
  */
  public void sendDirectMessage(String message, User user, String destUser){
    boolean find = false; //This user is not in the clientList
    for(User i : this.clientList){
      if(i.getName().equals(destUser) && i != user){
        find = true; // The user exists
        user.getOutStream().println(user.toString()+ " <to> "+ i.toString()+": "+message); //send the message to the destination user from the 'i' user
        i.getOutStream().println("dm: "+ user.toString()+": "+message);
      }
    }
    if(!find){ //user not found
      user.getOutStream().println(user.toString()+" is not an available user: "+message);
    }
  }

/**
   // A new thread is created to run the server
*/
  public static void main(String[] args) throws IOException{
    new Server(12001).run();
  }

class User{
  private int numberOfUsers = 0;
  private int userNo;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String name;
  private Socket client;

  /**
    Constructor initialises stream in and out, client, names the client, assigns the user a number, increases the total number of users
  */
  public User(Socket client, String clientName) throws IOException{
  this.streamOut = new PrintStream(client.getOutputStream());
  this.streamIn = client.getInputStream();
  this.client = client;
  this.name = clientName;
  this.userNo = numberOfUsers;
  numberOfUsers += 1;
  }

  /**
    return the PrintStream
  */
  public PrintStream getOutStream(){
    return this.streamOut;
  }

  /**
    return the InputStream
  */
  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getName(){
    return this.name;
  }

  public String toString(){
    return this.getName();
  }
}

class HandleClient implements Runnable{
  private Server server;
  private User user;

  /**
    Constructor initialises the server and user class, notifies all other clients of the new clients addition to the server
  */
  public HandleClient(Server server, User user){
    this.server = server;
    this.user = user;
    this.server.allClients();
  }

  /**
    Listens for new messages sent to the client
  */
  public void run(){
    String message;

    Scanner scanner = new Scanner(this.user.getInputStream());
    while(scanner.hasNextLine()){
      message = scanner.nextLine();
      if(message.charAt(0)=='@'){ //Is this a personal message for a single user?
        if(message.contains(" ")){
          System.out.println("dm: "+message);
          int space = message.indexOf(" "); // locate the first space
          String directMessage = message.substring(1, space);
          this.server.sendDirectMessage(message.substring(space+1, message.length()), user, directMessage);
        } 
      }else {
          server.broadcastMessage(message, user); // send the broadcast message to all clients
        }
	}  
      server.removeClient(user); // once the thread is finished remove the user
      this.server.allClients(); // notify all clients of the client that has left
      scanner.close();
  }
}

}
