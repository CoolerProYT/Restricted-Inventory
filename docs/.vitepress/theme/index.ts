import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import InventoryGrid from './components/InventoryGrid.vue'
import ItemSlot from './components/ItemSlot.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('InventoryGrid', InventoryGrid)
    app.component('ItemSlot', ItemSlot)
  },
} satisfies Theme
