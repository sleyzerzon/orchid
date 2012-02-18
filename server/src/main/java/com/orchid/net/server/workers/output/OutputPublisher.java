package com.orchid.net.server.workers.output;

import com.lmax.disruptor.EventTranslator;
import com.orchid.logic.ring.RingElement;
import com.orchid.logic.user.UserID;

/**
 * User: Igor Petruk
 * Date: 26.01.12
 * Time: 23:57
 */
public class OutputPublisher implements EventTranslator<RingElement>{
    RingElement message;

    public void send(RingElement message, UserID... recepients){
        this.message = message;
        for (UserID userID: recepients){
            OutputWorker outputWorker = userID.getOutputWorker();
            outputWorker.getDisruptor().publishEvent(this);
        }
        this.message = null;
    }

    @Override
    public RingElement translateTo(RingElement event, long sequence) {
        event.message = message.message;
        event.userID = message.userID;
        return event;
    }
}
