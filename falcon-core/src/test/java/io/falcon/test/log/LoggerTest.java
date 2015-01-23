package io.falcon.test.log;

import io.falcon.annotations.POST;
import io.falcon.utils.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by xux on 15-1-21.
 */
public class LoggerTest {
    static final Logger logger = LogManager.getLogger("Falcon");

    @Test
    public void test1(){
        logger.entry();
        logger.info("hello {} {}",new Object[]{"徐翔","哈哈"});
        logger.error("Did it again!");
    }

    @Test
    public void test2(){
        Log log = Log.getLogger("Falcon");
        log.info("你好");
        log.error("你好{0}{1}", "徐翔","哈哈");
        log.debug("你好");
    }

    @Test
    public void test3(){
        SimpleDateFormat httpDate =
                new SimpleDateFormat(
                        "EEE, dd MMM yyyy HH:mm:ss z",
                        Locale.US);
        GregorianCalendar g = DateTime.now().plusYears(-1).toGregorianCalendar();
        System.out.println(httpDate.format(g.getTime()));
    }

    @Test
    public void test4(){

    }
}
