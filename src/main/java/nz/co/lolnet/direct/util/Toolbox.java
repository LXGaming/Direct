/*
 * Copyright 2018 lolnet.co.nz
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

package nz.co.lolnet.direct.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Toolbox {
    
    public static ComponentBuilder getTextPrefix() {
        ComponentBuilder componentBuilder = new ComponentBuilder("");
        componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getPluginInformation().create()));
        componentBuilder.append("[" + Reference.PLUGIN_NAME + "]").bold(true).color(ChatColor.BLUE);
        componentBuilder.append(" ", ComponentBuilder.FormatRetention.NONE);
        return componentBuilder;
    }
    
    public static ComponentBuilder getPluginInformation() {
        ComponentBuilder componentBuilder = new ComponentBuilder("")
                .append(Reference.PLUGIN_NAME).color(ChatColor.BLUE).bold(true).append("\n")
                .append("    Version: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY).append(Reference.PLUGIN_VERSION).color(ChatColor.WHITE).append("\n")
                .append("    Authors: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY).append(Reference.AUTHORS).color(ChatColor.WHITE).append("\n")
                .append("    Source: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY).append(getURLClickEvent(Reference.SOURCE).create()).append("\n")
                .append("    Website: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY).append(getURLClickEvent(Reference.WEBSITE).create());
        return componentBuilder;
    }
    
    public static ComponentBuilder getURLClickEvent(String url) {
        ComponentBuilder componentBuilder = new ComponentBuilder("");
        componentBuilder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        componentBuilder.append(url).color(ChatColor.BLUE);
        componentBuilder.append(" ", ComponentBuilder.FormatRetention.NONE);
        return componentBuilder;
    }

    public static String convertColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    public static boolean isBlank(CharSequence charSequence) {
        int stringLength;
        if (charSequence == null || (stringLength = charSequence.length()) == 0) {
            return true;
        }
        
        for (int index = 0; index < stringLength; index++) {
            if (!Character.isWhitespace(charSequence.charAt(index))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isNotBlank(CharSequence charSequence) {
        return !isBlank(charSequence);
    }
    
    public static boolean containsIgnoreCase(String string, String searchString) {
        if (string == null || searchString == null) {
            return false;
        }
        
        int length = searchString.length();
        int max = string.length() - length;
        for (int index = 0; index <= max; index++) {
            if (string.regionMatches(true, index, searchString, 0, length)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Optional<InetSocketAddress> parseAddress(String host, int port) {
        try {
            return Optional.of(InetSocketAddress.createUnresolved(host, port));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
    
    public static <T> Optional<Set<T>> buildElements(String json, Class<T> type) {
        return parseJson(json, JsonArray.class).flatMap(jsonArray -> buildElements(jsonArray, type));
    }
    
    public static <T> Optional<Set<T>> buildElements(JsonArray jsonArray, Class<T> type) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return Optional.empty();
        }
        
        Set<T> elements = newHashSet();
        for (JsonElement jsonElement : jsonArray) {
            parseJson(jsonElement, type).ifPresent(elements::add);
        }
        
        return Optional.of(elements);
    }
    
    public static <T> Optional<T> parseJson(String json, Class<T> type) {
        try {
            return parseJson(new JsonParser().parse(json), type);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
    
    public static <T> Optional<T> parseJson(JsonElement jsonElement, Class<T> type) {
        try {
            return Optional.of(new Gson().fromJson(jsonElement, type));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
    
    @SuppressWarnings("deprecation")
    public static Map<String, ServerInfo> getProxyServers() {
        return ProxyServer.getInstance().getConfig().getServers();
    }
    
    @SuppressWarnings("deprecation")
    public static Collection<ListenerInfo> getProxyListeners() {
        return ProxyServer.getInstance().getConfig().getListeners();
    }
    
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }
    
    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(HashSet::new));
    }
}