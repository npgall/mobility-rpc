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

import java.util.UUID;

/**
 * @author Niall Gallagher
 */
public class RequestIdentifier {

    private final UUID sessionId;
    private final UUID requestId;
    private final String requestLabel;

    public RequestIdentifier(UUID sessionId, UUID requestId, String requestLabel) {
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.requestLabel = requestLabel;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public String getRequestLabel() {
        return requestLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestIdentifier that = (RequestIdentifier) o;

        return requestId.equals(that.requestId)
                && sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + requestId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RequestIdentifier{" +
                "sessionId=" + sessionId +
                ", requestId=" + requestId +
                ", requestLabel='" + requestLabel + '\'' +
                '}';
    }
}
