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

package com.byteatebit.nbecho.computethread;

import com.byteatebit.common.util.CollectionsUtil;
import com.byteatebit.nbecho.NbEchoService;
import com.byteatebit.nbserver.INbContext;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.function.Consumer;

public class NbEchoComputeThreadsService extends NbEchoService {


    public NbEchoComputeThreadsService(INbContext nbContext, SocketChannel socket) {
        super(nbContext, socket);
    }

    protected void echo(List<String> commands) {
        if (commands.isEmpty()) {
            readMessage();
            return;
        }
        echo(commands.get(0), () -> nbContext.schedule(() -> echo(CollectionsUtil.cdr(commands)), this::handleException));
    }

    protected void readMessage() {
        Consumer<List<String>> messageCallback = (messages) -> nbContext.schedule(() -> echo(messages), this::handleException);
        readDelimitedMessageTask.readMessages(this::register, messageCallback, this::handleException);
    }

}