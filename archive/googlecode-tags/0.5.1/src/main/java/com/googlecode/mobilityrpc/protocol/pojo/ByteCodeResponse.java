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
public class ByteCodeResponse {

    private final List<ClassData> byteCodeResponses;
    private final RequestIdentifier requestIdentifier;

    public ByteCodeResponse(List<ClassData> byteCodeResponses, RequestIdentifier requestIdentifier) {
        this.byteCodeResponses = byteCodeResponses;
        this.requestIdentifier = requestIdentifier;
    }

    public List<ClassData> getByteCodeResponses() {
        return byteCodeResponses;
    }

    public RequestIdentifier getRequestIdentifier() {
        return requestIdentifier;
    }

    public static class ClassData {
        private final String className;
        private final byte[] byteCode;

        public ClassData(String className, byte[] byteCode) {
            this.className = className;
            this.byteCode = byteCode;
        }

        public String getClassName() {
            return className;
        }

        public byte[] getByteCode() {
            return byteCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassData classData = (ClassData) o;
            return className.equals(classData.className)
                    && Arrays.equals(byteCode, classData.byteCode);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + Arrays.hashCode(byteCode);
            return result;
        }

        @Override
        public String toString() {
            return "ClassData{" +
                    "className='" + className + '\'' +
                    ", byteCode=" + byteCode.length + " bytes" +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteCodeResponse that = (ByteCodeResponse) o;

        return requestIdentifier.equals(that.requestIdentifier);

    }

    @Override
    public int hashCode() {
        return requestIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "ByteCodeResponse{" +
                "byteCodeResponses=" + byteCodeResponses +
                ", requestIdentifier=" + requestIdentifier +
                '}';
    }
}
