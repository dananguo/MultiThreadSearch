import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Main {

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入in文件夹绝对路径:");
		String absoluteInPath = sc.nextLine();
		String readPath = absoluteInPath + "\\sample.txt";
		String inputFilePath = absoluteInPath + "\\test.txt";
		System.out.println("请输入out文件夹绝对路径:");
		String absoluteOutPath = sc.nextLine();
		String writePath = absoluteOutPath + "\\sort.dat";
		String outputFilePath = absoluteOutPath + "\\out.log";
		
		System.out.println("请输入服务端线程数m:");
		int serverThreadCount = sc.nextInt();
		if (serverThreadCount > 5)
			serverThreadCount = 5;
		// 服务端主线程
		Thread server = new Thread(new Server(serverThreadCount, readPath, writePath));
		server.start();

		System.out.println("请输入客户端线程数m:");
		int clientThreadCount = sc.nextInt();
		if (clientThreadCount > 5)
			clientThreadCount = 5;
		sc.close();
		Vector<ClientThread> clientThreads = new Vector<>(clientThreadCount);
		// 客户端主线程
		Thread client = new Thread(new Client(clientThreads, clientThreadCount, inputFilePath, outputFilePath));
		client.start();
	}

}
