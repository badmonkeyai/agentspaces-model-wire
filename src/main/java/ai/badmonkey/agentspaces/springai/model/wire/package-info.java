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
/**
 * The wire records of the fleet model service, chat memory snapshots, and
 * usage entries: what a peer writes into a space when it asks the fleet for
 * a model round-trip ({@link ai.badmonkey.agentspaces.springai.model.wire.ModelRequest}),
 * answers one ({@link ai.badmonkey.agentspaces.springai.model.wire.ModelResponse},
 * {@link ai.badmonkey.agentspaces.springai.model.wire.ModelChunk},
 * {@link ai.badmonkey.agentspaces.springai.model.wire.ModelAttempt}), cancels
 * one ({@link ai.badmonkey.agentspaces.springai.model.wire.ModelCancel}), keeps a
 * conversation ({@link ai.badmonkey.agentspaces.springai.model.wire.ConversationSnapshot}),
 * or records usage ({@link ai.badmonkey.agentspaces.springai.model.wire.ModelUsage}).
 *
 * <p>Plain Java, with no framework type: strings, numbers, lists, maps, and
 * byte arrays, encoded by the canonical CBOR codec of the AgentSpaces core.
 * The space names an entry type by its fully qualified class name, so these
 * classes are shared by the Spring AI and Micronaut LangChain4j bridges
 * rather than copied, and the package keeps the name the records were born
 * with. The golden vectors shipped in {@code wire-golden.properties} pin the
 * bytes of every record.
 */
package ai.badmonkey.agentspaces.springai.model.wire;
