import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
//import java.io.File;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URI;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ServerHttpHandler implements HttpHandler{
    //private byte[]obImageData;


    /* The following code was meant preload and out-of-bounds image to replace 404-errors. 
    Changes to the met-api made this approach no longer work 
    
    ServerHttpHandler(String filename) throws IOException{
        super();
        try{
            File obTile = new File(filename);
            BufferedImage obImage = ImageIO.read(obTile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(obImage, "png", baos);
            obImageData = baos.toByteArray();
        }catch(IOException e){
            System.out.println("obTile.png må opprettes ved å kjøre: 'java RedTile obTile.png'");
        }
    }
    */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String reqUri = httpExchange.getRequestURI().toString(); 
        String[] parts = reqUri.split("/api-point/");
        OutputStream os = httpExchange.getResponseBody();

        String imageUrl = parts[1];
        System.out.println(imageUrl);
        String[] metUri = parts[1].split("/");
        String tileset = metUri[4];
        System.out.println(tileset);
                  
        try{
            // URL(String) constructor was deprecated, so uri then url.
            URI uri = new URI(imageUrl);
            URL url = uri.toURL();
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "IN2000-Team-28 davidhov@uio.no");
             
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException();
            }
            InputStream inputStream = connection.getInputStream();
            BufferedImage image = ImageIO.read(inputStream); // Read image
            inputStream.close(); // Close the input-stream
            connection.disconnect(); // Disconnect the connection
            image = TileConvert.tileConvert(tileset, image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageData = baos.toByteArray();
            httpExchange.sendResponseHeaders(200, imageData.length);
            os.write(imageData);
            os.close();
            
        }catch(Exception e){
            String error = "Something went wrong with fetching, converting or writing a tile";
            System.out.println(error);
            httpExchange.sendResponseHeaders(200, error.length());
            os.write(error.getBytes());
            os.close();
        }
    }
}