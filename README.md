# agentspaces-model-wire

The wire records of the fleet model service, chat memory snapshots, and usage
entries, as one small artifact with no dependencies:

| Records | Written by |
| --- | --- |
| `ModelRequest`, `ModelResponse`, `ModelChunk`, `ModelCancel`, `ModelAttempt`, and the `Wire*` parts they carry (`WireMessage`, `WireToolCall`, `WireToolResponse`, `WireToolDefinition`, `WireOptions`, `WireGeneration`, `WireUsage`, `WireMedia`) | the model service: `FleetChatModel` and `ModelServer` in `agentspaces-springai` (Spring AI) and `agentspaces-micronaut-langchain4j` (LangChain4j) |
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

```xml
<dependency>
  <groupId>ai.badmonkey.agentspaces</groupId>
  <artifactId>agentspaces-model-wire</artifactId>
  <version>0.2.0</version>
</dependency>
```

`WireGoldenTest` pins the canonical CBOR bytes of every record in
`wire-golden.properties`, shipped inside the jar so the consumers' suites
read the same vectors from the classpath. After a deliberate record change, regenerate with
`mvn test -Dwire.golden.write=true` and bump the version. `mvn clean install`
builds it; both consuming projects resolve it from the local repository (or
Maven Central once published).

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
