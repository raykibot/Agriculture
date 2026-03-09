<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  message: { type: String, required: true },
  type: { type: String, default: 'info' }, // 'success', 'error', 'info', 'warning'
  duration: { type: Number, default: 3000 },
  onClose: { type: Function }
})

const visible = ref(false)

onMounted(() => {
  visible.value = true
  setTimeout(() => {
    visible.value = false
    setTimeout(() => {
      if (props.onClose) props.onClose()
    }, 300) // 等待退出动画完成
  }, props.duration)
})

// 根据状态返回不同的 Emoji 或 SVG
const getIcon = () => {
  switch (props.type) {
    case 'success': return '✨'
    case 'error': return '🚨'
    case 'warning': return '⚡'
    default: return '💡'
  }
}
</script>

<template>
  <transition name="toast-pop">
    <div v-if="visible" :class="['custom-toast', `toast-${type}`]">
      <span class="toast-icon">{{ getIcon() }}</span>
      <span class="toast-text">{{ message }}</span>
    </div>
  </transition>
</template>

<style scoped>
/* 基础胶囊样式与毛玻璃背景 */
.custom-toast {
  position: fixed;
  top: 60px; /* 距离顶部稍微多一点，悬浮感更强 */
  left: 50%;
  transform: translateX(-50%); /* 水平居中 */
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 28px;
  border-radius: 50px; /* 胶囊形状，显得极其轻盈 */
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.12); /* 宽大多层阴影，制造浮空感 */
  font-size: 0.95rem;
  font-weight: 600;
  pointer-events: none; /* 防止遮挡下方操作 */
  
  /* 毛玻璃核心特效 */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

/* ================= 各状态的轻量透亮配色 ================= */

/* 成功：极浅的透亮薄荷绿 */
.toast-success {
  background-color: rgba(236, 253, 245, 0.85); 
  color: #047857;
  border: 1px solid rgba(167, 243, 208, 0.6);
}

/* 失败：极浅的透亮玫瑰红 */
.toast-error {
  background-color: rgba(254, 242, 242, 0.85);
  color: #b91c1c;
  border: 1px solid rgba(254, 202, 202, 0.6);
}

/* 信息：极浅的透亮科技蓝 */
.toast-info {
  background-color: rgba(239, 246, 255, 0.85);
  color: #4338ca;
  border: 1px solid rgba(191, 219, 254, 0.6);
}

/* 警告：极浅的透亮琥珀黄 */
.toast-warning {
  background-color: rgba(255, 251, 235, 0.85);
  color: #b45309;
  border: 1px solid rgba(253, 230, 138, 0.6);
}

/* 图标微调 */
.toast-icon {
  font-size: 1.1rem;
  line-height: 1;
}

/* ================= 灵动的进出场动画 ================= */

/* 进场使用模拟弹簧（Spring）的贝塞尔曲线，Q弹灵动 */
.toast-pop-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 退场使用平滑加速曲线，利落消失 */
.toast-pop-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 初始/结束状态：位置偏上，透明度为0，体积微缩 */
.toast-pop-enter-from,
.toast-pop-leave-to {
  opacity: 0;
  transform: translate(-50%, -30px) scale(0.9);
}
</style>