import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private String clientUserName;

  public Client(Socket socket, String username) {
    try {
      this.socket = socket;
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.clientUserName = username;
    } catch (IOException e) {
      closeEverything(socket, reader, writer);
    }
  }

  public void sendMessage() {
    try {
      writer.write(clientUserName);
      writer.newLine();
      writer.flush();

      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()) {
        String messageToSend = scanner.nextLine();
        writer.write(messageToSend);
        writer.newLine();
        writer.flush();
      }
    } catch (IOException e) {
      closeEverything(socket, reader, writer);
    }
  }

  public void listenForMessage() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        String message; 
        while(socket.isConnected()) {
          try {
            message = reader.readLine();
            System.out.println(message);
          } catch (IOException e) {
            closeEverything(socket, reader, writer);
            break;
          }
        }
      }
    }).start();
  }

  private void closeEverything(Socket socket2, BufferedReader reader2, BufferedWriter writer2) {
    try {
      if (reader != null) {
        reader.close();
      }

      if (writer != null) {
        writer.close();
      }

      if (socket != null) {
        socket.close();
      }

    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      Scanner scanner = new Scanner(System.in);
      System.out.println("Enter your username for the group chat: ");
      String username = scanner.nextLine();
      Socket socket = new Socket("localhost", 3000);
      Client client = new Client(socket, username);
      client.listenForMessage();
      client.sendMessage();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
