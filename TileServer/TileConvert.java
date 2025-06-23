import java.awt.image.BufferedImage;

class TileConvert{
    //creating constant values
    static final int height = 256;
    static final int width = 256;
    static final int darkRed = 255<<24 | 128<<16; //fully opaque, dark red
    static final int red = 128<<24 | 255<<16; //semi-transparent, red
    
    public static BufferedImage tileConvert(String tileset, BufferedImage image){
        if(tileset.equals("cloud-area-fraction")){
            return cloudConvert(image);
        }
        if(tileset.equals("air-temperature")){
            return tempConvert(image);
        }
        if(tileset.equals("wind")){
            return windConvert(image);
        }
        String[] tilesetSplit = tileset.split("-");
        if(tilesetSplit[0].equals("precipitation")){
            return rainConvert(image);
        }
        return null;
    }

    public static BufferedImage cloudConvert(BufferedImage image){
        BufferedImage outputImage = new BufferedImage(256, 256, 2);

        for(int y = 0; y<height; y++){
            for(int x = 0; x<width; x++){
                int pixel = image.getRGB(x,y);
                int redChannel   = (pixel >> 16) & 0xff; //extracting red-channel
                int rgb = ((int)(redChannel) << 24) | 0xffffff; //opacity = (red)%, color = white
                //originally multiplied red value so that it would be % * 255, but we found 200 as max opacity to be more visually appealing
                outputImage.setRGB(x, y, rgb);
            }
        }
        return outputImage;
    }

    public static BufferedImage rainConvert(BufferedImage image){
        BufferedImage outputImage = new BufferedImage(256, 256, 2);

        for(int y = 0; y<height; y++){
            for(int x = 0; x<width; x++){
                int pixel = image.getRGB(x,y);
                int rgb;
                if(pixel==0xff000000){ //make black pixels transparent
                    rgb = 0x00000000;
                }else if(pixel==0xffffffff){ //white pixels, which indicate no data because the point is not covered by the api 
                    if((x+(y%4))%4 == 0){ //draw said no data areas with a striped red to indicate that they are not available
                        rgb = darkRed;
                    }else{
                        rgb = red;
                    }
                }else{
                    rgb = 0x80ffffff & pixel; //make the rain somewhat transparent
                }
                outputImage.setRGB(x, y, rgb);
            }
        }
        return outputImage;
    }

    public static BufferedImage tempConvert(BufferedImage image){
        BufferedImage outputImage = new BufferedImage(256, 256, 2);

        for(int y = 0; y<height; y++){
            for(int x = 0; x<width; x++){
                int color = 0;
                int red = (image.getRGB(x,y)>>16) & 0xff;
                if(red>=178){ //temp in c is given in the red component as temp + 128 so all the checks are 128 larger than the temperature
                    color = 0x780030;
                }else if(red>=168){
                    color = 0xB21002;
                }else if(red>=158){
                    color = 0xFF5D56;
                }else if(red>=148){
                    color = 0xFFB37A;
                }else if(red>=138){
                    color = 0xFFF36E;
                }else if(red>=128){
                    color = 0xD0F5D9;
                }else if(red>=118){
                    color = 0x85E4ED;
                }else if(red>=108){
                    color = 0x6AC6EE;
                }else if(red>=98){
                    color = 0x649BE2;
                }else if(red>=88){
                    color = 0x114F9D;
                }else{
                    color = 0x481581;
                }
                outputImage.setRGB(x, y, (color | 128 << 24));
            }
        }
        return outputImage;
    }

    public static BufferedImage windConvert(BufferedImage image){
        BufferedImage outputImage = new BufferedImage(256, 256, 2);
        for( int y = 0; y<height; y++){
            for( int x = 0; x<width; x++){
                int pixel = image.getRGB(x,y);
                float windX = (((pixel >> 16) & 0xff)-128)/2.0f; //translate red component to windX-component
                float windY = (((pixel >> 8 ) & 0xff)-128)/2.0f; //translate green component to windY-component
                float hypotenuse = (float) Math.sqrt((windX*windX)+(windY*windY)); //pythagorean theorem to get the true speed of the wind from the x and y speeds
                int color = 0xA7CEA1;
                if(hypotenuse>32.6){
                    color = 0x310047;
                }else if(hypotenuse>28.5){
                    color = 0x4D0A6C;
                }else if(hypotenuse>24.5){
                    color = 0x5B278D;
                }else if(hypotenuse>20.8){
                    color = 0x7043A8;
                }else if(hypotenuse>17.2){
                    color = 0x7B57ED;
                }else if(hypotenuse>13.9){
                    color = 0x4B87EA;
                }else if(hypotenuse>10.8){
                    color = 0x13A8D6;
                }else if(hypotenuse>8){
                    color = 0x3CBEBE;
                }else if(hypotenuse>5.5){
                    color = 0x79CCAC;
                }else{
                    color = 0xA7CEA1;
                }
                outputImage.setRGB(x, y, (color | 128 << 24)); //50% opacity
            }  
        }
        return outputImage;
    }
}
