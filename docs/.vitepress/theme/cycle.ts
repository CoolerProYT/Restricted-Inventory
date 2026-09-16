import { onMounted, onUnmounted, ref } from 'vue'

// One shared clock, so every cycling slot on a page flips together, as they do in-game.
const tick = ref(0)
let users = 0
let timer: ReturnType<typeof setInterval> | undefined

export function useCycle() {
  onMounted(() => {
    if (users++ === 0) timer = setInterval(() => tick.value++, 1000)
  })
  onUnmounted(() => {
    if (--users === 0) clearInterval(timer)
  })
  return tick
}
