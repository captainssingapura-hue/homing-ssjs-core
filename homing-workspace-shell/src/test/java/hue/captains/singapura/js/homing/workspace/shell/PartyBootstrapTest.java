package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code PartyBootstrap}. Exercises the pure synchronous
 * core {@code assemble(decls, modules, PartyCtor)} — the async wrapper
 * is a thin shell over it.
 *
 * <p>Style: fixtures are full JS literals (Java manages, JS specifies).
 * Diligent Secretaries: pure transform exercised in isolation with a
 * fake Party constructor + fake Secretary modules; no DOM, no Promises.</p>
 */
class PartyBootstrapTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PartyBootstrapModule.js";

    /** Stand-in Party — records joinActor calls; exposes state for assertions. */
    private static final String STUBS = """
            class Party {
                constructor(opts) {
                    this.name      = opts.name;
                    this.rootPath  = opts.root.path;
                    this.initial   = opts.root.initial;
                    this.behavior  = opts.root.behavior;
                    this.actorIds  = [];
                }
                joinActor(a) { this.actorIds.push(a.id); }
            }
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Each test builds a fresh PartyBootstrap with the stub Party injected
     *  via constructor — there's no more per-call opts.partyCtor override. */
    private Value partyBootstrap() {
        Value deps = js.eval("js", "({})");
        deps.putMember("PartyCtor", global("Party"));
        return global("PartyBootstrap").newInstance(deps);
    }

    @Test
    void emptyDeclsProducesEmptyResult() {
        Value result = partyBootstrap().invokeMember("assemble",
                js.eval("js", "[]"),
                js.eval("js", "[]"));
        assertEquals(0, result.getMember("parties").getMemberKeys().size());
        assertEquals(0, result.getMember("workspaceCtx").getMemberKeys().size());
    }

    @Test
    void singlePartyWithActorAndExposedAs() {
        Value decls = js.eval("js", """
                [{
                    name: 'animals',
                    secretaryModuleUrl: '/module?class=...AnimalsSecretaryModule',
                    secretaryExportName: 'AnimalsSecretary',
                    actors: [{ id: 'animals/ribbon-selector', parentSecretary: 'animals' }],
                    exposedAs: 'animalsParty'
                }]""");
        Value modules = js.eval("js", """
                [{
                    AnimalsSecretary: {
                        initial:  { selectedAnimal: '-1' },
                        behavior: (state, env) => ({ newState: state, actions: [] })
                    }
                }]""");

        Value result = partyBootstrap().invokeMember("assemble", decls, modules);

        Value parties = result.getMember("parties");
        Value ctx     = result.getMember("workspaceCtx");

        assertTrue(parties.getMemberKeys().contains("animals"));
        Value party = parties.getMember("animals");
        assertEquals("animals", party.getMember("name").asString());
        assertEquals("animals", party.getMember("rootPath").asString());
        assertEquals(1, party.getMember("actorIds").getArraySize());
        assertEquals("animals/ribbon-selector",
                     party.getMember("actorIds").getArrayElement(0).asString());

        assertTrue(ctx.getMemberKeys().contains("animalsParty"));
        assertEquals("animals",
                     ctx.getMember("animalsParty").getMember("name").asString());
    }

    @Test
    void missingSecretaryExportSkipsParty() {
        Value decls = js.eval("js", """
                [{
                    name: 'animals',
                    secretaryModuleUrl: '/module?class=...AnimalsSecretaryModule',
                    secretaryExportName: 'AnimalsSecretary',
                    actors: [],
                    exposedAs: 'animalsParty'
                }]""");
        Value modules = js.eval("js", "[{}]");   // module has no AnimalsSecretary export

        Value result = partyBootstrap().invokeMember("assemble", decls, modules);

        assertEquals(0, result.getMember("parties").getMemberKeys().size(),
                "missing export → no Party constructed");
        assertEquals(0, result.getMember("workspaceCtx").getMemberKeys().size());
    }

    @Test
    void secretaryMissingBehaviorSkipsParty() {
        Value decls = js.eval("js", """
                [{
                    name: 'animals',
                    secretaryModuleUrl: '/x',
                    secretaryExportName: 'AnimalsSecretary',
                    actors: [],
                    exposedAs: null
                }]""");
        // Export present but missing behavior.
        Value modules = js.eval("js", "[{ AnimalsSecretary: { initial: {} } }]");

        Value result = partyBootstrap().invokeMember("assemble", decls, modules);
        assertEquals(0, result.getMember("parties").getMemberKeys().size());
    }

    @Test
    void multiplePartiesIndependentExposeOnlyDeclaredOnes() {
        Value decls = js.eval("js", """
                [
                    { name: 'a', secretaryModuleUrl: '/a',
                      secretaryExportName: 'A', actors: [], exposedAs: 'aParty' },
                    { name: 'b', secretaryModuleUrl: '/b',
                      secretaryExportName: 'B', actors: [], exposedAs: null }
                ]""");
        Value modules = js.eval("js", """
                [
                    { A: { initial: 1, behavior: () => ({newState:1,actions:[]}) } },
                    { B: { initial: 2, behavior: () => ({newState:2,actions:[]}) } }
                ]""");

        Value result = partyBootstrap().invokeMember("assemble", decls, modules);

        assertEquals(2, result.getMember("parties").getMemberKeys().size());
        // Only 'a' declared exposedAs.
        assertEquals(1, result.getMember("workspaceCtx").getMemberKeys().size());
        assertTrue(result.getMember("workspaceCtx").getMemberKeys().contains("aParty"));
    }

    @Test
    void actorsJoinInDeclaredOrder() {
        Value decls = js.eval("js", """
                [{
                    name: 'p',
                    secretaryModuleUrl: '/x',
                    secretaryExportName: 'S',
                    actors: [
                        { id: 'p/first',  parentSecretary: 'p' },
                        { id: 'p/second', parentSecretary: 'p' },
                        { id: 'p/third',  parentSecretary: 'p' }
                    ],
                    exposedAs: null
                }]""");
        Value modules = js.eval("js", """
                [{ S: { initial: null, behavior: () => ({newState:null,actions:[]}) } }]""");

        Value result = partyBootstrap().invokeMember("assemble", decls, modules);
        Value actors = result.getMember("parties").getMember("p").getMember("actorIds");
        assertEquals(3, actors.getArraySize());
        assertEquals("p/first",  actors.getArrayElement(0).asString());
        assertEquals("p/second", actors.getArrayElement(1).asString());
        assertEquals("p/third",  actors.getArrayElement(2).asString());
    }
}
