/*
 * Copyright 2026 Bad Monkey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.badmonkey.agentspaces.springai.model.wire;

/**
 * One peer's running token total for one model, republished on the usage publish interval under a
 * short lease: a per-peer monotone counter that anti-entropy carries like any entry, so the fleet
 * total is the sum of the newest total per issuer and no gossip protocol runs for it. Replaced the
 * push-sum epoch per time window, which every peer gossiped on every tick for the life of the
 * process (PERF2 §3.5).
 *
 * @param model            the model name
 * @param peer             the issuing peer's id, as a string
 * @param promptTokens     prompt tokens so far
 * @param completionTokens completion tokens so far
 * @param totalTokens      total tokens so far
 * @param sinceMillis      when this peer's count started, epoch millis
 */
public record ModelUsageTotal(String model, String peer, long promptTokens, long completionTokens,
                              long totalTokens, long sinceMillis) {
}
