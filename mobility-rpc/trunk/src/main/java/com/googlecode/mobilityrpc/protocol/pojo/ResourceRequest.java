/**
 * Copyright 2011, 2012 Niall Gallagher
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
package com.googlecode.mobilityrpc.protocol.pojo;

import java.util.List;

/**
 * @author Niall Gallagher
 */
public class ResourceRequest {

    private final List<String> resourceNames;
    private final RequestIdentifier requestIdentifier;

    public ResourceRequest(List<String> resourceNames, RequestIdentifier requestIdentifier) {
        this.resourceNames = resourceNames;
        this.requestIdentifier = requestIdentifier;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public RequestIdentifier getRequestIdentifier() {
        return requestIdentifier;
    }

    /**
     * @throws UnsupportedOperationException always, as this object is not intended to be compared for equality
     * or used as a key in a hash map.
     */
    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @throws UnsupportedOperationException always, as this object is not intended to be compared for equality
     * or used as a key in a hash map.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return "ResourceRequest{" +
                "resourceNames=" + resourceNames +
                ", requestIdentifier=" + requestIdentifier +
                '}';
    }
}
