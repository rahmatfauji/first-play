package helpers;

import com.typesafe.config.ConfigFactory;


public class KeyCorrector {
    private static final String secret_api = ConfigFactory.load().getString("play.http.secret.api_key");

    public static Boolean check_api_key(String key){
        System.out.println(key);
        System.out.println(secret_api);
        try{
            if(key.equals(secret_api)){
                return true;
            }else{
                return false;
            }


        }catch (Exception e){
            return false;
        }
    }
}
