package com.orchid.ring;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.orchid.logic.annotations.BusinessLogic;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: Igor Petruk
 * Date: 01.01.12
 * Time: 16:47
 */
public class InputRingConfiguration {
    Disruptor<RingElement> disruptor;
    RingBuffer<RingElement> ringBuffer;
    
    @Inject
    public InputRingConfiguration(@BusinessLogic List<EventHandler<RingElement>> businessLogic) {
        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
            int threadID;
            
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                String name="BussinessLogicThread-"+(threadID++);
                System.out.println("Starting "+name);
                thread.setName(name);
                return thread;
            }
        });
        disruptor = new Disruptor<RingElement>(RingElement.EVENT_FACTORY, executor,
           //                new MultiThreadedLowContentionClaimStrategy(1024),
                            new MultiThreadedClaimStrategy(1024),
                            new BlockingWaitStrategy());
        System.out.println(Arrays.toString(businessLogic.toArray()));
        if (businessLogic.size()>0){
            EventHandlerGroup<RingElement> eventHandlerGroup =
                    disruptor.handleEventsWith(businessLogic.get(0));
            for (int i = 1; i < businessLogic.size(); i++){
                eventHandlerGroup = eventHandlerGroup.then(businessLogic.get(i));
            }
        }
        ringBuffer = disruptor.start();
    }

    public Disruptor<RingElement> getDisruptor() {
        return disruptor;
    }

    public RingBuffer<RingElement> getRingBuffer() {
        return ringBuffer;
    }
}
