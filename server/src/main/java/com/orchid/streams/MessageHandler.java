package com.orchid.streams;

import com.orchid.user.UserID;

import java.nio.channels.ReadableByteChannel;

/**
 * User: Igor Petruk
 * Date: 25.12.11
 * Time: 16:31
 */
public interface MessageHandler {
    public void handleMessage(UserID userID, ReadableByteChannel byteChannel);
}
