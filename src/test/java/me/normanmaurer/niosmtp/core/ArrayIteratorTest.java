/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.core;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * 
 * @author Norman Maurer
 *
 */
public class ArrayIteratorTest {
    
    private final static String[] EMPTY_ARRAY = new String[] {};
    private final static String[] ONE_ELEMENT_ARRAY = new String[] { "1"};
    private final static String[] THREE_ELEMENTS_ARRAY = new String[] { "1", "2", "3"};

    @Test
    public void testEmpty() {
        Iterator<String> it = new ArrayIterator<String>(EMPTY_ARRAY);
        checkEmpty(it);
    }
    
    @Test
    public void testOneElement() {
        Iterator<String> it = new ArrayIterator<String>(ONE_ELEMENT_ARRAY);
        assertTrue(it.hasNext());
        assertEquals(ONE_ELEMENT_ARRAY[0], it.next());
        checkEmpty(it);
    }
    
    @Test
    public void testThreeElements() {
        Iterator<String> it = new ArrayIterator<String>(THREE_ELEMENTS_ARRAY);
        assertTrue(it.hasNext());
        assertEquals(THREE_ELEMENTS_ARRAY[0], it.next());
        assertTrue(it.hasNext());
        assertEquals(THREE_ELEMENTS_ARRAY[1], it.next());
        assertTrue(it.hasNext());
        assertEquals(THREE_ELEMENTS_ARRAY[2], it.next());
        checkEmpty(it);
    }
    
    
    
    @Test
    public void testReadOnly() {
        Iterator<String> it = new ArrayIterator<String>(ONE_ELEMENT_ARRAY);
        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    private void checkEmpty(Iterator<String> it) {
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

    }
}
