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
package com.googlecode.mobilityrpc.benchmarks.rmi;

import java.io.Serializable;
import java.util.List;

/**
 * @author Niall Gallagher
 */
public class Person implements Comparable<Person>, Serializable { // .. we implement Serializable only for RMI benchmark

    long personId;
    String firstName;
    String lastName;
    List<String> phoneNumbers;
    int houseNumber;
    String street;
    String city;
    String country;


    public Person(long personId, String firstName, String lastName, List<String> phoneNumbers, int houseNumber, String street, String city, String country) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return personId == person.personId;

    }

    @Override
    public int hashCode() {
        return (int) (personId ^ (personId >>> 32));
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + personId +
                '}';
    }


    @Override
    public int compareTo(Person o) {
        if (personId > o.personId) {
            return 1;
        }
        else if (personId < o.personId) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
