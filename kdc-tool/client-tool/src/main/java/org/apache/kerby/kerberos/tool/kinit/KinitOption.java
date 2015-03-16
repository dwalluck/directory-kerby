/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.kerby.kerberos.tool.kinit;

import org.apache.kerby.kerberos.kerb.client.KOption;

public enum KinitOption implements KOption {
    NONE("NONE"),
    LIFE_TIME("-l", "lifetime"),
    START_TIME("-s", "start time"),
    RENEWABLE_TIME("-r", "renewable lifetime"),
    FORWARDABLE("-f", "forwardable"),
    NOT_FORWARDABLE("-F", "not forwardable"),
    PROXIABLE("-p", "proxiable"),
    NOT_PROXIABLE("-P", "not proxiable"),
    ANONYMOUS("-n", "anonymous"),
    INCLUDE_ADDRESSES("-a", "include addresses"),
    NOT_INCLUDE_ADDRESSES("-A", "do not include addresses"),
    VALIDATE("-v", "validate"),
    RENEW("-R", "renew"),
    CANONICALIZE("-C", "canonicalize"),
    AS_ENTERPRISE_PN("-E", "client is enterprise principal name"),
    USE_KEYTAB("-k", "use keytab"),
    USE_DFT_KEYTAB("-i", "use default client keytab (with -k)"),
    USER_KEYTAB_FILE("-t", "filename of keytab to use"),
    KRB5_CACHE("-c", "Kerberos 5 cache name"),
    SERVICE("-S", "service"),
    ARMOR_CACHE("-T", "armor credential cache"),
    XATTR("-X", "<attribute>[=<value>]"),
    ;

    private String name;
    private String description;
    private Object value;

    KinitOption(String description) {
        this.description = description;
    }

    KinitOption(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        return name();
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public static KinitOption fromName(String name) {
        if (name != null) {
            for (KinitOption ko : values()) {
                if (ko.getName().equals(name)) {
                    return (KinitOption) ko;
                }
            }
        }
        return NONE;
    }
}

