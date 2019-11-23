import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable{
    private static final int SERVER_PORT = 8899;
    private static final int ARRAY_LENGTH = 1024;
    private ServerSocket serverSocket;
    private ExecutorService serverPoolExecutorService;
    private byte[] all = new byte[ARRAY_LENGTH];
    private String[] words;
    private String readPath;
    private String writePath;

    public Server(int serverThreadCount, String readPath, String writePath) throws IOException {
        this.readPath = readPath;
        this.writePath = writePath;
        serverSocket = new ServerSocket(SERVER_PORT);
        serverPoolExecutorService = Executors.newFixedThreadPool(serverThreadCount);
        System.out.println("等待连接...");
    }

    public void generateDic(String readPath, String writePath) {
        String str;
        try {
            str = readFile(readPath);
            if (splitFile(str)) {
                sort();
                writeFile(writePath);
            } else
                throw new RuntimeException("字符串解析失败");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int stringToBytes(int curLength, int pos) {
        byte[] b = null;
        int totalLength = 0;
        int wordLength = words[pos].length();
        String wordLen = String.valueOf(wordLength);
        String word = wordLen + words[pos];
        b = word.getBytes();
        totalLength = word.length();
        if (curLength + totalLength <= ARRAY_LENGTH) {
            System.arraycopy(b, 0, all, curLength, totalLength);
        } else {
            return 0;
        }
        return totalLength;
    }

    private void writeFile(String filePath) {
        if (words == null)
            return;
        if (words[0].length() > ARRAY_LENGTH)
            return;

        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            boolean outOfRange = false;
            boolean hasRemainder = false;
            boolean isFull = false;
            int length = 0;
            int i = 0, len = words.length;
            while (i < len) {
                int wordLength = stringToBytes(length, i);
                length += wordLength;

                if (wordLength == 0) {
                    outOfRange = true;
                    hasRemainder = true;
                    isFull = true;
                } else if (length == ARRAY_LENGTH && i < len - 1) {
                    hasRemainder = true;
                    isFull = true;
                } else if (length == ARRAY_LENGTH && i == len - 1) {
                    hasRemainder = false;
                    isFull = true;
                } else
                    hasRemainder = true;

                if (isFull) {
                    fos.write(all);
                    isFull = false;
                    if (hasRemainder) {
                        all = new byte[1024];
                        length = 0;
                        hasRemainder = false;
                    }
                }
                if (outOfRange) {
                    i--;
                    outOfRange = false;
                }
                i++;
            }
            if (hasRemainder)
                fos.write(all);

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sort() {
        if (words == null)
            return;
        int pos = 0;
        for (String each : words) {
            if (97 <= each.charAt(0) && each.charAt(0) <= 122)
                pos++;
            else
                break;
        }
        // 小写单词排序
        for (int i = 0; i < pos / 2; i++) {
            String temp = words[i];
            words[i] = words[pos - i - 1];
            words[pos - i - 1] = temp;
        }
        // 大写单词排序
        for (int i = pos, len = words.length; i < (len + pos) / 2; i++) {
            String temp = words[i];
            words[i] = words[pos + len - 1 - i];
            words[pos + len - i - 1] = temp;
        }
    }

    private boolean splitFile(String src) {
        if (src == null)
            return false;
        String regs = "[a-zA-Z]+[\\']?[a-zA-Z]*";//TODO:R.I.P
        Pattern exp = Pattern.compile(regs);
        Matcher matcher = exp.matcher(src);
        TreeSet<String> set = new TreeSet<String>(new MyComparator());
        String word;
        while (matcher.find()) {
            word = matcher.group();
            set.add(word);
        }
        words = set.toArray(new String[set.size()]);
        if (words != null)
            return true;
        else
            return false;
    }

    private String readFile(String filePath) throws Exception {
        File file = new File(filePath);
        FileReader fis = new FileReader(file);
        BufferedReader br = new BufferedReader(fis);
        String strs = "";
        StringBuffer sbf = new StringBuffer("");
        while ((strs = br.readLine()) != null) {
            sbf.append(strs);
        }

        br.close();
        fis.close();
        return sbf.toString();
    }


    public void service(String filePath) {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                serverPoolExecutorService.execute(new ServerThread(socket, filePath));
                System.out.println("已启动" + Thread.currentThread() + "服务端线程");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        generateDic(readPath, writePath);
        service(writePath);
    }
}
