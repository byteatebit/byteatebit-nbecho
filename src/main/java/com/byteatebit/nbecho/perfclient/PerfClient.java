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

import java.util.concurrent.CountDownLatch;

public class PerfClient {
    protected final int numRequests;
    protected final String host;
    protected final int port;
    protected final int numClients;
    protected final CountDownLatch countDownLatch;

    public PerfClient() {
        this.numRequests = 100000;
        this.numClients = 100;
        this.countDownLatch = new CountDownLatch(numClients);
        this.host = "localhost";
        this.port = 8080;
    }

    protected void printReport(long elapsedTime) {
        System.out.println("Execution complete");
        long elapsedSeconds = elapsedTime / 1000;
        System.out.println("Total elapsed time: " + elapsedSeconds);
        long totalRequests = numClients * numRequests;
        double tps = totalRequests / (double) elapsedTime * 1000;
        System.out.println("Total requests: " + numClients * numRequests);
        System.out.println("TPS: " + tps);
    }
}
