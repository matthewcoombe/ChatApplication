import java.io.*;
import java.util.*;
import java.net.*;
import java.net.*;

/*
* 02/03/2019
* Chat application on the Client side the Client class. The client runs this on a different host computer, port 12001 hard coded.
* Authored by kagiso, Mathew & Paul
*/

public class Client {
  private String host;
  private int port;

  /**
    Constructor initialise the server IP address host and client port
  */
  public Client(String host, int port){
    this.host = host;
    this.port = port;
  }

  /**
    Run the client as a thread and listen for server changes, broadcasts or messages.
  */
  public void run() throws UnknownHostException, IOException{
    Socket client = new Socket(host, port);
    // Proide feedback for the client connecting to the server
    System.out.println("You have successfully connected to the server");
    PrintStream output = new PrintStream(client.getOutputStream());
    Scanner scanner = new Scanner(System.in);
    System.out.print("Name: ");
    String name= scanner.nextLine();
    output.println(name); // notify the server of the clients name
    new Thread(new HandleMessage(client.getInputStream())).start();
    System.out.println("Messages: \n");
    while(scanner.hasNextLine()){ //send new messages from this client to the server
      output.println(scanner.nextLine());
    }

    output.close(); //close output stream
    scanner.close();  //close the Scanner
    client.close(); //close the socket
  }

  public static void main(String[] args) throws UnknownHostException, IOException{
    System.out.println("Please enter the server IP address");
    Scanner scanner = new Scanner(System.in);
    String line = scanner.nextLine(); // get the server IP address from the user
    line.replace("\n","");            // Correct the formating
    new Client(line, 12001).run();
  }


public class HandleMessage implements Runnable{
  private InputStream server;

  /**
  * Constructor initialises the HandleMessage class with a server to derive input from
  */
  public HandleMessage(InputStream server){
    this.server = server;
  }

  /**
  * This thread receives messages and prints them to the users screen
  */
  public void run(){
    Scanner scanner = new Scanner(server);
    String temp = "";
    while(scanner.hasNextLine()){
      temp = scanner.nextLine();
      if(temp.charAt(0) =='['){
        System.out.println("\n User list: "+new ArrayList<String>(Arrays.asList(temp.split(",")))+"\n");
      } else{
        try{
          System.out.println("\n");
        } catch(Exception ignore){}
      }
    }
    scanner.close();
  }// close run method

}
 
}
