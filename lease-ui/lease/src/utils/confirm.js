import { createVNode, render } from 'vue'
import ConfirmBox from '@/components/Confirm/ConfirmBox.vue'

/**
 * 全局确认弹窗 (基于 Promise)
 * @param {String} content 提示内容
 * @param {String} title 标题 (可选)
 * @returns {Promise<boolean>} 用户点击确定返回 true，点击取消返回 false
 */
export const showConfirm = (content, title = '操作确认') => {
  return new Promise((resolve) => {
    const container = document.createElement('div')
    document.body.appendChild(container)

    const removeNode = () => {
      render(null, container)
      container.remove()
    }

    const vnode = createVNode(ConfirmBox, {
      title,
      content,
      onConfirm: () => resolve(true),
      onCancel: () => resolve(false),
      remove: removeNode
    })

    render(vnode, container)
  })
}