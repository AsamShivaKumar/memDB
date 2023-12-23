package persistance;

import java.io.BufferedWriter;
import java.nio.file.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Aof {
    private Path filePath;
    private BufferedWriter writer;
    private Object lock;

    public Aof(String filePath){
        
        this.filePath = Paths.get(filePath);

        try{
            Files.createDirectories(this.filePath.getParent());
        }catch(IOException exp){
            System.out.println("IOExpception while creating directories for AOF file. Stack trace - ");
            exp.printStackTrace();
        }

        lock = new Object();

    }

    private void syncToFile(){
        synchronized(lock) {
            try {
                if(writer != null) writer.flush();
            } catch (IOException exp) {
                System.out.println("IOExpception while flushing to file. Stack trace - ");
                exp.printStackTrace();
            }
        }
    }

    public void open() throws IOException {

        synchronized(lock){
            if(writer == null){
                writer = new BufferedWriter(new FileWriter(filePath.toString(), true));
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(this::syncToFile, 0, 1, TimeUnit.SECONDS);
            }
        }
    }

    public void append(String data) throws IOException {

        synchronized(lock) {
            writer.append(data);
        }
    }

    public void close() throws IOException {

        synchronized(lock) {
            if(writer != null)  writer.close();
        }

    }

}
