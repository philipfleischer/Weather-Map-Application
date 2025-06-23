import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;


public class TileServer{
    public static void main(String[]args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);  //"localhost" / InetAddress.getByName("<ip-address>")
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);   //number of clients the server could handle in parallel

        server.createContext("/api-point/", new ServerHttpHandler());
        server.setExecutor(threadPoolExecutor);
        server.start();
        System.out.println("Server started on address: "+server.getAddress().getHostString()+", port: "+server.getAddress().getPort());
    }
}

        