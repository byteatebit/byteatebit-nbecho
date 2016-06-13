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

import com.byteatebit.common.builder.BaseBuilder;
import com.byteatebit.common.server.IServer;
import com.byteatebit.nbserver.ISocketChannelHandler;
import com.byteatebit.nbserver.NbServiceConfig;
import com.byteatebit.nbserver.simple.SimpleNbServer;
import com.byteatebit.nbserver.simple.SimpleNbServerConfig;
import com.byteatebit.nbserver.simple.tcp.TcpConnectorFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;

import java.io.IOException;

public class NbEchoServer {

    protected final IServer server;

    public NbEchoServer(ObjectBuilder builder) {
        server = SimpleNbServer.Builder.builder()
                .withConfig(SimpleNbServerConfig.builder()
                        .withListenPort(builder.port)
                        .withNbServiceConfig(NbServiceConfig.Builder.builder()
                            .withNumThreads(builder.numThreads)
                            .withNumIoThreads(builder.numIoThreads)
                            .withSelectorTimeoutMs(builder.selectorTimeoutMs)
                            .build())
                        .build())
                .withConnectorFactory(TcpConnectorFactory.Builder.builder()
                        .withMaxServerSocketBacklog(300)
                        .withConnectedSocketTask(builder.socketChannelHandler)
                        .build())
                .build();
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() throws IOException {
        server.shutdown();
    }

    public static void main(String args[]) throws IOException {
        run(args, new NbEchoConnectHandler());
    }

    public static void run(String args[], ISocketChannelHandler socketChannelHandler) throws IOException {
        NbEchoServer server;
        try {
            NbEchoServer.Builder serverBuilder = NbEchoServer.Builder.builder()
                    .withSocketChannelHandler(socketChannelHandler)
                    .withCommandLine(args);
            if (serverBuilder.shouldPrintHelp()) {
                serverBuilder.printUsage();
                return;
            }
            server = serverBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            NbEchoServer.Builder.builder().printUsage();
            return;
        }
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static class Builder extends ObjectBuilder<Builder> {
        @Override
        public Builder self() {
            return this;
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public abstract static class ObjectBuilder<T extends ObjectBuilder<T>> extends BaseBuilder<T> {

        public static final String HELP_OPTION                  = "h";
        public static final String PORT_OPTION                  = "p";
        public static final String THREAD_OPTION                = "t";
        public static final String IO_THREAD_OPTION             = "it";
        public static final String SELECTOR_TIMEOUT_OPTION      = "st";
        public static final int DEFAULT_PORT                    = 8080;
        public static final int DEFAULT_NUM_THREADS             = 2;
        public static final int DEFAULT_NUM_IO_THREADS          = 1;
        public static final int DEFAULT_SELECTOR_TIMEOUT        = 1;

        protected int port;
        protected int numThreads;
        protected int numIoThreads;
        protected long selectorTimeoutMs;
        protected boolean help = false;
        protected ISocketChannelHandler socketChannelHandler = new NbEchoConnectHandler();

        public T withSocketChannelHandler(ISocketChannelHandler socketChannelHandler) {
            this.socketChannelHandler = socketChannelHandler;
            return self();
        }

        protected Options createOptions() {
            Options options = new Options();
            options.addOption(Option.builder(HELP_OPTION)
                    .longOpt("help")
                    .desc("Display help")
                    .build());
            options.addOption(Option.builder(PORT_OPTION)
                            .longOpt("port")
                            .desc("Listen port")
                            .hasArg()
                            .build());
            options.addOption(Option.builder(THREAD_OPTION)
                    .longOpt("threads")
                    .desc("Number of threads for the server.  The minimum value is 2, one for " +
                            "IOTasks and one for the ComputeTask executor")
                    .hasArg()
                    .build());
            options.addOption(Option.builder(IO_THREAD_OPTION)
                    .longOpt("io-threads")
                    .desc("Number of IO threads for the server.  The default value is 2")
                    .hasArg()
                    .build());
            options.addOption(Option.builder(SELECTOR_TIMEOUT_OPTION)
                    .longOpt("selector-timeout")
                    .desc("The amount of time to wait on a select call.  The default is 1 ms.  " +
                            "A value of 0 results in a selectNow() call with a thread yield on 0 ready keys.")
                    .hasArg()
                    .build());
            return options;
        }

        protected T withCommandLine(String args[]) throws ParseException {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(createOptions(), args);
            String portString = commandLine.getOptionValue(PORT_OPTION, String.valueOf(DEFAULT_PORT));
            String numThreadsString = commandLine.getOptionValue(THREAD_OPTION, String.valueOf(DEFAULT_NUM_THREADS));
            String numIoThreadsString = commandLine.getOptionValue(IO_THREAD_OPTION, String.valueOf(DEFAULT_NUM_IO_THREADS));
            String selectorTimeoutString = commandLine.getOptionValue(SELECTOR_TIMEOUT_OPTION, String.valueOf(DEFAULT_SELECTOR_TIMEOUT));
            help = commandLine.hasOption(HELP_OPTION);
            port = Integer.parseInt(portString);
            numThreads = Integer.parseInt(numThreadsString);
            numIoThreads = Integer.parseInt(numIoThreadsString);
            selectorTimeoutMs = Long.parseLong(selectorTimeoutString);
            return self();
        }

        public NbEchoServer build() throws ParseException {
            Preconditions.checkArgument(numThreads >= 2, "numThreads must be >= 2");
            Preconditions.checkArgument(numIoThreads >= 1, "numIoThreads must be >= 1");
            Preconditions.checkArgument(numIoThreads >= 0, "selectorTimeout must be >= 0");
            return new NbEchoServer(this);
        }

        public boolean shouldPrintHelp() {
            return help;
        }

        public void printUsage() {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("NbEchoServer", createOptions());
        }

    }
}
