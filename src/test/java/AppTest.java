import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import org.junit.Test;

public class AppTest {


    @Test
    public void app8() {
        RedisClient redisClient = new RedisClient(
                RedisURI.create("redis://201061032@127.0.0.1:6379"));
        RedisConnection redisConnection = redisClient.connect();
        String result = redisConnection.ping();
        System.out.println();
    }



}
