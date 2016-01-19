import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Read http://stackoverflow.com/questions/817856/when-and-how-should-i-use-a-threadlocal-variable/34042362#34042362
 */
public class ThreadLocalTest {

    public static void main(String args[]) throws IOException {
        Thread firstThread = new Thread(new Job());   
        Thread secondThread = new Thread( new Job());
        firstThread.start();
        secondThread.start();       
    }
    
    public static DateFormat getThreadSafeDateFormater(){
        DateFormat formatter = SimpleDateFormatInstancePerThread.getDateFormatter();
        return formatter;
    }
    
    /*
     * SimpleDateFormat is the mutable class and shared resource which has to be synchronized
     * while performing format method call.   
     * So better give new instance of SimpleDateFormat for each thread, so that 
     * format method will be called on only one thread which owns SimpleDateFormat instance. 
     */
    public static String threadSafeFormat(DateFormat formatter, Date date){
        return formatter.format(date);
    }
    
    
    public static void cleanUpThreadSafeDateFormater(){
        SimpleDateFormatInstancePerThread.cleanup();
    }
    
}


/*
 * Thread Safe implementation of SimpleDateFormat
 * Each Thread will get its own instance of SimpleDateFormat.
 */
class SimpleDateFormatInstancePerThread {

    private static final ThreadLocal<SimpleDateFormat> dateFormatHolder = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") {
				UUID id = UUID.randomUUID();
				@Override
				public String toString() {
					return id.toString();
				};
			};
            System.out.println("Creating SimpleDateFormat instance " + dateFormat +" for Thread : " + Thread.currentThread().getName());
            return dateFormat;
        }
    };

    /*
     * Every time there is a call for DateFormat, ThreadLocal will return calling
     * Thread's copy of SimpleDateFormat
     */
    public static DateFormat getDateFormatter() {
        return dateFormatHolder.get();
    }
    
    public static void cleanup() {
        dateFormatHolder.remove();
    }
}

class Job implements java.lang.Runnable{
    
    @Override
    public void run() {
            System.out.println("Thread Name: " + Thread.currentThread().getName() + "  Formatted time: " + ThreadLocalTest.threadSafeFormat(ThreadLocalTest.getThreadSafeDateFormater(), new Date()));
            System.out.println("Thread Name: " + Thread.currentThread().getName() + "  Date Formatter Object: " + ThreadLocalTest.getThreadSafeDateFormater() + " is cleaned.");
            ThreadLocalTest.cleanUpThreadSafeDateFormater();
            ThreadLocalTest.getThreadSafeDateFormater();
    }
}

