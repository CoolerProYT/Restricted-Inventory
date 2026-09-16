<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { DRAWN_ICONS, itemIcon, itemName, TAG_MEMBERS, type Stack } from '../restricted'
import { useCycle } from '../cycle'

const props = withDefaults(
  defineProps<{
    /** Item id, or `#namespace:tag` to cycle through the tag's members. */
    id?: string | null
    /** Hosted variant render, such as `healing` or `sharpness_5`. */
    variant?: string
    /** Explicit stacks to cycle through; overrides `id`. */
    stacks?: Stack[] | null
    count?: number
    label?: boolean | string
    /** Draws the stack dimmed, the way an empty restricted slot previews it. */
    ghost?: boolean
    /** Slot index overlay, as shown while Tab is held. */
    index?: number | null
    tip?: string
  }>(),
  { id: null, variant: undefined, stacks: null, count: 1, label: false, ghost: false, index: null, tip: undefined },
)

const tick = useCycle()
const isTag = computed(() => !props.stacks && !!props.id?.startsWith('#'))

const cycle = computed<Stack[]>(() => {
  if (props.stacks) return props.stacks
  if (!props.id) return []
  if (isTag.value) return (TAG_MEMBERS[props.id] ?? []).map((id) => ({ id }))
  return [{ id: props.id, variant: props.variant }]
})

const current = computed(() => (cycle.value.length ? cycle.value[tick.value % cycle.value.length] : null))
const src = computed(() => (current.value ? itemIcon(current.value.id, current.value.variant) : null))
const drawn = computed(() => (current.value && DRAWN_ICONS.has(current.value.id) ? current.value.id : null))

const name = computed(() => {
  if (isTag.value) return props.id!
  return current.value ? itemName(current.value.id, current.value.variant) : ''
})
const labelText = computed(() => {
  if (typeof props.label === 'string') return props.label
  if (isTag.value) return itemName(props.id!)
  return props.stacks ? '' : name.value
})

// Falls back to initials when an item has no icon or the hosted icon fails to load.
const failed = ref(false)
watch(src, () => (failed.value = false))
const initials = computed(() =>
  name.value
    .replace(/^#/, '')
    .split(/[\s:_]+/)
    .filter((word) => /^[A-Za-z]/.test(word))
    .slice(-2)
    .map((word) => word[0].toUpperCase())
    .join(''),
)
</script>

<template>
  <span class="ri-item" :class="{ 'with-label': label }">
    <span class="ri-slot" :class="{ ghost }" :title="tip ?? name" :aria-label="tip ?? name" role="img">
      <svg v-if="drawn === 'minecraft:barrier'" class="ri-icon ri-barrier" viewBox="0 0 16 16" aria-hidden="true">
        <circle cx="8" cy="8" r="5.5" fill="none" stroke="#e02424" stroke-width="2" />
        <path d="M4.2 11.8 11.8 4.2" stroke="#e02424" stroke-width="2" />
      </svg>
      <img v-else-if="src && !failed" class="ri-icon pixelated" :src="src" alt="" loading="lazy" @error="failed = true" />
      <span v-else-if="current" class="ri-initials">{{ initials }}</span>
      <span v-if="isTag" class="ri-tag">#</span>
      <span v-if="count > 1" class="ri-count">{{ count }}</span>
      <span v-if="index !== null" class="ri-index">{{ index }}</span>
    </span>
    <span v-if="label && labelText" class="ri-label">{{ labelText }}</span>
  </span>
</template>

<style scoped>
.ri-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  vertical-align: middle;
}

.ri-slot {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex: none;
  background: var(--ri-slot-bg);
  border: 2px solid;
  border-color: var(--ri-slot-dark) var(--ri-slot-light) var(--ri-slot-light) var(--ri-slot-dark);
}

.ri-icon {
  width: 32px;
  height: 32px;
}

.ri-barrier {
  padding: 3px;
}

/* The mod's ghost preview: the stack under a translucent gray wash. */
.ghost .ri-icon,
.ghost .ri-initials {
  opacity: 0.55;
  filter: grayscale(0.35);
}

.ri-initials {
  font: 600 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

.ri-count,
.ri-tag,
.ri-index {
  position: absolute;
  font: 700 11px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
  pointer-events: none;
}

.ri-count {
  right: 1px;
  bottom: -1px;
}

.ri-tag {
  left: 1px;
  top: 1px;
  color: #ffdd55;
}

.ri-index {
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #ffff55;
  background: rgba(0, 0, 0, 0.35);
}

.ri-label {
  font-weight: 500;
}
</style>
