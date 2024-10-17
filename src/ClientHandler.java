import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

  private static ArrayList<ClientHandler> clients = new ArrayList<>();
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private String clientUserName;

  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.clientUserName = reader.readLine();
      clients.add(this);
      broadcastMessage("Server: " + clientUserName + " has joined the chat.");
    } catch (IOException e) {
      closeEverything(socket, reader, writer);
    }
  }

  @Override
  public void run() {
    String message;
    while (socket.isConnected()) {
      try {
        message = reader.readLine();
        broadcastMessage(clientUserName + ": " + message);

      } catch (IOException e) {
        closeEverything(socket, reader, writer);
        break;
      }
    }
  }

  private void broadcastMessage(String message) {
    for(ClientHandler client: clients) {
      try {
        if(!client.clientUserName.equals(clientUserName)) {
          client.writer.write(message);
          client.writer.newLine();
          client.writer.flush();
        }
      } catch (IOException e) {
        closeEverything(socket, reader, writer);
      }
    }
  }
  
  public void removeClient() {
    clients.remove(this);
    broadcastMessage(clientUserName + " has left the chat.");
  }

  private void closeEverything(Socket socket2, BufferedReader reader2, BufferedWriter writer2) {
    removeClient();
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

}
