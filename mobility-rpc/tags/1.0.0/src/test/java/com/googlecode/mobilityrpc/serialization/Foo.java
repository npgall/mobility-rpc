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
package com.googlecode.mobilityrpc.serialization;

/**
 * @author Niall Gallagher
 */
public class Foo {

    private final int bar;

    public Foo(int bar) {
        this.bar = bar;
    }

    public int getBar() {
        return bar;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "bar=" + bar +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Foo foo = (Foo) o;

        return bar == foo.bar;

    }

    @Override
    public int hashCode() {
        return bar;
    }
}
