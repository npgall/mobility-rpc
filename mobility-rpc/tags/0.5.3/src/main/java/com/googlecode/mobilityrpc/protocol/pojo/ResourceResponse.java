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
package com.googlecode.mobilityrpc.protocol.pojo;

import java.util.Arrays;
import java.util.List;

/**
 * @author Niall Gallagher
 */
public class ResourceResponse {

    private final List<ResourceData> resourceDataResponses;
    private final RequestIdentifier requestIdentifier;

    public ResourceResponse(List<ResourceData> resourceDataResponses, RequestIdentifier requestIdentifier) {
        this.resourceDataResponses = resourceDataResponses;
        this.requestIdentifier = requestIdentifier;
    }

    public List<ResourceData> getResourceDataResponses() {
        return resourceDataResponses;
    }

    public RequestIdentifier getRequestIdentifier() {
        return requestIdentifier;
    }

    public static class ResourceData {
        private final String resourceName;
        private final byte[] resourceData;

        public ResourceData(String resourceName, byte[] resourceData) {
            this.resourceName = resourceName;
            this.resourceData = resourceData;
        }

        public String getResourceName() {
            return resourceName;
        }

        public byte[] getResourceData() {
            return resourceData;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResourceData resourceData = (ResourceData) o;
            return resourceName.equals(resourceData.resourceName)
                    && Arrays.equals(this.resourceData, resourceData.resourceData);
        }

        @Override
        public int hashCode() {
            int result = resourceName.hashCode();
            result = 31 * result + Arrays.hashCode(resourceData);
            return result;
        }

        @Override
        public String toString() {
            return "ResourceData{" +
                    "resourceName='" + resourceName + '\'' +
                    ", resourceData=" + resourceData.length + " bytes" +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceResponse that = (ResourceResponse) o;

        return requestIdentifier.equals(that.requestIdentifier);

    }

    @Override
    public int hashCode() {
        return requestIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "ResourceResponse{" +
                "resourceDataResponses=" + resourceDataResponses +
                ", requestIdentifier=" + requestIdentifier +
                '}';
    }
}
