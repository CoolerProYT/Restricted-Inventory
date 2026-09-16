import { defineConfig } from 'vitepress'

// GitHub Pages serves a project site from /<repository>/. For a custom domain or a user site, build with DOCS_BASE=/.
const base = process.env.DOCS_BASE ?? '/Restricted-Inventory/'

export default defineConfig({
  lang: 'en-US',
  title: 'RestrictedInventory',
  description: 'Slot-level inventory rules for Minecraft: lock player inventory slots to an item, tag, or exact variant.',
  base,
  cleanUrls: true,
  lastUpdated: true,
  srcExclude: ['README.md'],
  // `head` entries are not rewritten for the base path, unlike links, images and the theme logo.
  head: [['link', { rel: 'icon', type: 'image/png', href: `${base}logo.png` }]],
  markdown: {
    lineNumbers: true,
  },
  themeConfig: {
    logo: { src: '/logo.png', alt: '' },
    nav: [
      { text: 'Guide', link: '/guide/getting-started', activeMatch: '/guide/' },
      { text: 'Reference', link: '/reference/configuration', activeMatch: '/reference/' },
    ],
    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Getting started', link: '/guide/getting-started' },
          { text: 'Config screen', link: '/guide/config-screen' },
          { text: 'How restrictions work', link: '/guide/how-it-works' },
        ],
      },
      {
        text: 'Reference',
        items: [
          { text: 'Configuration', link: '/reference/configuration' },
          { text: 'Variant filters', link: '/reference/component-filters' },
          { text: 'Groups & displays', link: '/reference/groups' },
          { text: 'Teams & tags', link: '/reference/teams-and-tags' },
          { text: 'Commands & permissions', link: '/reference/commands' },
          { text: 'Compatibility', link: '/reference/compatibility' },
        ],
      },
    ],
    socialLinks: [{ icon: 'github', link: 'https://github.com/CoolerProYT/Restricted-Inventory' }],
    outline: { level: [2, 3], label: 'On this page' },
    search: { provider: 'local' },
    footer: {
      message: 'Released under the MIT License.',
      copyright: 'RestrictedInventory by CoolerProMC',
    },
  },
})
