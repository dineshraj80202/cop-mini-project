import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class HttpTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("HttpServer class available and created on port " + server.getAddress().getPort());
        server.stop(0);
    }
}