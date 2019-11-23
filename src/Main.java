import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Main {

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("������in�ļ��о���·��:");
		String absoluteInPath = sc.nextLine();
		String readPath = absoluteInPath + "\\sample.txt";
		String inputFilePath = absoluteInPath + "\\test.txt";
		System.out.println("������out�ļ��о���·��:");
		String absoluteOutPath = sc.nextLine();
		String writePath = absoluteOutPath + "\\sort.dat";
		String outputFilePath = absoluteOutPath + "\\out.log";
		
		System.out.println("�����������߳���m:");
		int serverThreadCount = sc.nextInt();
		if (serverThreadCount > 5)
			serverThreadCount = 5;
		// ��������߳�
		Thread server = new Thread(new Server(serverThreadCount, readPath, writePath));
		server.start();

		System.out.println("������ͻ����߳���m:");
		int clientThreadCount = sc.nextInt();
		if (clientThreadCount > 5)
			clientThreadCount = 5;
		sc.close();
		Vector<ClientThread> clientThreads = new Vector<>(clientThreadCount);
		// �ͻ������߳�
		Thread client = new Thread(new Client(clientThreads, clientThreadCount, inputFilePath, outputFilePath));
		client.start();
	}

}
