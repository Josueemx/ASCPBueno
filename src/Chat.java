import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

public class Chat implements Runnable {
    private int port;
    private String hostname;
    private boolean encrypted;
    private String key;
    private boolean server;

    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private ServerSocket serverSocket;

    private JTextArea textArea;

    private boolean close = false;

    boolean authenticated = false;
    private int step = 1;
    private long q = 2426697107L;//353;
    private long a = 17123207L;//3;
    private long xa;
    private long xb;
    private long ka;
    private long kb;
    private long ya;

    private String K;

    public Chat(int port, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = false;
        this.textArea = textArea;
        serverSocket = new ServerSocket(port);

    }

    public Chat(int port, String key, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = true;
        this.textArea = textArea;
        this.key = key;
        serverSocket = new ServerSocket(port);
    }

    public Chat(String hostname, int port, JTextArea textArea) throws Exception {
        encrypted = false;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        socket = new Socket(hostname, port);
    }

    public Chat(String hostname, int port, String key, JTextArea textArea) throws Exception {
        encrypted = true;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        this.key = key;
        socket = new Socket(hostname, port);
    }

    public void sendMessage(String message) throws Exception {
        byte[] data = DataStructure.getPlainMessage(message);

        if(authenticated) {
            outputStream.write(DataStructure.encryptData(data, K));
        } else {
            outputStream.write(data);
        }
    }

    public void stop() {
        close = true;
        try {
            if (server) {
                serverSocket.close();
            }
        } catch (Exception ex) {
            System.out.println("WARNING: Exception while trying to stop server socket.");
        }

        try {
            socket.close();
        } catch (Exception ex) {
            System.out.println("WARNING: Exception while trying to stop socket.");
        }
    }

    public void run() {
        if (server) {
            try {
                socket = serverSocket.accept();
            } catch (SocketException ex) {
                if (close) {
                    System.out.println("Server stopped correctly.");
                } else {
                    System.out.println("Error while accepting the connection");
                    ex.printStackTrace();
                    System.out.println("Thread stopped.");
                }
                return;
            } catch (Exception ex) {
                System.out.println("Error while accepting the connection");
                ex.printStackTrace();
                System.out.println("Thread stopped.");
                return;
            }
        }

        String address = null;


        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            address = socket.getInetAddress().getHostAddress();
            textArea.append("Attempting to connect to: " + address + "\n");
        } catch (Exception ex) {
            System.out.println("Error getting streams");
            ex.printStackTrace();
            return;
        }

        try {

            if(step == 1) {
                if (server) {
                    Random rand = new Random();
                    //xa = rand.nextInt(q);
                    long leftLimit = 0;
                    xa = leftLimit + (long) (Math.random() * (q - leftLimit));
                    //textArea.append("\nXa = " + xa);
                    long ya = FastExp(a, xa, q);
                    //textArea.append("\nYa = " + ya);

                    step = 2;
                    sendMessage("step="+step);
                    //textArea.append("STEP 1 DONE");

                    sendMessage("ya=" + ya);
                    textArea.append("SENDING Ya to client...\n");
                }
            }



        } catch(Exception ex) {

        }

        while (true) {
            try {
                byte[] data = new byte[256];
                inputStream.read(data);

                String message;
                /*if (encrypted) {
                    message = DataStructure.getMessage(DataStructure.decryptData(data, key));
                } else {
                    message = DataStructure.getMessage(data);
                }*/
                message = DataStructure.getMessage(data);

                if (message.startsWith("step=")) {
                    step = Integer.parseInt(message.substring(5));
                }

                if(step == 2) {
                    if (!server) {
                        //textArea.append("\nSTARTING STEP 2");
                        if (message.startsWith("ya=")) {
                            ya = Long.parseLong(message.substring(3));
                            //textArea.append("\nreceived ya = " + ya);
                            textArea.append("RECEIVED Ya from server...\n");

                            Random rand = new Random();
                            //xb = rand.nextInt(q);
                            long leftLimit = 0;
                            xb = leftLimit + (long) (Math.random() * (q - leftLimit));


                        //textArea.append("\nXb = " + xb);
                            long yb = FastExp(a, xb, q);
                            //textArea.append("\nYb = " + ya);

                            step = 3;
                            sendMessage("step="+step);

                            sendMessage("yb=" + yb);
                            textArea.append("SENDING Yb to server...\n");

                        }
                        //textArea.append("STEP 2 DONE");
                    }
                }

                if(step == 3) {
                    if (server) {
                        //textArea.append("\nSTARTING STEP 3\n");
                        if (message.startsWith("yb=")) {
                            String yb = message.substring(3);
                            //textArea.append("\nreceived yb = " + yb);
                            textArea.append("RECEIVING Yb from client...\n");
                            //calculate server's k
                            ka = FastExp(Long.parseLong(yb), xa, q);

                            step = 4;
                            sendMessage("step="+step);


                        }
                        //textArea.append("STEP 3 DONE");
                    }
                }

                if(step == 4) {
                    if (!server) {
                        //textArea.append("\nSTARTING STEP 4\n");
                        //calculate client's k
                        kb = FastExp(ya, xb, q);

                        setKLength();

                        step = 5;
                        sendMessage("step="+step);


                        sendMessage("kb="+kb);

                        textArea.append("AUTHENTICATING key with server...\n");

                        //textArea.append("STEP 4 DONE");
                    }
                }


                if(step == 5) {
                    if(server) {
                        //textArea.append("\nSTARTING STEP 5\n");
                        if (message.startsWith("kb=")) {
                            kb = Long.parseLong(message.substring(3));
                            if (ka == kb) {
                                //textArea.append("\nKEYS MATCH\n");
                                step = 6;
                                setKLength();
                                sendMessage("step="+step);
                                authenticated = true;
                                //textArea.append("STEP 4 DONE");
                                textArea.append("SUCCESS Connection established with client: "+address+"\n");
                            }
                        }
                    }
                }

                if(step == 6) {
                    if(!server) {
                        authenticated = true;
                        textArea.append("SUCCESS Connection established with server: "+address+"\n");
                    }
                    step=7;
                }

                if(authenticated && !message.startsWith("step=") && !message.startsWith("kb=")) {
                    message = DataStructure.getMessage(DataStructure.decryptData(data, K));//aqui
                    textArea.append(address + ": " + message + "\n");
                }
            } catch (SocketException ex) {
                if (close) {
                    System.out.println("Socket stopped correctly.");
                } else {
                    System.out.println("Error while receiving data.");
                    System.out.println("Thread stopped.");
                }
                return;
            } catch (Exception ex) {
                System.out.println("Error while receiving data.");
                System.out.println("Thread stopped.");
                return;
            }
        }
    }

    public static long FastExp(long b, long exp, long q)
    {
        if (exp == 0)
        {
            return 1;
        }
        else
        {
            if (exp % 2 == 0)
            {
                return FastExp(b * b % q, exp / 2, q);
            }
            else
            {
                return b * FastExp(b, exp - 1, q) % q;
            }
        }
    }

    public void setKLength(){
        K = "";
        String temp = kb+"";
        if(temp.length()>8)
            K = temp.substring(0,8);
        if(temp.length()<8){
            K = temp;
            while(K.length()<8){
                K+='0';
            }
        }
    }

}
