package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.ModuleForm;
import hue.captains.singapura.js.homing.tree.RowDisplay;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * What a vertex of the crate tree IS (RFC 0053) — the crate family's answer,
 * the way {@code ListingDetails} is the catalogue's.
 *
 * <p>A crate tree is isomorphic to a catalogue — branches and leaves, each with
 * an identity and a position — but its content is a different kind of thing, so
 * it brings its own answer rather than borrowing one. It is emphatically NOT
 * promoted to a {@code Catalogue}: that would push every Java package through
 * {@code Entry} and hand it a URL position under Law 1, and a package is not a
 * navigable. Same shape, different kind.</p>
 *
 * <h2>What this replaces</h2>
 *
 * <p>Four stringly dimensions, three of them misused. The old builder said so
 * itself: <i>"kind = the mechanical ModuleForm; summary = FQCN (a detail widget
 * reads it)"</i>. So {@code Summary} carried a fully-qualified class name,
 * {@code Kind} carried an enum that had been flattened to a string, and
 * {@code Category} carried {@code ""} on every node of every kind, present only
 * because the sealed vocabulary demanded it.</p>
 *
 * <p>Here {@link ModuleForm} is an enum again, a module count is an {@code int}
 * rather than the pre-rendered {@code "3 modules"} the server used to bake into
 * a field named summary, and a crate's requires list is a list. Presentation
 * happens in {@link #row()} — once, at the edge — instead of being smeared
 * through the builder.</p>
 *
 * @since RFC 0053
 */
public sealed interface CrateDetails extends ValueObject {

    /** Narrowed to what a tree row draws. The only place strings get rendered. */
    RowDisplay row();

    /** Counting, said properly — trivial now that a count is an int and not prose. */
    static String plural(int n, String noun) {
        return n + " " + noun + (n == 1 ? "" : "s");
    }

    /** The forest root: every owned crate, counted. */
    record OfWorkspace(int crateCount, int moduleCount) implements CrateDetails {
        @Override public RowDisplay row() {
            return new RowDisplay("Crates", "",
                    plural(crateCount, "crate") + " · " + plural(moduleCount, "module"),
                    "workspace");
        }
    }

    /** One owned crate, with the package prefix its modules share. */
    record OfCrate(String name, int moduleCount, String packagePrefix, List<String> requires)
            implements CrateDetails {
        public OfCrate {
            Objects.requireNonNull(name, "CrateDetails.OfCrate.name");
            packagePrefix = packagePrefix == null ? "" : packagePrefix;
            requires = List.copyOf(requires);
        }
        @Override public RowDisplay row() {
            String reqs = requires.isEmpty() ? "no requires"
                    : "requires " + String.join(", ", requires);
            String pkg = packagePrefix.isEmpty() ? "" : packagePrefix + ".* · ";
            return new RowDisplay(name, "", plural(moduleCount, "module") + " · " + pkg + reqs, "crate");
        }
    }

    /** A package node — structure only; it owns no module of its own. */
    record OfPackage(String label, int moduleCount) implements CrateDetails {
        public OfPackage {
            Objects.requireNonNull(label, "CrateDetails.OfPackage.label");
        }
        @Override public RowDisplay row() {
            return new RowDisplay(label, "", plural(moduleCount, "module"), "package");
        }
    }

    /**
     * A module leaf. The FQCN is a NAMED field here; four conformance widgets
     * read it, one of them as a map key, and until now they read it out of a
     * field called {@code summary}.
     */
    record OfModule(String simpleName, ModuleForm form, String fqcn) implements CrateDetails {
        public OfModule {
            Objects.requireNonNull(simpleName, "CrateDetails.OfModule.simpleName");
            Objects.requireNonNull(form,       "CrateDetails.OfModule.form");
            Objects.requireNonNull(fqcn,       "CrateDetails.OfModule.fqcn");
        }
        /** The wire tag for a form — lowercase, hyphenated. */
        public String formTag() { return form.name().toLowerCase().replace('_', '-'); }
        @Override public RowDisplay row() {
            return new RowDisplay(simpleName, "", fqcn, formTag());
        }
    }
}
