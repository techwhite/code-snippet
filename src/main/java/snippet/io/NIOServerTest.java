package snippet.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

// 加载spring配置文件
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-mybatis.xml","classpath:spring-mvc.xml"})
public class NIOServerTest {
    @Autowired
    private NIOServer nioServer;

    @Test
    public void testNIOStartServer() throws IOException {
        NIOServer server = new NIOServer();
        server.initServer(8000);
        server.listen();
    }

}

/**

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Reactor%E6%A8%A1%E5%BC%8F.jpg)

 */