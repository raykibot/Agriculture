// src/utils/message.js
import { render, createVNode } from 'vue'
import MessageToast from '../components/MessageToast.vue'

// 创建一个挂载点
const mountNode = document.createElement('div')
document.body.appendChild(mountNode)

let currentVNode = null

export const showMessage = (message, type = 'info', duration = 3000) => {
  // 如果当前已经有弹窗，先卸载掉，避免重叠
  if (currentVNode) {
    render(null, mountNode)
  }

  // 创建 VNode (虚拟节点)，将参数传给组件的 props
  const vnode = createVNode(MessageToast, {
    message,
    type,
    duration,
    onClose: () => {
      // 动画结束后销毁组件
      render(null, mountNode)
      currentVNode = null
    }
  })

  // 将 VNode 渲染到真实的 DOM 节点上
  render(vnode, mountNode)
  currentVNode = vnode
}