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
import com.googlecode.mobilityrpc.protocol.pojo.ResourceResponse;
import com.googlecode.mobilityrpc.session.MobilitySession;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceRequest;
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
public class ResourceRequestMessageProcessor implements DeserializedMessageProcessor<ResourceRequest> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void process(MobilityControllerInternal mobilityController, ConnectionManager connectionManager, ConnectionId connectionId, ResourceRequest resourceRequest) {
        RequestIdentifier requestIdentifier = resourceRequest.getRequestIdentifier();
        MobilitySession session = mobilityController.getSession(requestIdentifier.getSessionId());
        if (resourceRequest.getResourceNames().size() != 1) {
            // Note: although the protocol supports returning multiple classes/resources in a single round-trip,
            // this optimization has not yet been implemented.
            // Throw exception if we receive such a request for multiple resources...
            throw new IllegalStateException("Unsupported number of resources requested, expected one resource: " + resourceRequest.getResourceNames());
        }
        final String requestedResourceName = resourceRequest.getResourceNames().get(0);
        byte[] resourceData;
        try {
            resourceData = getResourceFromClassLoader(requestedResourceName, session.getSessionClassLoader());
        }
        catch (ClassNotFoundException e) {
            resourceData = null;
        }

        List<ResourceResponse.ResourceData> resourceDataListToReturn;
        if (resourceData != null) {
            resourceDataListToReturn = Collections.singletonList(new ResourceResponse.ResourceData(requestedResourceName, resourceData));
        }
        else {
            resourceDataListToReturn = Collections.emptyList();
        }
        // Prepare ResourceResponse...
        ResourceResponse resourceResponse = new ResourceResponse(
                resourceDataListToReturn,
                requestIdentifier
        );
        mobilityController.sendOutgoingMessage(connectionId, resourceResponse);
        if (logger.isLoggable(Level.FINER)) {
            if (resourceResponse.getResourceDataResponses().isEmpty()) {
                logger.log(Level.FINER, "Failed to locate class bytecode or resource '" + requestedResourceName + "', returned response: " + resourceResponse);
            }
            else {
                logger.log(Level.FINER, "Successfully located class bytecode or resource '" + requestedResourceName + "', returned response: " + resourceResponse);
            }
        }
    }

    public static byte[] getResourceFromClassLoader(String resourceName, ClassLoader classLoader) throws ClassNotFoundException {
        try {
//            String resourceName = resourceName.replace('.', '/') + ".class";
            InputStream inputStream = classLoader.getResourceAsStream(resourceName);
            if (inputStream == null) {
                throw new ClassNotFoundException("Class loader could not locate class bytecode or resource '" + resourceName + "' via class loader '" + classLoader + "'");
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
            throw new IllegalStateException("Failed to load class bytecode or resource: " + resourceName + ", class loader: " + classLoader);
        }
    }

}
