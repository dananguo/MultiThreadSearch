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
        this.client = new Socket("localhost", CLIENT_PORT);//�贴���µ�Socket���������߳̿�����ֵ������ͬһ��socket
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
     * ����Ҫ��ѯ�ĵ��ʷ��͸������
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
     * ���շ������Ĳ�ѯ���,����List
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
            System.out.println("Client[port:" + client.getInetAddress() + ",id:" + id + "]�ɹ����ӷ����");
            sendWords();
            getBack();
            if (client != null)
                client.close();
            assert client != null;
            System.out.println("Client[port:" + client.getInetAddress() + ",id:" + id + "]���˳�");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
