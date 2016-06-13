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

package com.byteatebit.nbecho.perfclient;

import com.byteatebit.nbserver.IClientSocketChannelHandler;
import com.byteatebit.nbserver.INbContext;
import com.byteatebit.nbserver.NbServiceConfig;
import com.byteatebit.nbserver.simple.client.SimpleNbClient;
import com.byteatebit.nbserver.task.ReadDelimitedMessageTask;
import com.byteatebit.nbserver.task.WriteMessageTask;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class NbEchoPerfClient extends PerfClient implements IClientSocketChannelHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NbEchoPerfClient.class);

    public static final String MESSAGE_DELIMITER    = "\r?\n";

    protected final SimpleNbClient simpleNbClient;
    protected final long connectTimeoutMs;
    protected int taskId;

    public NbEchoPerfClient() {
        super();
        this.simpleNbClient = SimpleNbClient.Builder.builder()
                .withNbServiceConfig(NbServiceConfig.Builder.builder()
                        .withNumThreads(10)
                        .withNumIoThreads(2)
                        .withSelectorTimeoutMs(1l)
                        .build())
                .build();
        this.connectTimeoutMs = 5000;
        this.taskId = 0;
    }

    public void execute() throws IOException {
        System.out.println("Starting execution.  Running " + numClients + " clients with " + numRequests + " per client");
        long beginTime = System.currentTimeMillis();
        try (SimpleNbClient client = simpleNbClient.init()) {
            for (int i = 0; i < numClients; i++) {
                    try {
                        simpleNbClient.tcpConnect(host, port, this, connectTimeoutMs);
                    } catch (IOException e) {
                        LOG.error("connect failed", e);
                    }
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOG.error("coundDownLatch wait interrupted", e);
            }
        }
        long elapsedTime = System.currentTimeMillis() - beginTime;
        printReport(elapsedTime);
    }

    public static void main(String args[]) throws IOException {
        new NbEchoPerfClient().execute();
    }

    public synchronized int createTaskId() {
        return taskId++;
    }

    @Override
    public void accept(INbContext nbContext, SocketChannel socketChannel) {
        boolean connectionFinished = false;
        int taskId = createTaskId();
        try {
            connectionFinished = socketChannel.finishConnect();
        } catch (IOException e) {
            LOG.error("Could not complete socket connection.", e);
        }
        if (!connectionFinished) {
            LOG.error("Could not complete socket connection.  Closing socket channel");
            IOUtils.closeQuietly(socketChannel);
            countDownLatch.countDown();
            return;
        }
        WriteMessageTask writeMessageTask = WriteMessageTask.Builder.builder()
                .withByteBuffer(ByteBuffer.allocate(1024))
                .build();
        ReadDelimitedMessageTask readMessageTask = ReadDelimitedMessageTask.Builder.builder()
                .withByteBuffer(ByteBuffer.allocate(1024))
                .withDelimiter(MESSAGE_DELIMITER)
                .build();
        clientTask(nbContext, socketChannel, readMessageTask, writeMessageTask, numRequests, taskId);
    }

    protected void clientTask(final INbContext nbContext,
                              final SocketChannel socketChannel,
                              final ReadDelimitedMessageTask readMessageTask,
                              final WriteMessageTask writeMessageTask,
                              int count,
                              int taskId) {
        if (count % 10000 == 0)
            System.out.println("Task " + taskId + " has processed: " + (numRequests - count));
        if (count <= 0) {
            IOUtils.closeQuietly(socketChannel);
            countDownLatch.countDown();
            System.out.println("Count Down Latch count: " + countDownLatch.getCount());
            return;
        }
        Consumer<Exception> exceptionHandler = (e) -> {
            LOG.error("Exception during client execution.  Closing socket channel", e);
            countDownLatch.countDown();
            IOUtils.closeQuietly(socketChannel);
        };
        Consumer<List<String>> readCallback = (messages) -> {
            LOG.info("Messages received " + messages);
            clientTask(nbContext, socketChannel, readMessageTask, writeMessageTask, count - 1, taskId);
        };
        Runnable writeCallback = () -> readMessageTask.readMessages(nbContext, socketChannel,
                readCallback, exceptionHandler);
        writeMessageTask.writeMessage((UUID.randomUUID().toString() + "\n").getBytes(StandardCharsets.UTF_8), nbContext,
                socketChannel, writeCallback, exceptionHandler);
    }

    @Override
    public void connectFailed(String host, int port) throws IOException {
        LOG.error("Connection to " + host + ":" + port + " failed");
        countDownLatch.countDown();
    }
}


