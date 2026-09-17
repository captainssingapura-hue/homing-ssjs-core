package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * CSS cascade layer — a typed "tier" carried as a marker interface on
 * {@link CssClass} (via {@link InLayer}).
 *
 * <p>The framework serves CSS rules grouped by Layer, wrapped in
 * {@code @layer reset, layout, component, prose, state, media, theme;}
 * declarations. Rules in a later layer win the cascade over rules in an
 * earlier layer at any specificity — which makes cross-bundle authoring
 * deterministic regardless of stylesheet load order. See
 * <i>Defect 0003 — Two-Bundle CSS Cascade</i> for the motivating problem.</p>
 *
 * <p>Sealed; the 7 permitted sub-interfaces are the framework's canonical
 * ladder. Downstream consumers don't add new layers — that's a deliberate
 * constraint to keep the cascade reasoning bounded.</p>
 */
public sealed interface Layer
        extends StatelessFunctionalObject
        permits Reset, Layout, Component, Prose, State, MediaGated, ThemeOverlay {}
