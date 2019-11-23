import java.io.*;
import java.util.Vector;

public class Client implements Runnable{
    private String[][] lists;
    private String[][] results;//��Ž��
    private Vector<ClientThread> clientThreads;//����߳�
    private int clientThreadCount;
    private int wordsCount = 0;
    private int maxRounds = 0;
    private String inputFilePath;
    private String outputFilePath;

    public Client(Vector<ClientThread> clientThreads, int clientThreadCount, String inputFilePath, String outputFilePath) {
        this.clientThreads = clientThreads;
        this.clientThreadCount = clientThreadCount;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }

    private void setLists() {
        this.lists = new String[clientThreadCount][maxRounds];
        this.results = new String[clientThreadCount][maxRounds];
    }

    private void setMaxRounds() throws IOException {
        File file = new File(inputFilePath);
        FileReader fr = new FileReader(file);
        LineNumberReader lnr = new LineNumberReader(fr);
        lnr.skip(Long.MAX_VALUE);
        wordsCount = lnr.getLineNumber() + 1;//��ȡtest.txt�е��ʸ���
        this.maxRounds = (int) Math.ceil((double) wordsCount / clientThreadCount);
        System.out.println("test.txt���е�������" + wordsCount);
        System.out.println("���������ά�����еĶ�ά�����ǣ�" + maxRounds);
        lnr.close();
        //��ָ̬����ά�����еĶ�ά����
        if (this.maxRounds > 0)
            setLists();
    }

    /*
     * ��ȡ��Ҫ��ѯ�ĵ���
     */
    public void getWords() {
        try {
            //���ö�ά����Ķ�ά����
            setMaxRounds();

            File file = new File(inputFilePath);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String strs = "";
            int pos = 0;
            int round = 0;
            while ((strs = br.readLine()) != null) {
                lists[pos][round] = strs;
                pos = (pos + 1) % clientThreadCount;
                if (pos == 0)
                    round++;
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getResults() {
        int pos = 0;
        while (pos < clientThreadCount) {
            byte[] result = clientThreads.get(pos).getByte();
            results[pos] = byte2string(result);
            pos++;
        }
    }

    private String[] byte2string(byte[] bytes) {
        String string = new String(bytes);
        String[] result = string.split("\\s+|\n|\0");
        return result;
    }

    /*
     * ���շ������Ĳ�ѯ��� �����ѯ�������־�ļ�
     */
    public void outputLog() {
        try {
            //�ȴ������߳�ִ�����
            for (Thread thread : clientThreads) {
                thread.join();
            }
            //��ȡ��ѯ���
            getResults();

            FileWriter logFile = new FileWriter(outputFilePath);
            BufferedWriter bw = new BufferedWriter(logFile);
            String strs = "";
            int round = 0;
            int total = 0;
            while (round < maxRounds) {
                for (int pos = 0; pos < clientThreadCount; pos++) {
                    if (total < wordsCount) {
                        strs = results[pos][round];
                        bw.write(strs + "\n");
                        bw.flush();
                        total++;
                    } else break;
                }
                round++;
            }
            logFile.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void request() throws IOException {
        int i = 0;
        while (i < clientThreadCount) {
            ClientThread clientThread = new ClientThread(i, lists[i]);
            clientThread.start();
            //��������飬����ȡ��
            clientThreads.add(clientThread);
            i++;
            System.out.println("������" + i + "���ͻ����߳�");
        }
    }

    public void append(String filePath, long searchTime) {
        try {
            RandomAccessFile randomFile = new RandomAccessFile(filePath, "rw");
            String searTime = searchTime + "\n";
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(searTime.getBytes());
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            getWords();
            request();
            outputLog();
            long end = System.currentTimeMillis();
            long searchTime = end - start;
            append(outputFilePath, searchTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
