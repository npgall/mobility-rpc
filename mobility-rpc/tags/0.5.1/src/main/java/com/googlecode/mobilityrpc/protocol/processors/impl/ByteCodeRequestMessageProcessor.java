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
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.session.MobilitySession;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeRequest;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class ByteCodeRequestMessageProcessor implements DeserializedMessageProcessor<ByteCodeRequest> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void process(MobilityControllerInternal mobilityController, ConnectionManager connectionManager, ConnectionId connectionId, ByteCodeRequest byteCodeRequest) {
        RequestIdentifier requestIdentifier = byteCodeRequest.getRequestIdentifier();
        MobilitySession session = mobilityController.getSession(requestIdentifier.getSessionId());
        if (byteCodeRequest.getClassNames().size() != 1) {
            // Note: although the protocol supports returning multiple classes,
            // implementing this optimization is reserved for future work.
            // Throw exception if we receive such a request for multiple classes...
            throw new IllegalStateException("Unsupported number of classes requested, expected one class: " + byteCodeRequest.getClassNames());
        }
        final String requestedClassName = byteCodeRequest.getClassNames().get(0);
        byte[] byteCode;
        try {
            byteCode = getByteCodeForClass(requestedClassName, session.getSessionClassLoader());
        }
        catch (ClassNotFoundException e) {
            byteCode = null;
        }

        List<ByteCodeResponse.ClassData> classDataListToReturn;
        if (byteCode != null) {
            classDataListToReturn = Collections.singletonList(new ByteCodeResponse.ClassData(requestedClassName, byteCode));
        }
        else {
            classDataListToReturn = Collections.emptyList();
        }
        // Prepare ByteCodeResponse...
        ByteCodeResponse byteCodeResponse = new ByteCodeResponse(
                classDataListToReturn,
                requestIdentifier
        );
        mobilityController.sendOutgoingMessage(connectionId, byteCodeResponse);
        if (logger.isLoggable(Level.FINER)) {
            if (byteCodeResponse.getByteCodeResponses().isEmpty()) {
                logger.log(Level.FINER, "Failed to locate bytecode for class '" + requestedClassName + "', returned response: " + byteCodeResponse);
            }
            else {
                logger.log(Level.FINER, "Successfully located bytecode for class '" + requestedClassName + "', returned response: " + byteCodeResponse);
            }
        }
    }

    public static byte[] getByteCodeForClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            String resourceName = className.replace('.', '/') + ".class";
            InputStream inputStream = classLoader.getResourceAsStream(resourceName);
            if (inputStream == null) {
                throw new ClassNotFoundException("Class loader could not locate class '" + className + "' with resource name '" + resourceName + "' via class loader '" + classLoader + "'");
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int value = 0; value != -1;) {
                value = inputStream.read();
                if (value != -1) {
                    byteArrayOutputStream.write(value);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }
        catch (ClassNotFoundException e) {
            // Rethrow any possibly expected ClassNotFoundExceptions...
            throw e;
        }
        catch (Exception e) {
            // Catch and rethrow other unexpected exceptions...
            throw new IllegalStateException("Failed to load byte code for class: " + className + ", class loader: " + classLoader);
        }
    }

}
