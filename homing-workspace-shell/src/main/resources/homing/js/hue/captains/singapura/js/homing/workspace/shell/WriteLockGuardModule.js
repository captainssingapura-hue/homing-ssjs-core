// =============================================================================
// WriteLockGuard + ReadOnlyOverlay — Phase 7 of the workspace-shell chrome.
//
// WriteLockGuard.attach(opts) returns a Promise<controller>. Controller:
//   isHeld()                 — current state
//   takeOver()→Promise<bool> — re-request with steal:true
//   release()                — voluntarily yield the lock
//   inspect()                — state snapshot
//
// Side effects of state changes (managed inside the controller):
//   recorder.setWriteLockHeld(b)   — Phase 6 fence flip
//   overlay.show() / overlay.hide() — mask + banner DOM lifecycle
//   onChange(b)                     — passthrough hook (Phase 16 will use)
//
// Lock semantics (Web Locks API):
//   Boot     → request {mode:'exclusive', ifAvailable:true}.
//                acquired → held, hold via pending Promise.
//                missed   → read-only, no further auto-retry.
//   Take over → request {mode:'exclusive', steal:true}.
//                success → held, overlay.hide().
//                fail    → stay read-only.
//   Stolen   → handled inside the held promise's .catch; flips to read-only.
//
// Explicit Substrate doctrine:
//   - INSTANCE singleton + per-call attach returning a per-workspace controller.
//   - No async sugar (naive Promise chains).
//   - ALL collaborators in constructor: _navigatorLocks, _OverlayCtor.
// =============================================================================

class ReadOnlyOverlay {

    constructor(deps) {
        deps = deps || {};
        // Document — defaulted; tests inject a stub.
        this._document = deps.document ||
            (typeof document !== 'undefined' ? document : null);
        this._mask    = null;
        this._banner  = null;
        this._host    = null;
        this._workspaceName = null;
        this._onTakeOver    = null;
    }

    /**
     * Mount the mask + banner over the host. Idempotent (calling twice
     * does NOT double-mount). workspaceName + onTakeOver are captured
     * per mount so the banner can show the right name and wire the
     * Take Over button to the right callback.
     */
    mount(host, workspaceName, onTakeOver) {
        if (this._mask) return;   // idempotent
        if (!host)      throw new Error('[ReadOnlyOverlay] host required');
        if (!this._document) {
            console.warn('[ReadOnlyOverlay] no document — skipping mount');
            return;
        }
        this._host          = host;
        this._workspaceName = workspaceName || 'workspace';
        this._onTakeOver    = onTakeOver || function () { return Promise.resolve(false); };
        this._mask   = this._buildMask();
        this._banner = this._buildBanner();
        host.appendChild(this._mask);
        host.appendChild(this._banner);
    }

    /** Tear down both the mask and the banner. Idempotent. */
    unmount() {
        if (this._mask && this._mask.parentNode) {
            this._mask.parentNode.removeChild(this._mask);
        }
        if (this._banner && this._banner.parentNode) {
            this._banner.parentNode.removeChild(this._banner);
        }
        this._mask          = null;
        this._banner        = null;
        this._host          = null;
        this._workspaceName = null;
        this._onTakeOver    = null;
    }

    isMounted() { return !!this._mask; }

    /**
     * Click/key/wheel-blocking mask. Single transparent absorber over
     * the host (z:40); badge (z:50) and banner (z:55) sit above so the
     * Take Over button stays clickable.
     */
    _buildMask() {
        const m = this._document.createElement('div');
        m.setAttribute('data-readonly-mask', '1');
        m.style.cssText =
            'position:absolute;inset:0;z-index:40;'
          + 'background:rgba(20,8,8,0.22);'
          + 'cursor:not-allowed;pointer-events:all;';
        const blockedEvents = [
            'click', 'mousedown', 'mouseup', 'dblclick', 'contextmenu',
            'keydown', 'keyup', 'keypress', 'wheel', 'touchstart', 'touchmove'
        ];
        for (const t of blockedEvents) {
            m.addEventListener(t, function (ev) {
                ev.preventDefault();
                ev.stopPropagation();
            }, true);
        }
        return m;
    }

    /** Top banner with Take Over + Reload. */
    _buildBanner() {
        const self   = this;
        const banner = this._document.createElement('div');
        banner.style.cssText =
            'position:absolute;top:0;left:0;right:0;z-index:55;'
          + 'padding:8px 14px;background:#7a3010;color:#fff;'
          + 'font:12px/1.4 system-ui, sans-serif;'
          + 'display:flex;align-items:center;gap:12px;'
          + 'border-bottom:1px solid #b04820;box-sizing:border-box;'
          + 'box-shadow:0 2px 6px rgba(0,0,0,0.4);';

        const icon = this._document.createElement('span');
        icon.textContent     = '🔒';
        icon.style.cssText   = 'font-size:14px;';
        banner.appendChild(icon);

        const msg = this._document.createElement('span');
        msg.style.flex = '1';
        const strong = this._document.createElement('b');
        strong.textContent = 'Read-only — ';
        msg.appendChild(strong);
        msg.appendChild(this._document.createTextNode(
            'this workspace ("' + this._workspaceName + '") is open in another window. '
          + 'Changes here will not be saved.'));
        banner.appendChild(msg);

        const takeBtn = this._document.createElement('button');
        takeBtn.textContent = 'Take over';
        takeBtn.style.cssText =
            'padding:4px 12px;background:#fff;color:#7a3010;'
          + 'border:none;border-radius:3px;cursor:pointer;'
          + 'font:11px system-ui;font-weight:600;';
        takeBtn.addEventListener('click', function () {
            takeBtn.disabled    = true;
            takeBtn.textContent = '…';
            self._onTakeOver().then(function (got) {
                if (!got) {
                    takeBtn.disabled    = false;
                    takeBtn.textContent = 'Take over';
                }
                // On success the controller will call overlay.unmount()
                // so we don't need to reset the button — banner is gone.
            });
        });
        banner.appendChild(takeBtn);

        const reloadBtn = this._document.createElement('button');
        reloadBtn.textContent = 'Reload';
        reloadBtn.style.cssText =
            'padding:4px 12px;background:transparent;color:#fff;'
          + 'border:1px solid #fff;border-radius:3px;cursor:pointer;'
          + 'font:11px system-ui;';
        reloadBtn.addEventListener('click', function () {
            if (typeof location !== 'undefined') location.reload();
        });
        banner.appendChild(reloadBtn);

        return banner;
    }
}

class WriteLockGuard {

    constructor(deps) {
        deps = deps || {};
        // Web Locks API access — held as a dep so tests inject a stub.
        // Default reads navigator.locks if present; otherwise null and
        // the guard degrades to writer-mode silently.
        this._navigatorLocks = deps.navigatorLocks
            || ((typeof navigator !== 'undefined' && navigator.locks)
                ? navigator.locks : null);
        this._OverlayCtor = deps.OverlayCtor || ReadOnlyOverlay;
    }

    /**
     * Acquire the workspace's write lock and return a controller that
     * tracks lock state + drives the overlay. Returns Promise<ctrl>
     * resolved AFTER the initial acquire attempt.
     */
    attach(opts) {
        if (!opts)               throw new Error('[WriteLockGuard] opts required');
        if (!opts.lockName)      throw new Error('[WriteLockGuard] opts.lockName required');
        if (!opts.host)          throw new Error('[WriteLockGuard] opts.host required');
        if (!opts.workspaceName) throw new Error('[WriteLockGuard] opts.workspaceName required');
        if (!opts.recorder)      throw new Error('[WriteLockGuard] opts.recorder required');
        const self = this;
        const overlay = new this._OverlayCtor();

        // Controller state — held in a closure so the methods we return
        // see the live values.
        const state = {
            held:     false,
            release:  null
        };

        function applyHeld(held, reason) {
            state.held = held;
            try { opts.recorder.setWriteLockHeld(held); }
            catch (e) { console.error('[WriteLockGuard] recorder fence flip threw:', e); }
            if (held) {
                if (overlay.isMounted()) overlay.unmount();
            } else {
                if (!overlay.isMounted()) {
                    overlay.mount(opts.host, opts.workspaceName, function () {
                        return controller.takeOver();
                    });
                }
            }
            try { if (opts.onChange) opts.onChange(held, reason); }
            catch (e) { console.error('[WriteLockGuard] onChange threw:', e); }
        }

        function requestLock(mode) {
            // mode: 'ifAvailable' | 'steal'
            const locksApi = self._navigatorLocks;
            if (!locksApi || typeof locksApi.request !== 'function') {
                // No Web Locks support → degrade to writer-mode silently.
                applyHeld(true, 'no-locks-api');
                return Promise.resolve(true);
            }
            const reqOpts = (mode === 'steal')
                ? { mode: 'exclusive', steal: true }
                : { mode: 'exclusive', ifAvailable: true };
            return new Promise(function (resolveOuter) {
                let acquired = false;
                locksApi.request(opts.lockName, reqOpts, function (lock) {
                    if (!lock) {
                        // ifAvailable miss.
                        resolveOuter(false);
                        return;
                    }
                    acquired = true;
                    applyHeld(true, mode);
                    resolveOuter(true);
                    // Hold the lock until externally released. Returning
                    // a pending Promise keeps the lock held; resolving
                    // it via state.release yields voluntarily.
                    return new Promise(function (release) { state.release = release; });
                }).catch(function (err) {
                    if (acquired) {
                        // Lock stolen by another tab.
                        console.warn('[WriteLockGuard] write lock lost '
                                   + '(stolen by another tab):',
                            err && err.name ? err.name : err);
                        state.release = null;
                        applyHeld(false, 'stolen');
                    } else {
                        console.warn('[WriteLockGuard] write lock request errored:', err);
                        resolveOuter(false);
                    }
                });
            });
        }

        const controller = {
            isHeld:   function () { return state.held; },
            takeOver: function () { return requestLock('steal'); },
            release:  function () {
                if (typeof state.release === 'function') {
                    try { state.release(); } catch (e) {}
                    state.release = null;
                }
                applyHeld(false, 'released');
            },
            inspect:  function () {
                return {
                    held:           state.held,
                    overlayMounted: overlay.isMounted(),
                    hasLocksApi:    !!self._navigatorLocks
                };
            }
        };

        // Voluntary release on pagehide. Browser will eventually
        // release auto-held locks on full teardown, but that races the
        // next page's boot — any same-tab reload (theme change, F5,
        // navigation) lands the new page BEFORE the lock is free, and
        // the new page enters read-only mode for no reason. Releasing
        // at unload guarantees the lock is free by the time the new
        // page calls request(). Cross-tab protection unaffected — a
        // separate live tab still holds its own lock.
        if (typeof window !== 'undefined'
            && typeof window.addEventListener === 'function') {
            const releaseOnUnload = function () {
                if (state.release) {
                    try { state.release(); } catch (e) {}
                    state.release = null;
                    state.held = false;
                }
            };
            window.addEventListener('pagehide', releaseOnUnload);
            // beforeunload as belt-and-braces for browsers that fire
            // only one of the two reliably.
            window.addEventListener('beforeunload', releaseOnUnload);
        }

        return requestLock('ifAvailable').then(function (gotIt) {
            if (!gotIt) applyHeld(false, 'unavailable-at-boot');
            return controller;
        });
    }
}

WriteLockGuard.INSTANCE = new WriteLockGuard();
