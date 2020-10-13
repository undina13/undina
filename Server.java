package com.javarush.task.task30.task3008;



import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();

    public static void sendBroadcastMessage(Message message) {
        for (Connection value : connectionMap.values()) {
            try {
                value.send(message);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("невозможно отправить сообщение");
            }


        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if ((message.getType() == MessageType.USER_NAME) && (!message.getData().equals("")) && (!connectionMap.containsKey(message.getData()))) {


                    connectionMap.put(message.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return message.getData();


                }

            }
        }


        private void notifyUsers(Connection connection, String userName) throws IOException{
            for(String key : connectionMap.keySet()){
                if (!key.equals(userName)){
                    connection.send(new Message(MessageType.USER_ADDED, key));
                }

            }


        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
              Message text =  connection.receive();
                if(text.getType()==(MessageType.TEXT)){
                    String s = String.format("%s: %s", userName, text.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, s));

                }
                else {
                    ConsoleHelper.writeMessage("Ошибка! это не текст");
                }
            }

        }

        public void run(){
            String userName = "";
            ConsoleHelper.writeMessage("установлено новое соединение с удаленным адресом" + socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                //ConsoleHelper.writeMessage("установлено новое соединение с удаленным адресом" + connection.getRemoteSocketAddress());

                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

                }

             catch (IOException e) {
               ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным адресом");
            }
            catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным адресом");
            }

            finally {
                {
                    if(!userName.equals("")){
                        sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                        connectionMap.remove(userName);
                    }

                    ConsoleHelper.writeMessage("соединение с удаленным адресом закрыто");
                }
            }


        }


           


    }

    public static void main(String[] args) throws IOException {


        int portNumber = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }




        }







