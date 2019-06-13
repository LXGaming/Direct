/*
 * Copyright 2018 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.direct.bungee.util;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import io.github.lxgaming.direct.common.util.Reference;

import java.util.Collection;
import java.util.Collections;

public class DirectCommandSender implements CommandSender {
    
    @Override
    public String getName() {
        return Reference.NAME;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void sendMessage(String message) {
        ProxyServer.getInstance().getLogger().info(message);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void sendMessages(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    
    @Override
    public void sendMessage(BaseComponent... message) {
        sendMessage(BaseComponent.toLegacyText(message));
    }
    
    @Override
    public void sendMessage(BaseComponent message) {
        sendMessage(message.toLegacyText());
    }
    
    @Override
    public Collection<String> getGroups() {
        return Collections.emptySet();
    }
    
    @Override
    public void addGroups(String... groups) {
        throw new UnsupportedOperationException("Direct may not have groups");
    }
    
    @Override
    public void removeGroups(String... groups) {
        throw new UnsupportedOperationException("Direct may not have groups");
    }
    
    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
    
    @Override
    public void setPermission(String permission, boolean value) {
        throw new UnsupportedOperationException("Direct has all permissions");
    }
    
    @Override
    public Collection<String> getPermissions() {
        return Collections.emptySet();
    }
}