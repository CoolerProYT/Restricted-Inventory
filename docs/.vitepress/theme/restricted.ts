/** A slot rule as written in `restrictedSlots`. */
export type Rule =
  | string
  | {
      item?: string
      group?: string
      components?: Record<string, unknown>
      tag?: Record<string, unknown>
      nbt?: Record<string, unknown>
      display?: Rule
    }

/** A group as written in `groups`: a bare list of entries, or the object form with a group-wide display. */
export type Group = Rule[] | { entries: Rule[]; display?: Rule }

/** One stack to draw in a slot. */
export interface Stack {
  id: string
  /** Hosted variant render, such as `healing` for a potion or `sharpness_5` for an enchanted book. */
  variant?: string
}

/** Hosted renders of vanilla items, one PNG per item id. Mojang's textures are not bundled here. */
const VANILLA_ICONS = 'https://storage.googleapis.com/coolerpromc/textures'

/**
 * Members shown when a tag cycles, in the order the game lists them.
 * Only tags used on these pages are listed; anything else shows its first member or a tag badge.
 */
export const TAG_MEMBERS: Record<string, string[]> = {
  '#minecraft:swords': [
    'minecraft:wooden_sword',
    'minecraft:copper_sword',
    'minecraft:stone_sword',
    'minecraft:golden_sword',
    'minecraft:iron_sword',
    'minecraft:diamond_sword',
    'minecraft:netherite_sword',
  ],
  // Plain tipped arrows have no hosted render.
  '#minecraft:arrows': ['minecraft:arrow', 'minecraft:spectral_arrow'],
}

/** Names for items from other mods used in examples, which have no hosted icon. */
const NAMES: Record<string, string> = {
  'tacz:modern_kinetic_gun': 'Modern Kinetic Gun',
  'someflashlightmod:flashlight': 'Flashlight',
  'anothermod:torchlight': 'Torchlight',
}

/** Vanilla items that have no hosted render and are drawn by the page instead. */
export const DRAWN_ICONS = new Set(['minecraft:barrier'])

const titleCase = (path: string) =>
  path
    .split('_')
    .map((word) => (['of', 'the', 'and'].includes(word) ? word : word.charAt(0).toUpperCase() + word.slice(1)))
    .join(' ')

export function itemName(id: string, variant?: string): string {
  if (NAMES[id]) return NAMES[id]
  const path = id.replace(/^#/, '').split(':').pop() ?? id
  if (id.startsWith('#')) return `Any ${path.replace(/_/g, ' ')}`
  if (!variant) return titleCase(path)
  if (id === 'minecraft:enchanted_book') {
    const match = variant.match(/^(.*)_(\d+)$/)
    const level = match ? ` ${roman(Number(match[2]))}` : ''
    return `Enchanted Book (${titleCase(match ? match[1] : variant)}${level})`
  }
  // Potion variants: "Potion of Healing II", "Arrow of Swiftness (long)".
  const effect = titleCase(variant.replace(/^(long|strong)_/, ''))
  const modifier = variant.startsWith('long_') ? ' (long)' : variant.startsWith('strong_') ? ' II' : ''
  const noun = path === 'tipped_arrow' ? 'Arrow' : titleCase(path)
  return `${noun} of ${effect}${modifier}`
}

function roman(value: number): string {
  return ['', 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X'][value] ?? `${value}`
}

/** Hosted icon for a vanilla item, or null for items from other mods and items the page draws itself. */
export function itemIcon(id: string, variant?: string): string | null {
  if (DRAWN_ICONS.has(id)) return null
  const [namespace, path] = id.includes(':') ? id.split(':') : ['minecraft', id]
  if (namespace !== 'minecraft') return null
  return variant ? `${VANILLA_ICONS}/${namespace}/${path}/${variant}.png` : `${VANILLA_ICONS}/${namespace}/${path}.png`
}

const stripNamespace = (value: unknown) => (typeof value === 'string' ? value.replace(/^minecraft:/, '') : undefined)

/**
 * Picks the hosted variant render for a stack's components (1.21.1+) or NBT (1.20.1).
 * Potions and tipped arrows use their potion; enchanted books use their first stored enchantment.
 */
export function variantOf(data: Record<string, any> | undefined): string | undefined {
  if (!data) return undefined
  const potion = data['minecraft:potion_contents']?.potion ?? data.Potion
  if (potion) return stripNamespace(potion)
  const stored = data['minecraft:stored_enchantments']
  const levels = stored?.levels ?? stored
  if (levels && typeof levels === 'object') {
    const [first] = Object.entries(levels)
    if (first) return `${stripNamespace(first[0])}_${first[1]}`
  }
  const nbt = data.StoredEnchantments?.[0]
  if (nbt?.id) return `${stripNamespace(nbt.id)}_${nbt.lvl ?? 1}`
  return undefined
}

/** Stacks one rule entry can draw: a single stack, or every member of a tag. */
function entryStacks(entry: Rule): Stack[] {
  if (typeof entry === 'string') {
    return entry.startsWith('#') ? (TAG_MEMBERS[entry] ?? [entry]).map((id) => ({ id })) : [{ id: entry }]
  }
  if (!entry.item) return []
  const variant = variantOf(entry.components ?? entry.tag ?? entry.nbt)
  if (entry.item.startsWith('#')) return entryStacks(entry.item)
  return [{ id: entry.item, variant }]
}

/**
 * What a restricted slot draws, in the same order the mod picks it:
 * the rule's display, then the group's display, then a cycle through everything the rule accepts.
 */
export function displayStacks(rule: Rule, groups: Record<string, Group> = {}): Stack[] {
  if (typeof rule !== 'string' && rule.display) return entryStacks(rule.display).slice(0, 1)
  if (typeof rule !== 'string' && rule.group) {
    const group = findGroup(rule.group, groups)
    if (!group) return []
    if (!Array.isArray(group) && group.display) return entryStacks(group.display).slice(0, 1)
    return (Array.isArray(group) ? group : group.entries).flatMap(entryStacks)
  }
  return entryStacks(rule)
}

function findGroup(id: string, groups: Record<string, Group>): Group | undefined {
  const qualify = (name: string) => (name.includes(':') ? name : `restrictedinventory:${name}`)
  const key = Object.keys(groups).find((name) => qualify(name) === qualify(id))
  return key ? groups[key] : undefined
}

/** One-line description of a rule for tooltips. */
export function describeRule(rule: Rule): string {
  if (typeof rule === 'string') return rule
  if (rule.group) return `group ${rule.group}`
  const data = rule.components ?? rule.tag ?? rule.nbt
  let text = rule.item ?? '?'
  if (data) text += ` ${JSON.stringify(data)}`
  if (rule.display) text += ` (drawn as ${typeof rule.display === 'string' ? rule.display : rule.display.item})`
  return text
}
