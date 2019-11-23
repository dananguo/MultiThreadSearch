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
    private String[] words;// 存放单词，接受任务分配

    public ServerThread(Socket server,String filePath) {
        this.server = server;
        this.filePath = filePath;
    }

    /*
     * 将字节数组转换为字符串
     *
     * @param raf 文件指针
     *
     * @param wordlength 需读取的单词长度
     *
     * @param c 当前读取的字节，即raf指针的前一位
     */
    private String byte2string(RandomAccessFile raf, int wordLength, byte c) throws IOException {
        if (raf == null)
            return null;
        byte[] b = new byte[wordLength];
        b[0] = c;
        raf.read(b, 1, wordLength - 1);// raf.read()使指针指向下一位，故只读取wordlength-1长度
        String s = new String(b);
        return s;
    }

    /*
     * 判空
     */
    private boolean isZero(byte b) {
        return b == 0 ? true : false;
    }

    /*
     * 在字典中查找位于word之后的下一个单词
     *
     * @param word 所需查找单词的前一个单词
     */
    private String search(String word) {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            String len = "";
            int wordLength = 0;// 单词长度
            int totalLength = 0;// 总长度
            boolean isFound = false;
            long fileLength = raf.length();
            while (totalLength < fileLength) {
                byte hasRead = 0;// 当前读取内容

                hasRead = raf.readByte();
                if (isZero(hasRead)) {
                    totalLength++;
                    continue;
                }
                // 下一个单词长度+表示单词长度的数字串长度
                while (hasRead >= 48 && hasRead <= 57) {
                    len = len.concat(Integer.toString(hasRead - 48));// ASCII转阿拉伯数字
                    hasRead = raf.readByte();
                }
                if (len != "")
                    wordLength = Integer.parseInt(len);
                // 数字串+单词总长
                if (isFound == false) {
                    if (word.length() == wordLength) {
                        // 读length长度的字符
                        // 判断是否与单词相同，不相同跳过若干个字节，相同则找到返回
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
        System.out.println("Server[port:" + server.getInetAddress() + ",id:" + id + "]已运行");
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
                if (!words[i].equals("null")) {//不能用!=比较字符串，!=比较的是引用
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
