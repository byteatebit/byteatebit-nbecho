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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoPerfClient extends PerfClient {

    private static final Logger LOG = LoggerFactory.getLogger(EchoPerfClient.class);

    protected ExecutorService executorService;

    public EchoPerfClient() {
        executorService = Executors.newFixedThreadPool(numClients);
    }

    public static void main(String args[]) throws IOException {
        EchoPerfClient client = new EchoPerfClient();
        client.execute();
    }

    public void execute() throws IOException {
        System.out.println("Starting execution.  Running " + numClients + " clients with " + numRequests + " per client");
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < numClients; i++) {
            executorService.submit(this::clientRequests);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("coundDownLatch wait interrupted", e);
        }
        executorService.shutdown();
        long elapsedTime = System.currentTimeMillis() - beginTime;
        printReport(elapsedTime);
    }

    protected void clientRequests() {
        try (Socket socket = new Socket("localhost", 8080)) {
            OutputStream out = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (int i = 0; i < numRequests; i++) {
                if (i % 10000 == 0)
                    System.out.println("Thread has processed: " + i);
                writer.write(UUID.randomUUID().toString() + "\n");
                writer.flush();
                reader.readLine();
            }
        } catch (IOException e) {
            LOG.error("request failed", e);
        } finally {
            countDownLatch.countDown();
        }
    }

}
