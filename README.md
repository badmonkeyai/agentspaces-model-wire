# agentspaces-model-wire

The wire records of the fleet model service, chat memory snapshots, and usage
entries, as one small artifact with no dependencies:

| Records | Written by |
| --- | --- |
| `ModelRequest`, `ModelResponse`, `ModelChunk`, `ModelCancel`, `ModelAttempt`, and the `Wire*` parts they carry (`WireMessage`, `WireToolCall`, `WireToolResponse`, `WireToolDefinition`, `WireOptions`, `WireGeneration`, `WireUsage`, `WireMedia`) | the model service: `FleetChatModel` and `ModelServer` in `agentspaces-springai` (Spring AI) and `agentspaces-langchain4j` (LangChain4j on any host, which `agentspaces-micronaut-langchain4j` wires) |
| `ConversationSnapshot` | chat memory in a space: `SpaceChatMemoryRepository` (Spring AI) and `SpaceChatMemoryStore` (LangChain4j) |
| `ModelUsage` | fleet-wide token usage on both |

The space names an entry type by its fully qualified class name, so a peer
on one framework can only read what a peer on the other wrote if both use
the same classes. That is what this artifact is for: a Micronaut service
with no provider key calls a model a Spring Boot peer serves, and the
reverse; a conversation one framework's worker started resumes on the
other's after a crash; a mixed fleet's console sums usage as one.

The package is `ai.badmonkey.agentspaces.springai.model.wire`, where the
records were born. Renaming it would change the schema name on the wire, so
it stays until a protocol version change.

The artifact is on Maven Central under the group `ai.badmonkey.agentspaces`,
with no dependencies of its own. `agentspaces-springai`,
`agentspaces-langchain4j`, and `agentspaces-micronaut-langchain4j` bring it in transitively; add it directly
only to read or write the records from code of your own.

Maven (`pom.xml`):

```xml
<dependency>
  <groupId>ai.badmonkey.agentspaces</groupId>
  <artifactId>agentspaces-model-wire</artifactId>
  <version>0.5.0</version>
</dependency>
```

Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("ai.badmonkey.agentspaces:agentspaces-model-wire:0.5.0")
}
```

Gradle Groovy DSL (`build.gradle`):

```groovy
dependencies {
    implementation 'ai.badmonkey.agentspaces:agentspaces-model-wire:0.5.0'
}
```

`WireGoldenTest` pins the canonical CBOR bytes of every record in
`wire-golden.properties`, shipped inside the jar so the consumers' suites
read the same vectors from the classpath. After a deliberate record change, regenerate with
`mvn test -Dwire.golden.write=true` and bump the version. `mvn clean install`
builds it; both consuming projects resolve it from Maven Central, or from the
local repository when building against unreleased sources.

## The gateway convention (v0.1.24, ISSUE-ModelCard)

A peer advertising `aspace:cap/llm-gateway` (parameters `tasks`, `policyVersion`, `coordination`) routes model requests for its group. A `ModelRequest` is the gateway's when it is written with the tag `task=<kind>` (`chat`, `embed`, `rerank`, `classify`, `transcribe`, `vision`) or when its `model` is the gateway's advertised model name; a request addressed to a model name a `ModelServer` advertises is unchanged and the server answers it as before. The gateway chooses a server by its routing policy, forwards the request under a new request id addressed to the chosen model, screens the prompt and the completion through `agentspaces-guardrail`, and completes the original take with the response (or a structured failure naming the check that refused it, in `ModelResponse.error`). A `ModelCard` advertisement (SPEC §6.1a.1) names the models a group may route to.

## Releasing

The `central-publish` profile attaches the sources and javadoc jars, signs
every artifact with GPG (the passphrase from `MAVEN_GPG_PASSPHRASE`), and
uploads through the Sonatype Central Portal using the `central` server id in
`settings.xml`:

```sh
mvn -Pcentral-publish clean deploy
```

CI runs the same profile unsigned (`-Dgpg.skip=true`) on every push, so a
javadoc or packaging problem fails a pull request rather than a release. The
jar carries only the records, `package-info`, the golden vectors, and the
licence and notice under `META-INF`.
