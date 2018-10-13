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

package nz.co.lolnet.direct.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Toolbox {
    
    /**
     * Removes non-printable characters (excluding new line and carriage return) in the provided {@link java.lang.String String}.
     *
     * @param string The {@link java.lang.String String} to filter.
     * @return The filtered {@link java.lang.String String}.
     */
    public static String filter(String string) {
        return string.replaceAll("[^\\x20-\\x7E\\x0A\\x0D]", "");
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
    
    public static boolean containsIgnoreCase(Collection<String> list, String targetString) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        
        for (String string : list) {
            if (string.equalsIgnoreCase(targetString)) {
                return true;
            }
        }
        
        return false;
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
    
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(ArrayList::new));
    }
    
    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(HashSet::new));
    }
    
    @SafeVarargs
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }
    
    @SafeVarargs
    public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) {
        return Stream.of(elements).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }
}