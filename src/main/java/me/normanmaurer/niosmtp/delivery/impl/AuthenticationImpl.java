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
package me.normanmaurer.niosmtp.delivery.impl;

import me.normanmaurer.niosmtp.delivery.Authentication;

/**
 * {@link Authentication} implementation for simple usage
 * 
 * @author Norman Maurer
 *
 */
public class AuthenticationImpl implements Authentication{

    private final String username;
    private final String password;
    private final AuthMode mode;

    public AuthenticationImpl(String username, String password, AuthMode mode) {
        this.username = username;
        this.password = password;
        this.mode = mode;
    }

    @Override
    public AuthMode getMode() {
        return mode;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }        
    
    /**
     * Create an {@link Authentication} instance which use plain mode
     * 
     * @param username
     * @param password
     * @return authPlain
     */
    public static Authentication plain(String username, String password) {
        return new AuthenticationImpl(username, password, AuthMode.Plain);
    }
    
    
    /**
     * Create an {@link Authentication} instance which use login mode
     * 
     * @param username
     * @param password
     * @return authLogin
     */
    public static Authentication login(String username, String password) {
        return new AuthenticationImpl(username, password, AuthMode.Login);
    }
}
