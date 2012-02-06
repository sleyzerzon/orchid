package com.orchid.net.server.workers.input;

import com.orchid.net.server.connections.Connection;
import com.orchid.net.server.exceptions.NetworkException;
import com.orchid.net.server.workers.Worker;
import com.orchid.streams.BufferAggerator;
import com.orchid.streams.BufferPool;
import com.orchid.streams.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: Igor Petruk
 * Date: 25.12.11
 * Time: 9:42
 */
public class InputWorker extends Worker implements Runnable{
    @Inject
    BufferPool bufferPool;

    @Inject
    MessageHandler messageHandler;

    @Inject
    private Logger logger;

    Thread thread;

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    public void handleConnection(Connection connection){
        try{
            BufferAggerator bufferAggerator = new BufferAggerator(bufferPool,messageHandler);
            connection.setBufferAggerator(bufferAggerator);
            synchronized (this){
                selector.wakeup();
                SelectionKey readingKey = connection.getSocketChannel().register(selector,
                        SelectionKey.OP_READ,
                        connection);
            }
            logger.debug("Handling connection {} by {}", connection.getSocketChannel(), this);
        }catch(IOException e){
            logger.error("Error handling connection {}",e);
        }

        connectionsCount.incrementAndGet();
    }

    @Override
    public void run() {
        MDC.put("subsystem",workerName);
        logger.info("InputWorker started");
        while(true){
            try{
                selector.select();

                // Mad skillz
                synchronized (this){}

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> i = keys.iterator();
                while (i.hasNext()) {
                    SelectionKey key = (SelectionKey) i.next();
                    if (key.isReadable()) {
                        handleRead(key);
                        continue;
                    }
                }
            }catch(Exception e){
                 if(Thread.interrupted()){
                     break;
                 }else{
                    if (logger.isWarnEnabled()){
                        logger.warn("InputWorker error {}",
                            new NetworkException("InputWorker error", e));
                    }
                 }
             }
        }
        logger.info("InputWorker {} ended", this);
    }

    private void handleRead(SelectionKey key) throws IOException{
        //logger.info("Reading "+this);
        SocketChannel socketChannel = (SocketChannel)key.channel();
        Connection connection = (Connection)key.attachment();

        boolean running = connection.getBufferAggerator().
                readSome(connection.getUserID(), socketChannel);
        if (!running){
            handleDisconnect(key);
        }
    }
}
