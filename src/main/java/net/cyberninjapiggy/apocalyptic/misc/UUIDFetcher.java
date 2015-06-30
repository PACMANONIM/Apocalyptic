/*
 * Copyright (C) 2015 Kaisar Arkhan
 * Copyright (C) 2014 Nick Schatz
 * 
 * This file is part of Apocalyptic.
 * 
 * Apocalyptic is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Apocalyptic is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Apocalyptic. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package net.cyberninjapiggy.apocalyptic.misc;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.ImmutableList;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
  private static final int MAX_SEARCH = 100;
  private static final String PROFILE_URL = "https://api.mojang.com/profiles/page/";
  private static final String AGENT = "minecraft";
  private final JSONParser jsonParser = new JSONParser();
  private final List<String> names;

  public UUIDFetcher(Collection<String> names) {
    this.names = ImmutableList.copyOf(names);
  }

  public Map<String, UUID> call() throws Exception {
    Map<String, UUID> uuidMap = new HashMap<>();
    String body = buildBody(names);
    for (int i = 1; i < MAX_SEARCH; i++) {
      HttpURLConnection connection = createConnection(i);
      writeBody(connection, body);
      JSONObject jsonObject =
          (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
      JSONArray array = (JSONArray) jsonObject.get("profiles");
      Number count = (Number) jsonObject.get("size");
      if (count.intValue() == 0) {
        break;
      }
      for (Object profile : array) {
        JSONObject jsonProfile = (JSONObject) profile;
        String id = (String) jsonProfile.get("id");
        String name = (String) jsonProfile.get("name");
        UUID uuid =
            UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-"
                + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
        uuidMap.put(name, uuid);
      }
    }
    return uuidMap;
  }

  private static void writeBody(HttpURLConnection connection, String body) throws Exception {
    DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
    writer.write(body.getBytes());
    writer.flush();
    writer.close();
  }

  private static HttpURLConnection createConnection(int page) throws Exception {
    URL url = new URL(PROFILE_URL + page);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);
    return connection;
  }

  @SuppressWarnings("unchecked")
  private static String buildBody(List<String> names) {
    List<JSONObject> lookups = new ArrayList<>();
    for (String name : names) {
      JSONObject obj = new JSONObject();
      obj.put("name", name);
      obj.put("agent", AGENT);
      lookups.add(obj);
    }
    return JSONValue.toJSONString(lookups);
  }
}
