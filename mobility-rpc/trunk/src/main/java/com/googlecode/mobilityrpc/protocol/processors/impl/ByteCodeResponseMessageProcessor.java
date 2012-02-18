/**
 * Copyright 2011 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mobilityrpc.protocol.processors.impl;

import com.googlecode.mobilityrpc.controller.impl.MobilityControllerInternal;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.session.MobilitySession;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class ByteCodeResponseMessageProcessor implements DeserializedMessageProcessor<ByteCodeResponse> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void process(MobilityControllerInternal mobilityController, ConnectionManager connectionManager, ConnectionIdentifier connectionIdentifier, ByteCodeResponse byteCodeResponse) {
        RequestIdentifier requestIdentifier = byteCodeResponse.getRequestIdentifier();
        MobilitySession session = mobilityController.getSession(requestIdentifier.getSessionId());
        logger.log(Level.FINER, "Received ByteCodeResponse, submitting to session class loader: {0}", byteCodeResponse);
        session.getSessionClassLoader().processBytecodeResponse(byteCodeResponse);
    }
}
