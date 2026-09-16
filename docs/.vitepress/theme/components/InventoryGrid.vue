<script setup lang="ts">
import { computed, ref } from 'vue'
import { describeRule, displayStacks, type Group, type Rule } from '../restricted'
import ItemSlot from './ItemSlot.vue'

const props = withDefaults(
  defineProps<{
    /** A `restrictedSlots` map, exactly as written in the config. */
    slots?: Record<string, Rule>
    /** The `groups` map, for slots restricted to a group. */
    groups?: Record<string, Group>
    title?: string
    /** Start with slot numbers visible. */
    indices?: boolean
  }>(),
  { slots: () => ({}), groups: () => ({}), title: undefined, indices: false },
)

const showIndices = ref(props.indices)
const active = ref<number | null>(null)

const rows = [
  Array.from({ length: 9 }, (_, i) => 9 + i),
  Array.from({ length: 9 }, (_, i) => 18 + i),
  Array.from({ length: 9 }, (_, i) => 27 + i),
]
const hotbar = Array.from({ length: 9 }, (_, i) => i)

const rule = (slot: number): Rule | undefined => props.slots[String(slot)]
const stacks = (slot: number) => {
  const value = rule(slot)
  return value === undefined ? null : displayStacks(value, props.groups)
}
const tip = (slot: number) => {
  const value = rule(slot)
  return value === undefined ? `Slot ${slot}: unrestricted` : `Slot ${slot}: ${describeRule(value)}`
}

const restrictedCount = computed(() => Object.keys(props.slots).length)
const detail = computed(() => (active.value === null ? null : tip(active.value)))
</script>

<template>
  <figure class="ri-inventory">
    <div class="ri-panel">
      <div class="ri-head">
        <span class="ri-title">{{ title ?? 'Inventory' }}</span>
        <button type="button" class="ri-toggle" :aria-pressed="showIndices" @click="showIndices = !showIndices">
          <kbd>Tab</kbd> {{ showIndices ? 'Hide' : 'Show' }} slot numbers
        </button>
      </div>
      <div class="ri-rows">
        <div v-for="(row, r) in rows" :key="r" class="ri-row">
          <span
            v-for="slot in row"
            :key="slot"
            class="ri-cell"
            :class="{ restricted: rule(slot) !== undefined, active: active === slot }"
            tabindex="0"
            @mouseenter="active = slot"
            @focus="active = slot"
            @mouseleave="active = null"
            @blur="active = null"
          >
            <ItemSlot :stacks="stacks(slot) ?? []" ghost :index="showIndices ? slot : null" :tip="tip(slot)" />
          </span>
        </div>
        <div class="ri-row ri-hotbar">
          <span
            v-for="slot in hotbar"
            :key="slot"
            class="ri-cell"
            :class="{ restricted: rule(slot) !== undefined, active: active === slot }"
            tabindex="0"
            @mouseenter="active = slot"
            @focus="active = slot"
            @mouseleave="active = null"
            @blur="active = null"
          >
            <ItemSlot :stacks="stacks(slot) ?? []" ghost :index="showIndices ? slot : null" :tip="tip(slot)" />
          </span>
        </div>
      </div>
    </div>
    <figcaption class="ri-caption">
      <code v-if="detail">{{ detail }}</code>
      <span v-else-if="restrictedCount">
        {{ restrictedCount }} restricted {{ restrictedCount === 1 ? 'slot' : 'slots' }}. Hover a slot to read its rule.
      </span>
      <span v-else>Hover a slot to read its number.</span>
    </figcaption>
  </figure>
</template>

<style scoped>
.ri-inventory {
  margin: 16px 0;
  max-width: 100%;
}

.ri-panel {
  display: inline-block;
  max-width: 100%;
  overflow-x: auto;
  padding: 8px 10px 10px;
  background: var(--ri-panel-bg);
  border: 3px solid;
  border-color: var(--ri-panel-light) var(--ri-panel-dark) var(--ri-panel-dark) var(--ri-panel-light);
  border-radius: 4px;
  box-shadow: 0 0 0 2px #000;
}

.ri-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.ri-title {
  font: 600 14px/1.2 var(--vp-font-family-base);
  color: #3f3f3f;
}

.ri-toggle {
  font-size: 12px;
  color: #3f3f3f;
  padding: 2px 8px;
  border: 1px solid #8b8b8b;
  border-radius: 4px;
  background: #d8d8d8;
}

.ri-toggle:hover {
  background: #e8e8e8;
}

.ri-toggle[aria-pressed='true'] {
  background: #fff;
  border-color: #3f3f3f;
}

.ri-toggle kbd {
  font: 600 11px/1 var(--vp-font-family-mono);
  padding: 1px 4px;
  border: 1px solid #8b8b8b;
  border-radius: 3px;
  background: #f4f4f4;
}

.ri-row {
  display: flex;
  width: max-content;
}

.ri-hotbar {
  margin-top: 8px;
}

.ri-cell {
  display: inline-flex;
  outline: none;
}

.ri-cell :deep(.ri-slot) {
  transition: box-shadow 0.1s;
}

.ri-cell.active :deep(.ri-slot) {
  box-shadow: inset 0 0 0 20px rgba(255, 255, 255, 0.35);
}

.ri-cell:focus-visible :deep(.ri-slot) {
  outline: 2px solid var(--vp-c-brand-1);
  outline-offset: -2px;
}

.ri-caption {
  margin-top: 6px;
  min-height: 1.6em;
  font-size: 13px;
  color: var(--vp-c-text-2);
}

.ri-caption code {
  font-size: 12px;
  word-break: break-all;
}
</style>
