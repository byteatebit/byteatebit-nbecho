/*
 * Copyright (c) 2016 byteatebit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byteatebit.nbecho;

import com.byteatebit.common.util.CollectionsUtil;
import com.byteatebit.nbserver.INbContext;
import com.byteatebit.nbserver.SocketChannelTask;
import com.byteatebit.nbserver.task.ReadDelimitedMessageTask;
import com.byteatebit.nbserver.task.WriteMessageTask;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class NbEchoService extends SocketChannelTask {

    private static final Logger LOG = LoggerFactory.getLogger(NbEchoService.class);

    public static final String QUIT_CMD             = "quit";
    public static final String SLEEP_CMD            = "sleep";
    public static final String MESSAGE_DELIMITER    = "\r?\n";

    protected final ReadDelimitedMessageTask readDelimitedMessageTask;
    protected final WriteMessageTask writeMessageTask;


    public NbEchoService(INbContext nbContext, SocketChannel socket) {
        super(nbContext, socket);
        this.readDelimitedMessageTask = ReadDelimitedMessageTask.Builder.builder()
                .withDelimiter(MESSAGE_DELIMITER)
                .withByteBuffer(ByteBuffer.allocate(2048))
                .build();
        this.writeMessageTask = WriteMessageTask.Builder.builder()
                .withByteBuffer(ByteBuffer.allocate(2048))
                .build();
    }

    public void start() {
        readMessage();
    }


    protected void readMessage() {
        readDelimitedMessageTask.readMessages(this::register, messages -> echo(messages), this::handleException);
    }

    protected void echo(List<String> commands) {
        if (commands.isEmpty()) {
            readMessage();
            return;
        }
        echo(commands.get(0), () -> echo(CollectionsUtil.cdr(commands)));
    }

    protected void echo(String command, Runnable callback) {
        LOG.info("EchoService recieved command '" + command + "'");
        if (QUIT_CMD.equals(command.toLowerCase())) {
            LOG.info("EchoService shutting down due to receipt of "
                    + QUIT_CMD + " command");
            IOUtils.closeQuietly(socketChannel);
        } else if (SLEEP_CMD.equals(command.toLowerCase())) {
            try {
                LOG.info("Sleeping for 5 seconds");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error("sleep interrupted", e);
            }
            writeMessageTask.writeMessage(
                    "Slept for 5 seconds\n".getBytes(StandardCharsets.UTF_8),
                    this::register, callback, this::handleException);
        } else
            writeMessageTask.writeMessage(
                    (command + "\n").getBytes(StandardCharsets.UTF_8),
                    this::register, callback, this::handleException);
    }

    protected void handleException(Exception exception) {
        if (exception instanceof EOFException)
            LOG.warn("End of stream.  Closing socketChannel channel");
        else
            LOG.error("Exception handler invoked.  Closing socketChannel channel", exception);
        IOUtils.closeQuietly(socketChannel);
    }

}
