/**
 * _DomOpsPartyBase.js
 *
 * Internal base class shared by all DomOpsParty level types.
 * Not part of the public API — import from DomOpsParty.js instead.
 *
 * Each party node:
 *  - Has a name and depth.
 *  - Owns a set of named DOM elements created via createElement().
 *    This is the only sanctioned way to create DOM elements — no component
 *    may call document.createElement() directly.
 *  - Can have named child branches created via createBranch() overrides.
 *
 * dissolve() — integrated teardown
 * ─────────────────────────────────
 * branch.dissolve() is the single-call teardown for any branch. It:
 *  1. Recursively dissolves all sub-branches (depth-first).
 *  2. Calls _releaseAll() on every dissolved node — removes each owned
 *     DOM element from the document and clears the element map.
 *  3. Removes this branch from its parent's branch map.
 *
 * After dissolve(), the entire DOM subtree and party subtree rooted here
 * are clean. No manual element removal is needed.
 */

const VALID_NAME = /^[a-zA-Z0-9_-]+$/;

// `export { _DomOpsPartyBase };` is appended by the framework's EsModuleWriter.
// `import { ... }` lines are generated from the Java EsModule's ImportsFor.
class _DomOpsPartyBase {
  /** @type {string} */
  #name;

  /** @type {number} */
  #depth;

  /** @type {(() => void)|null} — callback to remove this branch from its parent's map */
  #deregister;

  /** @type {Map<string, HTMLElement>} — elements owned by this branch */
  #elements;

  /** @type {Map<string, _DomOpsPartyBase>} */
  #branches;

  /** @type {WeakRef|null} — weak reference to the object that owns this branch */
  #ownerRef = null;

  /**
   * @type {string|null} — String(owner), captured at activate(). A plain
   * string outlives the owner it names, which is the point: a LEAKED branch
   * — owner registered, since collected — is exactly the one whose owner
   * can no longer be asked, and exactly the one whose name matters most.
   */
  #ownerLabel = null;

  /**
   * @param {string}         name       - Branch label. [a-zA-Z0-9_-]+
   * @param {number}         depth      - Distance from root (0 = root, 18 = max).
   * @param {(() => void)|null} [deregister=null] - Callback to remove this
   *        branch from its parent's map. Supplied by _addBranch(); null for root.
   */
  constructor(name, depth, deregister = null) {
    if (typeof name !== 'string' || name.trim() === '') {
      throw new TypeError(
        `[DomOpsParty] name must be a non-empty string. Received: ${JSON.stringify(name)}`
      );
    }
    if (!VALID_NAME.test(name)) {
      throw new RangeError(
        `[DomOpsParty] name "${name}" contains invalid characters. ` +
        `Only letters, digits, underscores, and hyphens are allowed.`
      );
    }

    this.#name       = name;
    this.#depth      = depth;
    this.#deregister = deregister;
    this.#elements   = new Map();
    this.#branches   = new Map();
  }

  // ── Getters ───────────────────────────────────────────────────────────────

  /**
   * Whether the registered owner of this branch is still alive.
   *
   * Returns:
   *   null  — no owner registered (root or unowned branch)
   *   true  — owner object is reachable (not a leak)
   *   false — owner has been garbage-collected → this branch is leaked
   *
   * @returns {boolean|null}
   */
  get isOwnerAlive() {
    if (this.#ownerRef === null) return null;
    return this.#ownerRef.deref() !== undefined;
  }

  /** Human-readable label for this party node. @returns {string} */
  get name()  { return this.#name; }

  /** Distance from the root singleton (0 = root, 18 = deepest). @returns {number} */
  get depth() { return this.#depth; }

  // ── Activation gate ───────────────────────────────────────────────────────

  /**
   * Throws if this branch has not been activated via activate(owner).
   * Called by createElement() and createBranch() to enforce the rule that
   * every branch must have an owner before it can do work.
   */
  #assertActivated() {
    if (this.#ownerRef === null) {
      throw new Error(
        `[DomOpsParty] Branch "${this.#name}" has not been activated. ` +
        `Call branch.activate(owner) before using it.`
      );
    }
  }

  // ── Element management ────────────────────────────────────────────────────

  /**
   * Creates a DOM element, registers it under this branch, and returns it.
   *
   * This is the only sanctioned way to create DOM elements in the application.
   * Components must never call document.createElement() directly.
   *
   * @param {string} name    - Unique name within this branch. [a-zA-Z0-9_-]+
   * @param {string} tagName - Valid HTML tag name.
   * @returns {HTMLElement}
   *
   * @throws {Error}      If the branch has not been activated.
   * @throws {TypeError}  If name or tagName are not non-empty strings, or name
   *                      contains invalid characters.
   * @throws {RangeError} If name is already in use on this branch.
   */
  createElement(name, tagName) {
    this.#assertActivated();
    if (typeof name !== 'string' || name.trim() === '') {
      throw new TypeError(
        `[DomOpsParty] createElement: name must be a non-empty string. ` +
        `Received: ${JSON.stringify(name)}`
      );
    }
    if (!VALID_NAME.test(name)) {
      throw new RangeError(
        `[DomOpsParty] createElement: name "${name}" contains invalid characters. ` +
        `Only letters, digits, underscores, and hyphens are allowed.`
      );
    }
    if (this.#elements.has(name)) {
      throw new RangeError(
        `[DomOpsParty] createElement: name "${name}" is already in use ` +
        `on branch "${this.#name}".`
      );
    }
    if (typeof tagName !== 'string' || tagName.trim() === '') {
      throw new TypeError(
        `[DomOpsParty] createElement: tagName must be a non-empty string. ` +
        `Received: ${JSON.stringify(tagName)}`
      );
    }

    const el = document.createElement(tagName);
    this.#elements.set(name, el);
    return el;
  }

  /**
   * Returns a previously created element by name, or null if not found.
   *
   * @param {string} name
   * @returns {HTMLElement|null}
   */
  getElement(name) {
    return this.#elements.get(name) ?? null;
  }

  /**
   * Descriptors for all elements owned by this branch (excludes sub-branches).
   *
   * @returns {{ name: string, tagName: string }[]}
   */
  listElements() {
    return [...this.#elements.entries()].map(([name, el]) => ({
      name,
      tagName: el.tagName.toLowerCase(),
    }));
  }

  /** Number of elements directly owned by this branch. @returns {number} */
  get elementCount() { return this.#elements.size; }

  // ── Branches ──────────────────────────────────────────────────────────────

  /** Number of direct child branches. @returns {number} */
  get branchCount() { return this.#branches.size; }

  /**
   * Creates a named child branch at depth + 1.
   * Each level class overrides this to return the next concrete level type.
   * DomOpsPartyL18 does not override — this default always throws.
   *
   * @param {string} name
   * @throws {RangeError} Always — maximum depth (18) has been reached.
   */
  createBranch(name) {
    throw new RangeError(
      `[DomOpsParty] createBranch: Maximum branch depth (18) reached. ` +
      `Cannot create branch "${name}".`
    );
  }

  /**
   * Returns the named child branch, or null if it does not exist.
   *
   * @param {string} name
   * @returns {_DomOpsPartyBase|null}
   */
  getBranch(name) {
    return this.#branches.get(name) ?? null;
  }

  /** @param {string} name @returns {boolean} */
  hasBranch(name) {
    return this.#branches.has(name);
  }

  /** Names of all direct child branches. @returns {string[]} */
  listBranches() {
    return [...this.#branches.keys()];
  }

  /**
   * Recursively dissolves the named child branch: releases all elements in
   * the subtree (depth-first), clears all its sub-branches, then removes it
   * from this node's branch map.
   *
   * @param {string} name
   * @throws {ReferenceError} If no branch with that name exists.
   */
  dissolveBranch(name) {
    const branch = this.#branches.get(name);
    if (!branch) {
      throw new ReferenceError(
        `[DomOpsParty] dissolveBranch: No branch named "${name}" found at this level.`
      );
    }
    branch._dissolveTree();
    this.#branches.delete(name);
  }

  /**
   * Dissolves this branch: recursively releases all elements in the subtree,
   * clears all sub-branches, then removes this node from its parent.
   *
   * This is the preferred single-call teardown. After dissolve(), null your
   * branch reference — the object must not be used again.
   */
  dissolve() {
    this._dissolveTree();
    this.#deregister?.();
  }

  // ── Inspection ────────────────────────────────────────────────────────────

  toString() {
    return `DomOpsParty("${this.#name}", depth=${this.#depth}, ` +
           `elements=${this.#elements.size}, branches=${this.#branches.size})`;
  }

  /**
   * The owner's label — `String(owner)` as captured at activate() — or null
   * when this branch was never activated. RFC 0063 D2: a label, never the
   * handle. A string cannot dissolve anything; the WeakRef stays private.
   *
   * Captured rather than derived so that it survives the owner: a leaked
   * branch is precisely one whose owner can no longer be dereferenced, and
   * precisely the one you want named.
   *
   * @returns {string|null}
   */
  get ownerLabel() { return this.#ownerLabel; }

  /**
   * A frozen, plain-data projection of this branch and everything under it —
   * RFC 0063's observation by construction.
   *
   * Nothing in the result is a function, and nothing in it is a live branch
   * or element, so a holder of a snapshot can inspect the tree and cannot
   * touch it. That is the whole contract: a monitor inside the tree it
   * renders must not hold the handle that could dissolve its own host.
   *
   * `path` is computed by this walk — relative to the node it is called on,
   * absolute from the root. A branch does not store its own path; being
   * addressable from outside is a capability the party has never granted.
   *
   * `ownerAlive === false` is a leak: an owner that was registered and has
   * since been collected. Detection depends on the engine having run GC, so
   * `true` means "not collected yet", not "not leaked".
   *
   * @returns {Readonly<{
   *   name: string, depth: number, path: readonly string[],
   *   owner: string|null, ownerAlive: boolean|null,
   *   elements: readonly {name: string, tagName: string}[],
   *   branches: readonly object[]
   * }>}
   */
  snapshot() { return this.#snapshotAt([]); }

  /** The walk behind snapshot(); `above` is the path to this node's parent. */
  #snapshotAt(above) {
    const path = Object.freeze([...above, this.#name]);
    const elements = Object.freeze(
      this.listElements().map(e => Object.freeze({ name: e.name, tagName: e.tagName })));
    const branches = Object.freeze(
      [...this.#branches.values()].map(b => b.#snapshotAt(path)));
    return Object.freeze({
      name:       this.#name,
      depth:      this.#depth,
      path,
      owner:      this.#ownerLabel,
      ownerAlive: this.isOwnerAlive,
      elements,
      branches,
    });
  }

  // ── Protected helpers — for subclass createBranch overrides only ──────────

  /**
   * Depth-first recursive teardown used by both dissolveBranch() and dissolve().
   * For every node in the subtree rooted here:
   *  - Recursively calls _dissolveTree() on each child branch.
   *  - Calls _releaseAll() to remove all owned elements from the DOM and
   *    clear the element map.
   *  - Clears the branch map of this node.
   *
   * Does NOT remove this node from its parent — that is the caller's job.
   */
  _dissolveTree() {
    for (const branch of this.#branches.values()) {
      branch._dissolveTree();
    }
    this.#branches.clear();
    this._releaseAll();
  }

  /**
   * Removes all owned DOM elements from the document and clears the element map.
   * Called by _dissolveTree(). Safe to call on elements already detached from DOM.
   */
  _releaseAll() {
    for (const el of this.#elements.values()) {
      el.remove();
    }
    this.#elements.clear();
  }

  /**
   * Validates a branch name and asserts it is not already in use at this level.
   *
   * @param {string} name
   * @throws {TypeError}  If name is not a non-empty string.
   * @throws {RangeError} If name contains invalid characters or already exists.
   */
  _validateBranchName(name) {
    this.#assertActivated();
    if (typeof name !== 'string' || name.trim() === '') {
      throw new TypeError(
        `[DomOpsParty] createBranch: Branch name must be a non-empty string. ` +
        `Received: ${JSON.stringify(name)}`
      );
    }
    if (!VALID_NAME.test(name)) {
      throw new RangeError(
        `[DomOpsParty] createBranch: Branch name "${name}" contains invalid characters. ` +
        `Only letters, digits, underscores, and hyphens are allowed.`
      );
    }
    if (this.#branches.has(name)) {
      throw new RangeError(
        `[DomOpsParty] createBranch: A branch named "${name}" already exists at this level.`
      );
    }
  }

  /**
   * Binds an owner to this branch (set-once).
   *
   * Intended call site: the component constructor, immediately after
   * receiving the branch from its parent:
   *
   *   this.#branch = parentBranch.createBranch(id);
   *   this.#branch.activate(this);
   *
   * Once activated, isOwnerAlive tracks the owner via WeakRef.
   * Calling activate() a second time throws — ownership is immutable.
   *
   * Activation is optional: root and utility branches that have no
   * component owner work fine without it.
   *
   * @param {object} owner - The component responsible for this branch.
   * @throws {Error} If the branch has already been activated.
   */
  activate(owner) {
    if (this.#ownerRef !== null) {
      throw new Error(
        `[DomOpsParty] activate: Branch "${this.#name}" is already activated.`
      );
    }
    this.#ownerRef = new WeakRef(owner);
    this.#ownerLabel = String(owner);
  }

  /**
   * Constructs a child branch with a deregister closure baked into its
   * constructor, registers it in this node's branch map, and returns it.
   * Call after _validateBranchName.
   *
   * The returned branch is unactivated. The caller (or the component that
   * receives the branch) should call branch.activate(owner) to bind an owner.
   *
   * @param {string}                          name
   * @param {new (name: string, deregister: () => void) => _DomOpsPartyBase} BranchClass
   * @returns {_DomOpsPartyBase}
   */
  _addBranch(name, BranchClass) {
    const branches = this.#branches;
    const branch = new BranchClass(name, () => branches.delete(name));
    branches.set(name, branch);
    return branch;
  }
}
