import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private static final int CLIENT_PORT = 8899;
    private Socket client;
    private int id;
    private String[] list;
    private byte[] resultWords;

    public byte[] getByte() {
        return resultWords;
    }

    public ClientThread(int id, String[] list) throws IOException {
        this.client = new Socket("localhost", CLIENT_PORT);//需创建新的Socket，若在主线程拷贝赋值，则是同一个socket
        this.id = id;
        this.list = list;
        this.resultWords = new byte[1024];
    }

    private byte[] str2byte() {
        String consult = "";
        for (String s : list) {
            consult += s + "\0";
        }
        return consult.getBytes();
    }

    /*
     * 将需要查询的单词发送给服务端
     */
    private void sendWords() {
        try {
            OutputStream os = client.getOutputStream();
            byte[] binaryWords = str2byte();
            os.write(binaryWords);
            System.out.println("send");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 接收服务器的查询结果,放入List
     */
    private void getBack() {
        try {
            InputStream is = client.getInputStream();
            is.read(resultWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Client[port:" + client.getInetAddress() + ",id:" + id + "]成功连接服务端");
            sendWords();
            getBack();
            if (client != null)
                client.close();
            assert client != null;
            System.out.println("Client[port:" + client.getInetAddress() + ",id:" + id + "]已退出");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
