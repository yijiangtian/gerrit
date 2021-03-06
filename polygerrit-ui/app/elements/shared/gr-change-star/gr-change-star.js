/**
 * @license
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import '../gr-icons/gr-icons.js';
import '../../../styles/shared-styles.js';
import {GestureEventListeners} from '@polymer/polymer/lib/mixins/gesture-event-listeners.js';
import {LegacyElementMixin} from '@polymer/polymer/lib/legacy/legacy-element-mixin.js';
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {htmlTemplate} from './gr-change-star_html.js';

/** @extends PolymerElement */
class GrChangeStar extends GestureEventListeners(
    LegacyElementMixin(
        PolymerElement)) {
  static get template() { return htmlTemplate; }

  static get is() { return 'gr-change-star'; }
  /**
   * Fired when star state is toggled.
   *
   * @event toggle-star
   */

  static get properties() {
    return {
    /** @type {?} */
      change: {
        type: Object,
        notify: true,
      },
    };
  }

  _computeStarClass(starred) {
    return starred ? 'active' : '';
  }

  _computeStarIcon(starred) {
    // Hollow star is used to indicate inactive state.
    return `gr-icons:star${starred ? '' : '-border'}`;
  }

  _computeAriaLabel(starred) {
    return starred ? 'Unstar this change' : 'Star this change';
  }

  toggleStar() {
    const newVal = !this.change.starred;
    this.set('change.starred', newVal);
    this.dispatchEvent(new CustomEvent('toggle-star', {
      bubbles: true,
      composed: true,
      detail: {change: this.change, starred: newVal},
    }));
  }
}

customElements.define(GrChangeStar.is, GrChangeStar);
