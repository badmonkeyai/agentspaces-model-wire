/*
 * Copyright 2026 Bad Monkey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.badmonkey.agentspaces.springai.model.wire;

import ai.badmonkey.agentspaces.common.codec.CborCodec;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the canonical CBOR bytes of every wire record, so a peer on another
 * framework that shares these classes stays byte-compatible with this one.
 * Run with {@code -Dwire.golden.write=true} to regenerate the vectors after
 * a deliberate change.
 */
class WireGoldenTest {

    static Map<String, Object> vectors() {
        WireMessage user = new WireMessage("user", "Plan the offsite", List.of(), List.of(),
                List.of(new WireMedia("image/png", "map.png", new byte[] {1, 2, 3}, null)),
                Map.of("k", "v"));
        WireMessage assistant = new WireMessage("assistant", "", List.of(
                new WireToolCall("call-1", "function", "fleet_pricer", "{\"orderId\":\"1\"}")),
                List.of(), List.of(), Map.of());
        WireMessage tool = new WireMessage("tool", "", List.of(), List.of(
                new WireToolResponse("call-1", "fleet_pricer", "{\"total\":28.5}")),
                List.of(), Map.of());
        WireOptions options = new WireOptions("gpt-4.1-mini", 0.2, 512, 0.9, 40, 0.0, 0.0,
                List.of("END"));
        WireUsage usage = new WireUsage(12, 34, 46);
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("ModelRequest", new ModelRequest("req-1", "gpt-4.1-mini", List.of(user, assistant, tool),
                options, List.of(new WireToolDefinition("fleet_pricer", "Prices an order",
                        "{\"type\":\"object\"}")), true, "analyst"));
        v.put("ModelResponse", new ModelResponse("req-1", "gpt-4.1-mini",
                List.of(new WireGeneration(assistant, "TOOL_CALLS")), usage, null, "peer-a", 1));
        v.put("ModelChunk", new ModelChunk("req-1", 1, 3,
                List.of(new WireGeneration(new WireMessage("assistant", "Hel", List.of(), List.of(),
                        List.of(), Map.of()), null)), false, null, null, "peer-a"));
        v.put("ModelCancel", new ModelCancel("req-1", "analyst"));
        v.put("ConversationSnapshot", new ConversationSnapshot("conv-1", 2, List.of(user)));
        v.put("ModelUsage", new ModelUsage("gpt-4.1-mini", "openai", 12, 34, 46, "analyst", "peer-b"));
        v.put("ModelUsageTotal", new ModelUsageTotal("gpt-4.1-mini", "peer-b", 120, 340, 460, 1700000000000L));
        v.put("ModelAttempt", new ModelAttempt("req-1", 2, "peer-c"));
        return v;
    }

    @Test
    void everyRecordMatchesItsGoldenBytes() throws IOException {
        CborCodec codec = CborCodec.defaultCodec();
        Map<String, Object> vectors = vectors();
        Properties golden = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/wire-golden.properties")) {
            if (in != null) {
                golden.load(in);
            }
        }
        if (Boolean.getBoolean("wire.golden.write")) {
            StringBuilder out = new StringBuilder("# Canonical CBOR of each wire record; see WireGoldenTest.\n");
            vectors.forEach((name, value) -> out.append(name).append('=')
                    .append(HexFormat.of().formatHex(codec.toBytes(value))).append('\n'));
            Files.writeString(Path.of("src/main/resources/wire-golden.properties"), out.toString(),
                    StandardCharsets.UTF_8);
            return;
        }
        assertThat(golden).as("wire-golden.properties present").isNotEmpty();
        vectors.forEach((name, value) -> {
            String hex = HexFormat.of().formatHex(codec.toBytes(value));
            assertThat(hex).as(name).isEqualTo(golden.getProperty(name));
            // Records holding byte[] compare by identity, so compare the re-encoded bytes.
            Object back = codec.fromBytes(codec.toBytes(value), value.getClass());
            assertThat(HexFormat.of().formatHex(codec.toBytes(back))).as(name + " round trip").isEqualTo(hex);
        });
    }
}
