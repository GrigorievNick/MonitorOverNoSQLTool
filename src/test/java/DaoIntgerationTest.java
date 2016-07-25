import java.util.concurrent.TimeUnit;
import org.mhr.monitor.Main;
import org.mhr.monitor.dao.ILiveStreamDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest
public class DaoIntgerationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ILiveStreamDao client;

    @Test
    public void test() throws InterruptedException {
        client.find().replay(4, TimeUnit.SECONDS).refCount().subscribe(System.out::println);
        Thread.sleep(10_000);
    }
}
