import { createVNode, render } from 'vue'
import MessageComponent from '@/components/Message/Message.vue'

/**
 * 极简全局提示方法
 * @param {String} message 提示内容
 * @param {String} type 类型: 'success' | 'error' | 'warning' | 'info'
 * @param {Number} duration 停留时间 (毫秒)
 */
export const showMessage = (message, type = 'info', duration = 2500) => {
  // 1. 创建一个挂载用的容器
  const container = document.createElement('div')
  document.body.appendChild(container)

  // 2. 将 Vue 组件编译为虚拟 DOM (VNode)
  const vnode = createVNode(MessageComponent, {
    message,
    type,
    duration,
    onDestroy: () => {
      // 动画结束后，卸载虚拟 DOM 并移除真实 DOM 节点
      render(null, container)
      container.remove()
    }
  })

  // 3. 将虚拟 DOM 渲染到物理容器中
  render(vnode, container)
}