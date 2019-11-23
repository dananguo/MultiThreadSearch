import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;

public class ServerThread implements Runnable {
    private String filePath;
    private Socket server;
    private int id;
    private String[] words;// ��ŵ��ʣ������������

    public ServerThread(Socket server,String filePath) {
        this.server = server;
        this.filePath = filePath;
    }

    /*
     * ���ֽ�����ת��Ϊ�ַ���
     *
     * @param raf �ļ�ָ��
     *
     * @param wordlength ���ȡ�ĵ��ʳ���
     *
     * @param c ��ǰ��ȡ���ֽڣ���rafָ���ǰһλ
     */
    private String byte2string(RandomAccessFile raf, int wordLength, byte c) throws IOException {
        if (raf == null)
            return null;
        byte[] b = new byte[wordLength];
        b[0] = c;
        raf.read(b, 1, wordLength - 1);// raf.read()ʹָ��ָ����һλ����ֻ��ȡwordlength-1����
        String s = new String(b);
        return s;
    }

    /*
     * �п�
     */
    private boolean isZero(byte b) {
        return b == 0 ? true : false;
    }

    /*
     * ���ֵ��в���λ��word֮�����һ������
     *
     * @param word ������ҵ��ʵ�ǰһ������
     */
    private String search(String word) {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            String len = "";
            int wordLength = 0;// ���ʳ���
            int totalLength = 0;// �ܳ���
            boolean isFound = false;
            long fileLength = raf.length();
            while (totalLength < fileLength) {
                byte hasRead = 0;// ��ǰ��ȡ����

                hasRead = raf.readByte();
                if (isZero(hasRead)) {
                    totalLength++;
                    continue;
                }
                // ��һ�����ʳ���+��ʾ���ʳ��ȵ����ִ�����
                while (hasRead >= 48 && hasRead <= 57) {
                    len = len.concat(Integer.toString(hasRead - 48));// ASCIIת����������
                    hasRead = raf.readByte();
                }
                if (len != "")
                    wordLength = Integer.parseInt(len);
                // ���ִ�+�����ܳ�
                if (isFound == false) {
                    if (word.length() == wordLength) {
                        // ��length���ȵ��ַ�
                        // �ж��Ƿ��뵥����ͬ������ͬ�������ɸ��ֽڣ���ͬ���ҵ�����
                        String s = byte2string(raf, wordLength, hasRead);
                        if (word.equals(s)) {
                            isFound = true;
                        }
                    }
                } else {
                    String s = byte2string(raf, wordLength, hasRead);
                    return s;
                }
                totalLength += wordLength + len.length();
                raf.seek(0);
                raf.skipBytes(totalLength);
                len = "";
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        System.out.println("Server[port:" + server.getInetAddress() + ",id:" + id + "]������");
        InputStream is = null;
        OutputStream os = null;
        try {
            byte[] raf = new byte[1024];
            is = server.getInputStream();
            is.read(raf);
            String str = new String(raf);
            this.words = str.split("\\s+|\n|\0");
            System.out.println("Processing Words:" + Arrays.toString(words));
            byte[] results;
            String result = "";
            for (int i = 0; i < words.length; i++) {
                if (!words[i].equals("null")) {//������!=�Ƚ��ַ�����!=�Ƚϵ�������
                    String feedback = search(words[i]);
                    System.out.println(feedback + "--" + Thread.currentThread());
                    result += feedback + "\0";
                }
            }
            results = result.getBytes();
            os = server.getOutputStream();
            os.write(results);
            System.out.println("Mission Completed");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
//				server.shutdownInput();
                if (os != null)
                    os.close();
                if (is != null)
                    is.close();
//				if (server != null)
//					server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
